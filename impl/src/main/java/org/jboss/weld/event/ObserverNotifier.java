/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.event;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.event.ObserverException;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.events.WeldNotificationOptions;
import org.jboss.weld.events.WeldNotificationOptions.NotificationMode;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provides event-related operations such as observer method resolution and event delivery.
 *
 * An ObserverNotifier may be created with strict checks enabled. In such case event type checks are performed. Otherwise, the
 * ObserverNotifier is called
 * lenient. The lenient version should be used for internal dispatching of events only.
 *
 * @author Jozef Hartinger
 * @author David Allen
 *
 */
public class ObserverNotifier {

    private static final RuntimeException NO_EXCEPTION_MARKER = new RuntimeException();

    private final TypeSafeObserverResolver resolver;
    private final SharedObjectCache sharedObjectCache;
    private final boolean strict;
    protected final CurrentEventMetadata currentEventMetadata;
    private final ComputingCache<Type, RuntimeException> eventTypeCheckCache;
    private final Executor asyncEventExecutor;
    private final ScheduledExecutorService timerExecutor;
    private final SecurityServices securityServices;
    private final LazyValueHolder<RequestContext> requestContextHolder;

    protected ObserverNotifier(String contextId, TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        this.resolver = resolver;
        this.sharedObjectCache = services.get(SharedObjectCache.class);
        this.strict = strict;
        this.currentEventMetadata = services.get(CurrentEventMetadata.class);
        if (strict) {
            this.eventTypeCheckCache = ComputingCacheBuilder.newBuilder().build(new EventTypeCheck());
        } else {
            // not necessary
            this.eventTypeCheckCache = null;
        }
        // fall back to FJP.commonPool() if ExecutorServices are not installed
        this.asyncEventExecutor = services.getOptional(ExecutorServices.class).map((e) -> e.getTaskExecutor())
                .orElse(ForkJoinPool.commonPool());
        // ScheduledExecutor might have null value
        this.timerExecutor = services.getOptional(ExecutorServices.class).map((e) -> e.getTimerExecutor()).orElse(null);
        this.securityServices = services.getRequired(SecurityServices.class);
        // LazyValueHolder is used because contexts are not ready yet at the point when ObserverNotifier is first initialized
        this.requestContextHolder = LazyValueHolder
                .forSupplier(() -> Container.instance(contextId).deploymentManager().instance()
                        .select(RequestContext.class, UnboundLiteral.INSTANCE).get());
    }

    /**
     * Resolves observer methods based on the given event type and qualifiers. If strict checks are enabled the given type is
     * verified.
     *
     * @param eventType the event object
     * @param qualifiers given event qualifiers
     * @return resolved observer methods
     */
    public <T> ResolvedObservers<T> resolveObserverMethods(Type eventType, Annotation... qualifiers) {
        checkEventObjectType(eventType);
        return this.<T> resolveObserverMethods(buildEventResolvable(eventType, qualifiers));
    }

    /**
     * Resolves observer methods based on the given event type and qualifiers. If strict checks are enabled the given type is
     * verified.
     *
     * @param eventType the event object
     * @param qualifiers given event qualifiers
     * @return resolved observer methods
     */
    public <T> ResolvedObservers<T> resolveObserverMethods(Type eventType, Set<Annotation> qualifiers) {
        checkEventObjectType(eventType);
        return this.<T> resolveObserverMethods(buildEventResolvable(eventType, qualifiers));
    }

    /**
     * Resolves observer methods using the given resolvable.
     *
     * @param resolvable the given resolvable
     * @return resolved observer methods
     */
    public <T> ResolvedObservers<T> resolveObserverMethods(Resolvable resolvable) {
        return cast(resolver.resolve(resolvable, true));
    }

    /**
     * Delivers the given event object to observer methods resolved based on the runtime type of the event object and given
     * event qualifiers. If strict checks
     * are enabled the event object type is verified.
     *
     * @param event the event object
     * @param metadata event metadata
     * @param qualifiers event qualifiers
     */
    public void fireEvent(Object event, EventMetadata metadata, Annotation... qualifiers) {
        fireEvent(event.getClass(), event, metadata, qualifiers);
    }

    /**
     * Delivers the given event object to observer methods resolved based on the given event type and qualifiers. If strict
     * checks are enabled the given type is
     * verified.
     *
     * @param eventType the given event type
     * @param event the given event object
     * @param qualifiers event qualifiers
     */
    public void fireEvent(Type eventType, Object event, Annotation... qualifiers) {
        fireEvent(eventType, event, null, qualifiers);
    }

