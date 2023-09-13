/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;

/**
 * Injector implementation that injects {@link Inject}-annotated fields and calls CDI initializer methods.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DefaultInjector<T> implements Injector<T> {

    public static <T> DefaultInjector<T> of(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        return new DefaultInjector<T>(type, bean, beanManager);
    }

    private final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
    private final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;

    public DefaultInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        this.injectableFields = InjectionPointFactory.instance().getFieldInjectionPoints(bean, type, beanManager);
        this.initializerMethods = BeanMethods.getInitializerMethods(bean, type, beanManager);
    }

    @Override
    public void registerInjectionPoints(Set<InjectionPoint> injectionPoints) {
        injectionPoints.addAll(InjectionPoints.flattenInjectionPoints(this.injectableFields));
        injectionPoints.addAll(InjectionPoints.flattenParameterInjectionPoints(this.initializerMethods));
    }

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx, final BeanManagerImpl manager,
            SlimAnnotatedType<T> type, InjectionTarget<T> injectionTarget) {
        new InjectionContextImpl<T>(manager, injectionTarget, type, instance) {
            @Override
            public void proceed() {
                Beans.injectFieldsAndInitializers(instance, ctx, manager, injectableFields, initializerMethods);
            }
        }.run();
    }

    @Override
    public List<Set<FieldInjectionPoint<?, ?>>> getInjectableFields() {
        return injectableFields;
    }

    @Override
    public List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods() {
        return initializerMethods;
    }
}
