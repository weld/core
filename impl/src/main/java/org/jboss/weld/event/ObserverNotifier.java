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

import static org.jboss.weld.logging.messages.UtilMessage.EVENT_TYPE_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.UtilMessage.TYPE_PARAMETER_NOT_ALLOWED_IN_EVENT_TYPE;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.jboss.weld.exceptions.IllegalArgumentException;

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

    /**
     *
     * @param resolver
     * @param services
     * @param strict indicates whether event type should be performed or not
     * @return ObserverNotifier instance
     */
    public static ObserverNotifier of(TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        if (services.contains(TransactionServices.class)) {
            return new TransactionalObserverNotifier(resolver, services, strict);
        } else {
            return new ObserverNotifier(resolver, services, strict);
        }
    }

    private static final RuntimeException NO_EXCEPTION_MARKER = new RuntimeException();

    private final TypeSafeObserverResolver resolver;
    private final SharedObjectCache sharedObjectCache;
    private final boolean strict;
    private final ConcurrentMap<Type, RuntimeException> eventTypeCheckCache;

    protected ObserverNotifier(TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        this.resolver = resolver;
        this.sharedObjectCache = services.get(SharedObjectCache.class);
        this.strict = strict;
        if (strict) {
            eventTypeCheckCache = new MapMaker().makeComputingMap(new EventTypeCheck());
        } else {
            eventTypeCheckCache = null; // not necessary
        }
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings) {
        checkEventObjectType(event);
        return this.<T>resolveObserverMethods(event.getClass(), bindings);
    }

    public void fireEvent(Object event, Annotation... qualifiers) {
        fireEvent(event.getClass(), event, qualifiers);
    }

    public void fireEvent(Type eventType, Object event, Annotation... qualifiers) {
        checkEventObjectType(event);
        Set<Annotation> qualifierSet = new HashSet<Annotation>(Arrays.asList(qualifiers));
        // we use the array of qualifiers for resolution so that we can catch duplicate qualifiers
        notifyObservers(event, qualifierSet, resolveObserverMethods(eventType, qualifiers));
    }

    public void fireEvent(Type eventType, Object event, Set<Annotation> qualifiers) {
        checkEventObjectType(event);
        notifyObservers(event, qualifiers, resolveObserverMethods(eventType, qualifiers));
    }

    public void fireEvent(Object event, Resolvable resolvable) {
        checkEventObjectType(event);
        notifyObservers(event, Collections.<Annotation>emptySet(), resolveObserverMethods(resolvable));
    }

    private <T> void notifyObservers(final T event, Set<Annotation> qualifiers, final Set<ObserverMethod<? super T>> observers) {
        for (ObserverMethod<? super T> observer : observers) {
            /*
             * The spec requires that the set of qualifiers of an event always contains the {@link Any} qualifier. We optimize this
             * and only do it for extension-provided observer methods since {@link ObserverMethodImpl} does not use the qualifiers
             * anyway.
             */
            if (!(observer instanceof ObserverMethodImpl<?, ?>) && !qualifiers.contains(AnyLiteral.INSTANCE)) {
                qualifiers = new HashSet<Annotation>(qualifiers);
                qualifiers.add(AnyLiteral.INSTANCE);
                qualifiers = Collections.unmodifiableSet(qualifiers);
            }
            notifyObserver(event, qualifiers, observer);
        }
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Type eventType, Annotation... qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Resolvable resolvable = new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(sharedObjectCache.getTypeClosureHolder(eventType).get())
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierIfAbsent(AnyLiteral.INSTANCE)
            .create();
        return resolveObserverMethods(resolvable);
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Type eventType, Set<Annotation> qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Set<Type> typeClosure = sharedObjectCache.getTypeClosureHolder(eventType).get();
        Resolvable resolvable = new ResolvableBuilder(resolver.getMetaAnnotationStore())
            .addTypes(typeClosure)
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierIfAbsent(AnyLiteral.INSTANCE)
            .create();
        return resolveObserverMethods(resolvable);
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Resolvable resolvable) {
        return cast(resolver.resolve(resolvable, true));
    }

    public void clear() {
        resolver.clear();
        if (eventTypeCheckCache != null) {
            eventTypeCheckCache.clear();
        }
    }

    protected <T> void notifyObserver(final T event, Set<Annotation> qualifiers, final ObserverMethod<? super T> observer) {
        observer.notify(event, qualifiers);
    }

    public void checkEventObjectType(Object event) {
        checkEventObjectType(event.getClass());
    }

    public void checkEventObjectType(Type eventType) {
        if (strict) {
            RuntimeException exception = eventTypeCheckCache.get(eventType);
            if (exception != NO_EXCEPTION_MARKER) {
                throw exception;
            }
        }
    }

    private class EventTypeCheck implements Function<Type, RuntimeException> {
        @Override
        public RuntimeException apply(Type eventType) {
            Type[] typeParameters;
            final Type resolvedType = sharedObjectCache.getResolvedType(eventType);
            if (resolvedType instanceof Class<?>) {
                typeParameters = new Type[0];
            } else if (resolvedType instanceof ParameterizedType) {
                typeParameters = ((ParameterizedType) resolvedType).getActualTypeArguments();
            } else {
                return new IllegalArgumentException(EVENT_TYPE_NOT_ALLOWED, resolvedType);
            }
            /*
             * If the runtime type of the event object contains a type variable, the container must throw an IllegalArgumentException.
             */
            for (Type type : typeParameters) {
                if (type instanceof TypeVariable<?>) {
                    return new IllegalArgumentException(TYPE_PARAMETER_NOT_ALLOWED_IN_EVENT_TYPE, resolvedType);
                }
            }
            /*
             * If the runtime type of the event object is assignable to the type of a container lifecycle event, IllegalArgumentException
             * is thrown.
             */
            Class<?> resolvedClass = Reflections.getRawType(resolvedType);
            for (Class<?> containerEventType : Observers.CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES) {
                if (containerEventType.isAssignableFrom(resolvedClass)) {
                    return new IllegalArgumentException(EVENT_TYPE_NOT_ALLOWED, resolvedType);
                }
            }
            return NO_EXCEPTION_MARKER;
        }
    }
}
