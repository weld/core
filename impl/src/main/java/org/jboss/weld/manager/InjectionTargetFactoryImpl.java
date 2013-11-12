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
package org.jboss.weld.manager;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.Interceptors;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.DecoratorInjectionTarget;
import org.jboss.weld.injection.producer.InjectionTargetInitializationContext;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.injection.producer.LifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.NonProducibleInjectionTarget;
import org.jboss.weld.injection.producer.NoopLifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.ejb.SessionBeanInjectionTarget;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.api.WeldInjectionTargetFactory;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Factory capable of producing {@link InjectionTarget} implementations for a given combination of {@link AnnotatedType} and
 * {@link Bean} objects.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InjectionTargetFactoryImpl<T> implements WeldInjectionTargetFactory<T> {

    private final BeanManagerImpl manager;
    private final EnhancedAnnotatedType<T> type;
    private final InjectionTargetService injectionTargetService;
    private final InjectionServices injectionServices;

    protected InjectionTargetFactoryImpl(AnnotatedType<T> type, BeanManagerImpl manager) {
        this.manager = manager;
        this.type = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type, manager.getId());
        this.injectionTargetService = manager.getServices().get(InjectionTargetService.class);
        this.injectionServices = manager.getServices().get(InjectionServices.class);
    }

    @Override
    public InjectionTarget<T> createInjectionTarget(Bean<T> bean) {
        return createInjectionTarget(bean, false);
    }

    @Override
    public InjectionTarget<T> createInterceptorInjectionTarget() {
        return createInjectionTarget(null, true);
    }

    private InjectionTarget<T> createInjectionTarget(Bean<T> bean, boolean interceptor) {
        try {
            InjectionTarget<T> injectionTarget = createInjectionTarget(type, bean, interceptor);
            injectionTargetService.validateProducer(injectionTarget);
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public BasicInjectionTarget<T> createInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, boolean interceptor) {
        BasicInjectionTarget<T> injectionTarget = chooseInjectionTarget(type, bean, interceptor);
        /*
         * Every InjectionTarget, regardless whether it's used within Weld's Bean implementation or requested from extension has
         * to be initialized after beans (interceptors) are deployed.
         */
        injectionTargetService.addInjectionTargetToBeInitialized(new InjectionTargetInitializationContext<T>(type, injectionTarget));
        postProcessInjectionTarget(type, injectionTarget);
        return injectionTarget;
    }

    private BasicInjectionTarget<T> chooseInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, boolean interceptor) {
        if (bean instanceof Decorator<?> || type.isAnnotationPresent(javax.decorator.Decorator.class)) {
            return new DecoratorInjectionTarget<T>(type, bean, manager);
        }
        if (type.isAbstract()) {
            if (type.getJavaClass().isInterface()) {
                throw BeanLogger.LOG.injectionTargetCannotBeCreatedForInterface(type);
            }
            BeanLogger.LOG.injectionTargetCreatedForAbstractClass(type.getJavaClass());
            return new NonProducibleInjectionTarget<T>(type, bean, manager);
        }
        if (Reflections.isNonStaticInnerClass(type.getJavaClass())) {
            BeanLogger.LOG.injectionTargetCreatedForNonStaticInnerClass(type.getJavaClass());
            return new NonProducibleInjectionTarget<T>(type, bean, manager);
        }
        if (Beans.getBeanConstructor(type) == null) {
            if (bean != null) {
                throw BeanLogger.LOG.injectionTargetCreatedForClassWithoutAppropriateConstructorException(type.getJavaClass());
            }
            BeanLogger.LOG.injectionTargetCreatedForClassWithoutAppropriateConstructor(type.getJavaClass());
            return new NonProducibleInjectionTarget<T>(type, bean, manager);
        }
        if (bean instanceof SessionBean<?>) {
            return new SessionBeanInjectionTarget<T>(type, (SessionBean<T>) bean, manager);
        }
        if (interceptor){
            return new InterceptorInjectionTarget<T>(type, manager);
        }
        return new BeanInjectionTarget<T>(type, bean, manager);
    }

    protected InjectionTarget<T> createMessageDrivenInjectionTarget() {
        try {
            InjectionTarget<T> injectionTarget = new BasicInjectionTarget<T>(type, null, manager);
            postProcessInjectionTarget(type, injectionTarget);
            injectionTargetService.validateProducer(injectionTarget);
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    private <X> void postProcessInjectionTarget(AnnotatedType<X> type, InjectionTarget<X> injectionTarget) {
        if (injectionServices != null) {
            injectionServices.registerInjectionTarget(injectionTarget, type);
        }
    }


    /**
     * {@link InjectionTarget} for interceptors which do not have associated {@link Interceptor}. These interceptors are a
     * result of using {@link Interceptors} annotation directly on the target class.
     *
     * @author Jozef Hartinger
     */
    private static class InterceptorInjectionTarget<T> extends BasicInjectionTarget<T> {
        public InterceptorInjectionTarget(EnhancedAnnotatedType<T> type, BeanManagerImpl beanManager) {
            super(type, null, beanManager);
        }

        @Override
        protected LifecycleCallbackInvoker<T> initInvoker(EnhancedAnnotatedType<T> type) {
            return NoopLifecycleCallbackInvoker.getInstance();
        }
    }
}
