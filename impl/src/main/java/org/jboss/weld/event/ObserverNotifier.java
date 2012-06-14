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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.Observers;

/**
 * Provides event-related operations such sa observer method resolution and event delivery.
 *
 * @author Jozef Hartinger
 *
 */
public class ObserverNotifier {

    private final TypeSafeObserverResolver resolver;
    private final SharedObjectCache sharedObjectCache;

    public ObserverNotifier(TypeSafeObserverResolver resolver, SharedObjectCache sharedObjectCache) {
        this.resolver = resolver;
        this.sharedObjectCache = sharedObjectCache;
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... bindings) {
        Observers.checkEventObjectType(sharedObjectCache, event);
        return this.<T>resolveObserverMethods(event.getClass(), bindings);
    }

    public void fireEvent(Object event, Annotation... qualifiers) {
        fireEvent(event.getClass(), event, qualifiers);
    }

    public void fireEvent(Type eventType, Object event, Annotation... qualifiers) {
        Observers.checkEventObjectType(sharedObjectCache, event);
        Set<Annotation> qualifierSet = new HashSet<Annotation>(Arrays.asList(qualifiers));
        // we use the array of qualifiers for resolution so that we can catch duplicate qualifiers
        notifyObservers(event, qualifierSet, resolveObserverMethods(eventType, qualifiers));
    }

    public void fireEvent(Type eventType, Object event, Set<Annotation> qualifiers) {
        Observers.checkEventObjectType(sharedObjectCache, event);
        notifyObservers(event, qualifiers, resolveObserverMethods(eventType, qualifiers));
    }

    private <T> void notifyObservers(final T event, final Set<Annotation> qualifiers, final Set<ObserverMethod<? super T>> observers) {
        /*
         * The spec requires that the set of qualifiers of an event always contains the {@link Any} qualifier. We optimize this
         * and only do it for extension-provided observer methods since {@link ObserverMethodImpl} does not use the qualifiers
         * anyway.
         */
        Set<Annotation> allQualifiers = null;

        for (ObserverMethod<? super T> observer : observers) {
            if (observer instanceof ObserverMethodImpl<?, ?>) {
                observer.notify(event);
            } else {
                if (allQualifiers == null) {
                    allQualifiers = new HashSet<Annotation>(qualifiers);
                    allQualifiers.add(AnyLiteral.INSTANCE);
                    allQualifiers = Collections.unmodifiableSet(allQualifiers);
                }
                observer.notify(event, allQualifiers);
            }
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
        return cast(resolver.resolve(resolvable, true));
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
        return cast(resolver.resolve(resolvable, true));
    }

    public void clear() {
        resolver.clear();
    }
}
