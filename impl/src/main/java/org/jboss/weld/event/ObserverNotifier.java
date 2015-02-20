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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Provides event-related operations such sa observer method resolution and event delivery.
 *
 *
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

    protected ObserverNotifier(TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        this.resolver = resolver;
        this.sharedObjectCache = services.get(SharedObjectCache.class);
        this.strict = strict;
        this.currentEventMetadata = services.get(CurrentEventMetadata.class);
        if (strict) {
            eventTypeCheckCache = ComputingCacheBuilder.newBuilder().build(new EventTypeCheck());
        } else {
            eventTypeCheckCache = null; // not necessary
        }
        // fall back to FJP.commonPool() if ExecutorServices are not installed
        this.asyncEventExecutor = services.getOptional(ExecutorServices.class).map((e) -> e.getTaskExecutor()).orElse(ForkJoinPool.commonPool());
    }

    public <T> ResolvedObservers<T> resolveObserverMethods(T event, Annotation... bindings) {
        checkEventObjectType(event);
        return this.<T>resolveObserverMethods(buildEventResolvable(event.getClass(), bindings));
    }

    public <T> ResolvedObservers<T> resolveObserverMethods(Type eventType, Set<Annotation> qualifiers) {
        checkEventObjectType(eventType);
        return this.<T>resolveObserverMethods(buildEventResolvable(eventType, qualifiers));
    }

    public <T> ResolvedObservers<T> resolveObserverMethods(Resolvable resolvable) {
        return cast(resolver.resolve(resolvable, true));
    }

    public void fireEvent(Object event, EventMetadata metadata, Annotation... qualifiers) {
        fireEvent(event.getClass(), event, metadata, qualifiers);
    }

    public void fireEvent(Type eventType, Object event, Annotation... qualifiers) {
        fireEvent(eventType, event, null, qualifiers);
    }

    public void fireEvent(Type eventType, Object event, EventMetadata metadata, Annotation... qualifiers) {
        checkEventObjectType(eventType);
        // we use the array of qualifiers for resolution so that we can catch duplicate qualifiers
        notify(resolveObserverMethods(buildEventResolvable(eventType, qualifiers)), event, metadata);
    }

    public void fireEvent(Object event, Resolvable resolvable) {
        checkEventObjectType(event);
        notify(resolveObserverMethods(resolvable), event, null);
    }

    public Resolvable buildEventResolvable(Type eventType, Set<Annotation> qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Set<Type> typeClosure = sharedObjectCache.getTypeClosureHolder(eventType).get();
        return new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(typeClosure)
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierUnchecked(QualifierInstance.ANY)
            .create();
    }

    public Resolvable buildEventResolvable(Type eventType, Annotation... qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        return new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(sharedObjectCache.getTypeClosureHolder(eventType).get())
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierUnchecked(QualifierInstance.ANY)
            .create();
    }

    public void clear() {
        resolver.clear();
        if (eventTypeCheckCache != null) {
            eventTypeCheckCache.clear();
        }
    }

    public void checkEventObjectType(Object event) {
        checkEventObjectType(event.getClass());
    }

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
             * If the runtime type of the event object contains a type variable, the container must throw an IllegalArgumentException.
             */
            if (Types.containsUnresolvedTypeVariableOrWildcard(resolvedType)) {
                return UtilLogger.LOG.typeParameterNotAllowedInEventType(eventType);
            }

            /*
             * If the runtime type of the event object is assignable to the type of a container lifecycle event, IllegalArgumentException
             * is thrown.
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

    public <T> void notify(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata) {
        notify(ResolvedObservers.of(observers), event, metadata);
    }

    public <T> void notify(ResolvedObservers<T> observers, T event, EventMetadata metadata) {
        if (!observers.isMetadataRequired()) {
            metadata = null;
        }
        notifySyncObservers(observers.getImmediateObservers(), event, metadata);
        notifyTransactionObservers(observers.getTransactionObservers(), event, metadata);
    }


    protected <T> void notifySyncObservers(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata) {
        if (observers.isEmpty()) {
            return;
        }
        final ThreadLocalStackReference<EventMetadata> stack = currentEventMetadata.pushIfNotNull(metadata);
        try {
            for (ObserverMethod<? super T> observer : observers) {
                observer.notify(event);
            }
        } finally {
            stack.pop();
        }
    }

    protected <T> void notifyTransactionObservers(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata) {
        notifySyncObservers(observers, event, metadata); // no transaction support
    }

    public <T, U extends T> CompletionStage<U> notifyAsync(ResolvedObservers<T> observers, U event, EventMetadata metadata, Executor executor) {
        if (!observers.isMetadataRequired()) {
            metadata = null;
        }
        notifyTransactionObservers(observers.getTransactionObservers(), event, metadata);
        return notifyAsyncObservers(observers.getImmediateObservers(), event, metadata, executor);
    }

    public <T, U extends T> CompletionStage<U> notifyAsyncObservers(List<ObserverMethod<? super T>> observers, U event, EventMetadata metadata, Executor executor) {
        if (executor == null) {
            executor = asyncEventExecutor;
        }
        return new AsyncEventDeliveryStage<>(() -> {
            notifySyncObservers(observers, event, metadata);
            return event;
        }, executor);
    }
}