    public void fireEvent(Type eventType, Object event, EventMetadata metadata, Annotation... qualifiers) {
        checkEventObjectType(eventType);
        // we use the array of qualifiers for resolution so that we can catch duplicate qualifiers
        notify(resolveObserverMethods(buildEventResolvable(eventType, qualifiers)), event, metadata);
    }

    /**
     * Delivers the given event object to observer methods resolved based on the given resolvable. If strict checks are enabled
     * the event object type is
     * verified.
     *
     * @param event the given event object
     * @param resolvable
     */
    public void fireEvent(Object event, Resolvable resolvable) {
        checkEventObjectType(event);
        notify(resolveObserverMethods(resolvable), event, null);
    }

    protected Resolvable buildEventResolvable(Type eventType, Set<Annotation> qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Set<Type> typeClosure = sharedObjectCache.getTypeClosureHolder(eventType).get();
        ResolvableBuilder resolvableBuilder = new ResolvableBuilder(resolver.getMetaAnnotationStore()).addTypes(typeClosure)
                .addType(Object.class)
                .addQualifiers(qualifiers)
                .addQualifierUnchecked(QualifierInstance.ANY);
        if (qualifiers.isEmpty()) {
            resolvableBuilder.addQualifier(Default.Literal.INSTANCE);
        }
        return resolvableBuilder.create();

    }

    protected Resolvable buildEventResolvable(Type eventType, Annotation... qualifiers) {
        return buildEventResolvable(eventType, Set.of(qualifiers));
    }

    /**
     * Clears cached observer method resolutions and event type checks.
     */
    public void clear() {
        resolver.clear();
        if (eventTypeCheckCache != null) {
            eventTypeCheckCache.clear();
        }
    }

    protected void checkEventObjectType(Object event) {
        checkEventObjectType(event.getClass());
    }

    /**
     * If strict checks are enabled this method performs event type checks on the given type. More specifically it verifies that
     * no type variables nor wildcards
     * are present within the event type. In addition, this method verifies, that the event type is not assignable to a
     * container lifecycle event type. If
     * strict checks are not enabled then this method does not perform any action.
     *
     * @param eventType the given event type
     * @throws org.jboss.weld.exceptions.IllegalArgumentException if the strict mode is enabled and the event type contains a
     *         type variable, wildcard or is
     *         assignable to a container lifecycle event type
     */
    public void checkEventObjectType(Type eventType) {
        if (strict) {
            RuntimeException exception = eventTypeCheckCache.getValue(eventType);
            if (exception != NO_EXCEPTION_MARKER) {
                throw exception;
            }
        }
    }

    private static class EventTypeCheck implements Function<Type, RuntimeException> {

        @Override
        public RuntimeException apply(Type eventType) {
            Type resolvedType = Types.getCanonicalType(eventType);
            /*
             * If the runtime type of the event object contains a type variable, the container must throw an
             * IllegalArgumentException.
             */
            if (Types.containsTypeVariable(resolvedType)) {
                return UtilLogger.LOG.typeParameterNotAllowedInEventType(eventType);
            }
            /*
             * If the runtime type of the event object is assignable to the type of a container lifecycle event,
             * IllegalArgumentException is thrown.
             */
            Class<?> resolvedClass = Reflections.getRawType(eventType);
            for (Class<?> containerEventType : Observers.CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES) {
                if (containerEventType.isAssignableFrom(resolvedClass)) {
                    return UtilLogger.LOG.eventTypeNotAllowed(eventType);
                }
            }
            return NO_EXCEPTION_MARKER;
        }
    }

    /**
     * Delivers the given synchronous event object to synchronous and transactional observer methods. Event metadata is made
     * available for injection into
     * observer methods, if needed. Asynchronous observer methods are ignored.
     *
     * @param observers the given observer methods
     * @param event the given event object
     * @param metadata event metadata
     */
    public <T> void notify(ResolvedObservers<T> observers, T event, EventMetadata metadata) {
        if (!observers.isMetadataRequired()) {
            metadata = null;
        }
        notifySyncObservers(observers.getImmediateSyncObservers(), event, metadata, ObserverExceptionHandler.IMMEDIATE_HANDLER);
        notifyTransactionObservers(observers.getTransactionObservers(), event, metadata,
                ObserverExceptionHandler.IMMEDIATE_HANDLER);
    }

