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

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.DecoratorInjectionTarget;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.injection.producer.NonProducibleInjectionTarget;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.manager.api.WeldInjectionTargetFactory;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.InjectionTargets;

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
    private final EnhancedAnnotatedType<T> originalAnnotatedType;
    private final InjectionTargetService injectionTargetService;
    private final InjectionServices injectionServices;

    private volatile EnhancedAnnotatedType<T> annotatedType;
    private volatile AnnotatedTypeConfiguratorImpl<T> configurator;

    protected InjectionTargetFactoryImpl(AnnotatedType<T> type, BeanManagerImpl manager) {
        this.manager = manager;
        this.originalAnnotatedType = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type,
                manager.getId());
        this.injectionTargetService = manager.getServices().get(InjectionTargetService.class);
        this.injectionServices = manager.getServices().get(InjectionServices.class);
    }

    @Override
    public WeldInjectionTarget<T> createInjectionTarget(Bean<T> bean) {
        return createInjectionTarget(bean, false);
    }

    @Override
    public WeldInjectionTarget<T> createInterceptorInjectionTarget() {
        return createInjectionTarget(null, true);
    }

    private WeldInjectionTarget<T> createInjectionTarget(Bean<T> bean, boolean interceptor) {
        try {
            initAnnotatedType();
            return validate(createInjectionTarget(annotatedType, bean, interceptor));
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public BasicInjectionTarget<T> createInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, boolean interceptor) {
        BasicInjectionTarget<T> injectionTarget = chooseInjectionTarget(type, bean, interceptor);
        /*
         * Every InjectionTarget, regardless whether it's used within Weld's Bean implementation or requested from extension has
         * to be initialized after beans
         * (interceptors) are deployed.
         */
        initialize(injectionTarget);
        postProcess(injectionTarget);
        return injectionTarget;
    }

    @Override
    public synchronized AnnotatedTypeConfigurator<T> configure() {
        if (annotatedType != null) {
            BeanManagerLogger.LOG.unableToConfigureInjectionTargetFactory(annotatedType);
            throw new IllegalStateException();
        }
        if (configurator == null) {
            configurator = new AnnotatedTypeConfiguratorImpl<>(originalAnnotatedType);
        }
        return configurator;
    }

    private synchronized void initAnnotatedType() {
        if (annotatedType != null) {
            return;
        }
        if (configurator != null) {
            AnnotatedType<T> configuredType = configurator.complete();
            annotatedType = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(configuredType,
                    manager.getId());
        } else {
            annotatedType = originalAnnotatedType;
        }
    }

    private BasicInjectionTarget<T> chooseInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, boolean interceptor) {
        if (bean instanceof Decorator<?> || type.isAnnotationPresent(jakarta.decorator.Decorator.class)) {
            return new DecoratorInjectionTarget<T>(type, bean, manager);
        }
        NonProducibleInjectionTarget<T> nonProducible = InjectionTargets.createNonProducibleInjectionTarget(type, bean,
                manager);
        if (nonProducible != null) {
            return nonProducible;
        }
        if (bean instanceof SessionBean<?>) {
            return manager.getServices().get(EjbSupport.class).createSessionBeanInjectionTarget(type, (SessionBean<T>) bean,
                    manager);
        }
        if (bean instanceof Interceptor<?> || type.isAnnotationPresent(jakarta.interceptor.Interceptor.class)) {
            return BeanInjectionTarget.forCdiInterceptor(type, bean, manager);
        }
        if (interceptor) {
            return BasicInjectionTarget.createNonCdiInterceptor(type, manager);
        }
        return BeanInjectionTarget.createDefault(type, bean, manager);
    }

    protected InjectionTarget<T> createMessageDrivenInjectionTarget(EjbDescriptor<T> descriptor) {
        return prepareInjectionTarget(
                manager.getServices().get(EjbSupport.class).createMessageDrivenInjectionTarget(originalAnnotatedType,
                        descriptor, manager));
    }

    private BasicInjectionTarget<T> initialize(BasicInjectionTarget<T> injectionTarget) {
        injectionTargetService.addInjectionTargetToBeInitialized(originalAnnotatedType, injectionTarget);
        return injectionTarget;
    }

    private <I extends InjectionTarget<T>> I validate(I injectionTarget) {
        injectionTargetService.validateProducer(injectionTarget);
        return injectionTarget;
    }

    private void postProcess(InjectionTarget<T> injectionTarget) {
        if (injectionServices != null) {
            injectionServices.registerInjectionTarget(injectionTarget, originalAnnotatedType.slim());
        }
    }

    /*
     * Just a shortcut for calling validate, initialize and postProcess
     */
    private BasicInjectionTarget<T> prepareInjectionTarget(BasicInjectionTarget<T> injectionTarget) {
        try {
            postProcess(initialize(validate(injectionTarget)));
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public WeldInjectionTarget<T> createNonProducibleInjectionTarget() {
        return prepareInjectionTarget(NonProducibleInjectionTarget.create(originalAnnotatedType, null, manager));
    }
}
