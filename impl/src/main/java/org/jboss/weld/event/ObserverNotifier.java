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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.Observers;

import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * Provides event-related operations such sa observer method resolution and event delivery.
 *
 * @author Jozef Hartinger
 * @author David Allen
 *
 */
public class ObserverNotifier {

    public static ObserverNotifier of(TypeSafeObserverResolver resolver, BeanManagerImpl beanManager) {
        if (beanManager.getServices().contains(TransactionServices.class)) {
            return new TransactionalObserverNotifier(resolver, beanManager);
        } else {
            return new ObserverNotifier(resolver, beanManager);
        }
    }

    private final TypeSafeObserverResolver resolver;
    protected final BeanManagerImpl beanManager;
    private final SharedObjectCache sharedObjectCache;

    protected ObserverNotifier(TypeSafeObserverResolver resolver, BeanManagerImpl beanManager) {
        this.resolver = resolver;
        this.beanManager = beanManager;
        final ServiceRegistry services = beanManager.getServices();
        this.sharedObjectCache = services.get(SharedObjectCache.class);
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
        notifyObservers(event, resolveObserverMethods(eventType, qualifiers));
    }

    public void fireEvent(Type eventType, Object event, Set<Annotation> qualifiers) {
        Observers.checkEventObjectType(sharedObjectCache, event);
        notifyObservers(event, resolveObserverMethods(eventType, qualifiers));
    }

    private <T> void notifyObservers(final T event, final Set<ObserverMethod<? super T>> observers) {
        for (ObserverMethod<? super T> observer : observers) {
            notifyObserver(event, observer);
        }
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Type eventType, Annotation... qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Resolvable resolvable = new ResolvableBuilder(beanManager)
            .addTypes(sharedObjectCache.getTypeClosure(eventType))
            .addType(Object.class)
            .addQualifiers(qualifiers)
            .addQualifierIfAbsent(AnyLiteral.INSTANCE)
            .create();
        return cast(resolver.resolve(resolvable, true));
    }

    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(Type eventType, Set<Annotation> qualifiers) {
        // We can always cache as this is only ever called by Weld where we avoid non-static inner classes for annotation literals
        Set<Type> typeClosure = sharedObjectCache.getTypeClosure(eventType);
        Resolvable resolvable = new ResolvableBuilder(beanManager)
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

    protected <T> void notifyObserver(final T event, final ObserverMethod<? super T> observer) {
        observer.notify(event);
    }
}