    protected <T> void notifySyncObservers(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata,
            ObserverExceptionHandler handler) {
        if (observers.isEmpty()) {
            return;
        }
        final ThreadLocalStackReference<EventMetadata> stack = currentEventMetadata.pushIfNotNull(metadata);
        try {
            for (ObserverMethod<? super T> observer : observers) {
                try {
                    Observers.notify(observer, event, metadata);
                } catch (Throwable throwable) {
                    handler.handle(throwable);
                }
            }
        } finally {
            stack.pop();
        }
    }

    protected <T> void notifyTransactionObservers(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata,
            ObserverExceptionHandler handler) {
        notifySyncObservers(observers, event, metadata, ObserverExceptionHandler.IMMEDIATE_HANDLER); // no transaction support
    }

    /**
     * Delivers the given asynchronous event object to given observer asynchronous observer methods.
     *
     * Asynchronous observer methods are scheduled to be notified in a separate thread. Note that this method exits just after
     * event delivery to asynchronous
     * observer methods is scheduled. {@link EventMetadata} is made available for injection into observer methods, if needed.
     *
     * Note that if any of the observer methods throws an exception, it is never thrown out of this method. Instead, all the
     * exceptions are grouped together
     * using {@link CompletionException} and the returned {@link CompletionStage} fails with this compound exception.
     *
     * If an executor is provided then observer methods are notified using this executor. Otherwise, Weld's task executor is
     * used.
     *
     * @param observers the given observer methods
     * @param event the given event object
     * @param metadata event metadata
     * @param options
     */
    public <T, U extends T> CompletionStage<U> notifyAsync(ResolvedObservers<T> observers, U event, EventMetadata metadata,
            NotificationOptions options) {
        if (!observers.isMetadataRequired()) {
            metadata = null;
        }
        return notifyAsyncObservers(observers.getAsyncObservers(), event, metadata, options.getExecutor(), options);
    }

