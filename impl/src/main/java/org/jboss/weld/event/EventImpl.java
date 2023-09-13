/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.collections.WeldCollections.putIfAbsent;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.bean.builtin.AbstractFacade;
import org.jboss.weld.bean.builtin.FacadeInjectionPoint;
import org.jboss.weld.events.WeldEvent;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.EventObjectTypeResolverBuilder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.TypeResolver;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of the Event interface
 *
 * @param <T> The type of event being wrapped
 * @author David Allen
 * @seejakarta.enterprise.event.Event
 */
@SuppressFBWarnings(value = "SE_NO_SUITABLE_CONSTRUCTOR", justification = "Uses SerializationProxy")
public class EventImpl<T> extends AbstractFacade<T, WeldEvent<T>> implements WeldEvent<T>, Serializable {

    private static final String EVENT_ARGUMENT_NAME = "event";
    private static final String SUBTYPE_ARGUMENT_NAME = "subtype";
    private static final long serialVersionUID = 656782657242515455L;
    private static final int DEFAULT_CACHE_CAPACITY = 4;
    private static final NotificationOptions EMPTY_NOTIFICATION_OPTIONS = NotificationOptions.builder().build();

    public static <E> EventImpl<E> of(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        return new EventImpl<E>(injectionPoint, beanManager);
    }

    private final transient HierarchyDiscovery injectionPointTypeHierarchy;
    private transient volatile CachedObservers lastCachedObservers;
    private final transient Map<Class<?>, CachedObservers> cachedObservers;

    private EventImpl(InjectionPoint injectionPoint, BeanManagerImpl beanManager) {
        super(injectionPoint, null, beanManager);
        this.injectionPointTypeHierarchy = new HierarchyDiscovery(getType());
        this.cachedObservers = new ConcurrentHashMap<Class<?>, CachedObservers>(DEFAULT_CACHE_CAPACITY);
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return Formats.formatAnnotations(getQualifiers()) + " Event<" + Formats.formatType(getType()) + ">";
    }

    @Override
    public void fire(T event) {
        Preconditions.checkArgumentNotNull(event, EVENT_ARGUMENT_NAME);
        CachedObservers observers = getObservers(event);
        // we can do lenient here as the event type is checked within #getObservers()
        getBeanManager().getGlobalLenientObserverNotifier().notify(observers.observers, event, observers.metadata);
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event) {
        Preconditions.checkArgumentNotNull(event, EVENT_ARGUMENT_NAME);
        return fireAsyncInternal(event, EMPTY_NOTIFICATION_OPTIONS);
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
        Preconditions.checkArgumentNotNull(event, EVENT_ARGUMENT_NAME);
        Preconditions.checkArgumentNotNull(options, "options");
        return fireAsyncInternal(event, options);
    }

    private <U extends T> CompletionStage<U> fireAsyncInternal(U event, NotificationOptions options) {
        CachedObservers observers = getObservers(event);
        // we can do lenient here as the event type is checked within #getObservers()
        return getBeanManager().getGlobalLenientObserverNotifier().notifyAsync(observers.observers, event, observers.metadata,
                options);
    }

    private CachedObservers getObservers(T event) {
        Class<?> runtimeType = event.getClass();
        CachedObservers lastResolvedObservers = this.lastCachedObservers;
        // fast track for cases when the same type is used repeatedly
        if (lastResolvedObservers != null && lastResolvedObservers.rawType.equals(runtimeType)) {
            return lastResolvedObservers;
        }
        lastResolvedObservers = cachedObservers.get(runtimeType);
        if (lastResolvedObservers == null) {
            // this is not atomic and less elegant than computeIfAbsent but is faster and atomicity does not really matter here
            // as createCachedObservers() does not have any side effects
            lastResolvedObservers = putIfAbsent(cachedObservers, runtimeType, createCachedObservers(runtimeType));
        }
        return this.lastCachedObservers = lastResolvedObservers;
    }

    private CachedObservers createCachedObservers(Class<?> runtimeType) {
        final Type eventType = getEventType(runtimeType);
        // this performs type check
        final ResolvedObservers<T> observers = getBeanManager().getGlobalStrictObserverNotifier()
                .resolveObserverMethods(eventType, getQualifiers());
        final EventMetadata metadata = new EventMetadataImpl(eventType, getInjectionPoint(), getQualifiers());
        return new CachedObservers(runtimeType, observers, metadata);
    }

    @Override
    public WeldEvent<T> select(Annotation... qualifiers) {
        return selectEvent(this.getType(), qualifiers);
    }

    @Override
    public <U extends T> WeldEvent<U> select(Class<U> subtype, Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(subtype, SUBTYPE_ARGUMENT_NAME);
        return selectEvent(subtype, qualifiers);
    }

    @Override
    public <U extends T> WeldEvent<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(subtype, SUBTYPE_ARGUMENT_NAME);
        return selectEvent(subtype.getType(), qualifiers);
    }

    @Override
    public <X> WeldEvent<X> select(Type type, Annotation... qualifiers) {
        // verify if this was invoked on WeldInstance<Object>
        if (!this.getType().equals(Object.class)) {
            throw EventLogger.LOG.selectByTypeOnlyWorksOnObject();
        }
        // This cast should be safe, we make sure that this method is only invoked on WeldEvent<Object>
        // and any type X will always extend Object
        return (WeldEvent<X>) selectEvent(type, qualifiers);
    }

    private <U extends T> WeldEvent<U> selectEvent(Type subtype, Annotation[] newQualifiers) {
        getBeanManager().getGlobalStrictObserverNotifier().checkEventObjectType(subtype);
        return new EventImpl<U>(
                new FacadeInjectionPoint(getBeanManager(), getInjectionPoint(), Event.class, subtype, getQualifiers(),
                        newQualifiers),
                getBeanManager());
    }

    protected Type getEventType(Class<?> runtimeType) {
        Type resolvedType = runtimeType;
        if (Types.containsTypeVariable(resolvedType)) {
            /*
             * If the container is unable to resolve the parameterized type of the event object, it uses the specified type to
             * infer the parameterized type of the event types.
             */
            resolvedType = injectionPointTypeHierarchy.resolveType(resolvedType);
        }
        if (Types.containsTypeVariable(resolvedType)) {
            /*
             * Examining the hierarchy of the specified type did not help. This may still be one of the cases when combining the
             * event type and the specified type reveals the actual values for type variables. Let's try that.
             */
            Type canonicalEventType = Types.getCanonicalType(runtimeType);
            TypeResolver objectTypeResolver = new EventObjectTypeResolverBuilder(injectionPointTypeHierarchy.getResolver()
                    .getResolvedTypeVariables(),
                    new HierarchyDiscovery(canonicalEventType).getResolver()
                            .getResolvedTypeVariables())
                    .build();
            resolvedType = objectTypeResolver.resolveType(canonicalEventType);
        }
        return resolvedType;
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw EventLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<T> extends AbstractFacadeSerializationProxy<T, WeldEvent<T>> {

        private static final long serialVersionUID = 9181171328831559650L;

        public SerializationProxy(EventImpl<T> event) {
            super(event);
        }

        private Object readResolve() throws ObjectStreamException {
            return EventImpl.of(getInjectionPoint(), getBeanManager());
        }

    }

    private class CachedObservers {
        private final Class<?> rawType;
        private final ResolvedObservers<T> observers;
        private final EventMetadata metadata;

        private CachedObservers(Class<?> rawType, ResolvedObservers<T> observers, EventMetadata metadata) {
            this.rawType = rawType;
            this.observers = observers;
            this.metadata = metadata;
        }
    }
}
