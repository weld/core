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

import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.Function;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Provides event-related operations such as observer method resolution and event delivery.
 *
 * An ObserverNotifier may be created with strict checks enabled. In such case event type checks are performed. Otherwise, the ObserverNotifier is called
 * lenient. The lenient version should be used for internal dispatching of events only.
 *
 * @author Jozef Hartinger
 * @author David Allen
 *
 */
public class ObserverNotifier {

    /**
     *
     * @param resolver
     * @param services
     * @param strict indicates whether event type should be performed or not
     * @return ObserverNotifier instance
     */
    public static ObserverNotifier of(String contextId, TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        if (services.contains(TransactionServices.class)) {
            return new TransactionalObserverNotifier(contextId, resolver, services, strict);
        } else {
            return new ObserverNotifier(resolver, services, strict);
        }
    }

    private static final RuntimeException NO_EXCEPTION_MARKER = new RuntimeException();

    private final TypeSafeObserverResolver resolver;
    private final SharedObjectCache sharedObjectCache;
    private final boolean strict;
    protected final CurrentEventMetadata currentEventMetadata;
    private final ComputingCache<Type, RuntimeException> eventTypeCheckCache;

    protected ObserverNotifier(TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
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
    }

    /**
     * Resolves observer methods based on the given event type and qualifiers. If strict checks are enabled the given type is verified.
     *
     * @param event the event object
     * @param qualifiers given event qualifiers
     * @return resolved observer methods
     */
    public <T> ResolvedObservers<T> resolveObserverMethods(Type eventType, Annotation... qualifiers) {
        checkEventObjectType(eventType);
        return this.<T>resolveObserverMethods(buildEventResolvable(eventType, qualifiers));
    }

    /**
     * Resolves observer methods based on the given event type and qualifiers. If strict checks are enabled the given type is verified.
     *
     * @param event the event object
     * @param qualifiers given event qualifiers
     * @return resolved observer methods
     */
    public <T> ResolvedObservers<T> resolveObserverMethods(Type eventType, Set<Annotation> qualifiers) {
        checkEventObjectType(eventType);
        return this.<T>resolveObserverMethods(buildEventResolvable(eventType, qualifiers));
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
     * Delivers the given event object to observer methods resolved based on the runtime type of the event object and given event qualifiers. If strict checks
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
     * Delivers the given event object to observer methods resolved based on the given event type and qualifiers. If strict checks are enabled the given type is
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
     * Delivers the given event object to observer methods resolved based on the given resolvable. If strict checks are enabled the event object type is
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
        return new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(typeClosure)
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierUnchecked(QualifierInstance.ANY)
            .create();
    }

    protected Resolvable buildEventResolvable(Type eventType, Annotation... qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        return new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(sharedObjectCache.getTypeClosureHolder(eventType).get())
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierUnchecked(QualifierInstance.ANY)
            .create();
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
     * If strict checks are enabled this method performs event type checks on the given type. More specifically it verifies that no type variables nor wildcards
     * are present within the event type. In addition, this method verifies, that the event type is not assignable to a container lifecycle event type. If
     * strict checks are not enabled then this method does not perform any action.
     *
     * @param eventType the given event type
     * @throws org.jboss.weld.exceptions.IllegalArgumentException if the strict mode is enabled and the event type contains a type variable, wildcard or is
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
             * If the runtime type of the event object contains a type variable, the container must throw an IllegalArgumentException.
             */
            if (Types.containsTypeVariable(resolvedType)) {
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

    /**
     * Delivers the given event object to given observer methods. Event metadata is made available for injection into observer methods, if needed.
     *
     * @param observers the given observer methods
     * @param event the given event object
     * @param metadata event metadata
     */
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
}