    protected <T, U extends T> CompletionStage<U> notifyAsyncObservers(List<ObserverMethod<? super T>> observers, U event,
            EventMetadata metadata,
            Executor executor, NotificationOptions options) {
        if (executor == null) {
            executor = asyncEventExecutor;
        }
        if (observers.isEmpty()) {
            return AsyncEventDeliveryStage.completed(event, executor);
        }
        // We should always initialize and validate all notification options first
        final NotificationMode mode = initModeOption(options.get(WeldNotificationOptions.MODE));
        final Long timeout = initTimeoutOption(options.get(WeldNotificationOptions.TIMEOUT));
        final Consumer<Runnable> securityContextActionConsumer = securityServices.getSecurityContextAssociator();
        // grab current TCCL
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final ObserverExceptionHandler exceptionHandler;
        CompletableFuture<U> completableFuture;

        if (observers.size() > 1 && NotificationMode.PARALLEL.equals(mode)) {
            // Attempt to notify async observers in parallel
            exceptionHandler = new CollectingExceptionHandler(new CopyOnWriteArrayList<>());
            List<CompletableFuture<T>> completableFutures = new ArrayList<>(observers.size());
            for (ObserverMethod<? super T> observer : observers) {
                completableFutures.add(CompletableFuture.supplyAsync(
                        createSupplier(tccl, securityContextActionConsumer, event, metadata, exceptionHandler, false, () -> {
                            notifyAsyncObserver(observer, event, metadata, exceptionHandler);
                        }), executor));
            }
            completableFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[] {}))
                    .thenApply((ignoredVoid) -> {
                        handleExceptions(exceptionHandler);
                        return event;
                    });
        } else {
            // Async observers are notified serially in a single worker thread
            exceptionHandler = new CollectingExceptionHandler();
            completableFuture = CompletableFuture.supplyAsync(
                    createSupplier(tccl, securityContextActionConsumer, event, metadata, exceptionHandler, true, () -> {
                        for (ObserverMethod<? super T> observer : observers) {
                            notifyAsyncObserver(observer, event, metadata, exceptionHandler);
                        }
                    }), executor);
        }

        // If NotificationOptionKeys.TIMEOUT is set, we will trigger the counter and use CompletableFuture.anyOf()
        if (timeout != null) {
            completableFuture = CompletableFuture.anyOf(completableFuture, startTimer(timeout))
                    .thenApply((ignoredObject) -> event);
        }
        return new AsyncEventDeliveryStage<>(completableFuture, executor);
    }

    /**
     * Verifies that, if timeout options was set, the executor is available and input value for timeout can be interpreted as
     * Long.
     * Returns the timeout value if all is alright, null if this option was not requested.
     */
    private Long initTimeoutOption(Object timeoutOptionValue) {
        if (timeoutOptionValue != null) {
            // throw exception if we don't have scheduled executor available
            if (timerExecutor == null) {
                throw EventLogger.LOG.noScheduledExecutorServicesProvided();
            }
            // now verify that the input value makes sense
            try {
                String inputValue = timeoutOptionValue.toString();
                return Long.parseLong(inputValue);
            } catch (NumberFormatException nfe) {
                throw EventLogger.LOG.invalidInputValueForTimeout(nfe);
            }
        } else {
            return null;
        }
    }

    private NotificationMode initModeOption(Object value) {
        if (value != null) {
            NotificationMode mode = NotificationMode.of(value);
            if (mode == null) {
                throw EventLogger.LOG.invalidNotificationMode(value);
            }
            return mode;
        }
        return null;
    }

    /**
     * Starts the timer thread and returns it as CompletableFuture.
     */
    private <T> CompletableFuture<T> startTimer(Long timeout) {
        CompletableFuture<T> timeoutFuture = new CompletableFuture<T>();
        timerExecutor.schedule(() -> timeoutFuture.completeExceptionally(new TimeoutException()), timeout,
                TimeUnit.MILLISECONDS);
        return timeoutFuture;
    }

    private <T, U extends T> void notifyAsyncObserver(ObserverMethod<? super T> observer, U event, EventMetadata metadata,
            ObserverExceptionHandler exceptionHandler) {
        try {
            Observers.notify(observer, event, metadata);
        } catch (Throwable e) {
            exceptionHandler.handle(e);
        }
    }

    /**
     * The supplier associates the security context with the current thread, activates the request context, runs the "notify"
     * action and handles exceptions if
     * required.
     *
     * @param event
     * @param metadata
     * @param exceptionHandler
     * @param handleExceptions
     * @param notifyAction
     * @return a new supplier
     */
    private <T, U extends T> Supplier<T> createSupplier(ClassLoader threadContextClassLoader,
            Consumer<Runnable> securityContextActionConsumer, U event, EventMetadata metadata,
            ObserverExceptionHandler exceptionHandler,
            boolean handleExceptions, Runnable notifyAction) {
        return () -> {
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            final ThreadLocalStackReference<EventMetadata> stack = currentEventMetadata.pushIfNotNull(metadata);
            final RequestContext requestContext = requestContextHolder.get();
            securityContextActionConsumer.accept(() -> {
                try {
                    Thread.currentThread().setContextClassLoader(threadContextClassLoader);
                    requestContext.activate();
                    notifyAction.run();
                } finally {
                    stack.pop();
                    requestContext.invalidate();
                    requestContext.deactivate();
                    Thread.currentThread().setContextClassLoader(originalCl);
                }
            });
            if (handleExceptions) {
                handleExceptions(exceptionHandler);
            }
            return event;
        };
    }

    @SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "https://github.com/findbugsproject/findbugs/issues/79")
    private void handleExceptions(ObserverExceptionHandler handler) {
        List<Throwable> handledExceptions = handler.getHandledExceptions();
        if (!handledExceptions.isEmpty()) {
            CompletionException exception = null;
            if (handledExceptions.size() == 1) {
                exception = new CompletionException(handledExceptions.get(0));
            } else {
                exception = new CompletionException(null);
            }
            for (Throwable handledException : handledExceptions) {
                exception.addSuppressed(handledException);
            }
            throw exception;
        }
    }

    /**
     * There are two different strategies of exception handling for observer methods. When an exception is raised by a
     * synchronous or transactional observer for
     * a synchronous event, this exception stops the notification chain and the exception is propagated immediately. On the
     * other hand, an exception thrown
     * during asynchronous event delivery never is never propagated directly. Instead, all the exceptions for a given
     * asynchronous event are collected and then
     * made available together using CompletionException.
     *
     * @author Jozef Hartinger
     *
     */
    protected interface ObserverExceptionHandler {

        ObserverExceptionHandler IMMEDIATE_HANDLER = throwable -> {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            if (throwable instanceof Error) {
                throw (Error) throwable;
            }
            throw new ObserverException(throwable);
        };

        void handle(Throwable throwable);

        default List<Throwable> getHandledExceptions() {
            return Collections.emptyList();
        }
    }

    static class CollectingExceptionHandler implements ObserverExceptionHandler {

        private List<Throwable> throwables;

        CollectingExceptionHandler() {
            this(new LinkedList<>());
        }

        CollectingExceptionHandler(List<Throwable> throwables) {
            this.throwables = throwables;
        }

        @Override
        public void handle(Throwable throwable) {
            throwables.add(throwable);
        }

        @Override
        public List<Throwable> getHandledExceptions() {
            return throwables;
        }
    }
}
