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

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.CreationException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * An implementation of {@link InjectionTarget} for classes that do not fulfill bean class requirements (e.g. are abstract or
 * non-static inner classes).
 * Instances of these class can be injected using this implementation. If the application attempts to
 * {@link #produce(CreationalContext)} a new instance of the
 * class, {@link CreationException} is thrown.
 *
 * @see WELD-1441
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class NonProducibleInjectionTarget<T> extends BasicInjectionTarget<T> {

    public static <T> NonProducibleInjectionTarget<T> create(EnhancedAnnotatedType<T> type, Bean<T> bean,
            BeanManagerImpl beanManager) {
        return create(type, bean, null, null, beanManager);
    }

    public static <T> NonProducibleInjectionTarget<T> create(EnhancedAnnotatedType<T> type, Bean<T> bean, Injector<T> injector,
            LifecycleCallbackInvoker<T> invoker, BeanManagerImpl beanManager) {
        if (injector == null) {
            injector = DefaultInjector.of(type, bean, beanManager);
        }
        if (invoker == null) {
            invoker = DefaultLifecycleCallbackInvoker.of(type);
        }
        return new NonProducibleInjectionTarget<T>(type, bean, beanManager, injector, invoker);
    }

    /**
     *
     * @param type
     * @param bean
     * @param beanManager
     * @param injector
     * @param invoker
     */
    private NonProducibleInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Injector<T> injector,
            LifecycleCallbackInvoker<T> invoker) {
        super(type, bean, beanManager, injector, invoker);
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Set<InjectionPoint> injectionPoints) {
        return null;
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        throw BeanLogger.LOG.injectionTargetCannotProduceInstance(getAnnotated().getJavaClass());
    }

    @Override
    protected void checkType(EnhancedAnnotatedType<T> type) {
        // suppress type check
    }

    @Override
    public boolean hasInterceptors() {
        return false;
    }

    @Override
    public boolean hasDecorators() {
        return false;
    }

}
