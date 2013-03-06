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

import static org.jboss.weld.annotated.AnnotatedTypeValidator.validateAnnotatedType;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.DecoratorInjectionTarget;
import org.jboss.weld.injection.producer.BeanInjectionTarget;
import org.jboss.weld.injection.producer.InjectionTargetInitializationContext;
import org.jboss.weld.injection.producer.InjectionTargetService;
import org.jboss.weld.injection.producer.ejb.SessionBeanInjectionTarget;
import org.jboss.weld.resources.ClassTransformer;

/**
 * Factory capable of producing {@link InjectionTarget} implementations for a given combination of {@link AnnotatedType} and
 * {@link Bean} objects.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InjectionTargetFactoryImpl<T> implements InjectionTargetFactory<T> {

    private final BeanManagerImpl manager;
    private final EnhancedAnnotatedType<T> type;
    private final InjectionTargetService injectionTargetService;

    protected InjectionTargetFactoryImpl(AnnotatedType<T> type, BeanManagerImpl manager) {
        this.manager = manager;
        validateAnnotatedType(type);
        this.type = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(type, manager.getId());
        this.injectionTargetService = manager.getServices().get(InjectionTargetService.class);
    }

    @Override
    public InjectionTarget<T> createInjectionTarget(Bean<T> bean) {
        try {
            InjectionTarget<T> injectionTarget = createInjectionTarget(type, bean);
            injectionTargetService.validateProducer(injectionTarget);
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }

    public BasicInjectionTarget<T> createInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean) {
        BasicInjectionTarget<T> injectionTarget = null;
        if (bean instanceof Decorator<?> || type.isAnnotationPresent(javax.decorator.Decorator.class)) {
            injectionTarget = new DecoratorInjectionTarget<T>(type, bean, manager);
        } else if (bean instanceof SessionBean<?>) {
            injectionTarget = new SessionBeanInjectionTarget<T>(type, (SessionBean<T>) bean, manager);
        } else {
            injectionTarget = new BeanInjectionTarget<T>(type, bean, manager);
        }
        /*
         * Every InjectionTarget, regardless whether it's used within Weld's Bean implementation or requested from extension has
         * to be initialized after beans (interceptors) are deployed.
         */
        manager.getServices().get(InjectionTargetService.class).addInjectionTargetToBeInitialized(new InjectionTargetInitializationContext<T>(type, injectionTarget));
        return injectionTarget;
    }

    protected InjectionTarget<T> createMessageDrivenInjectionTarget() {
        try {
            InjectionTarget<T> injectionTarget = new BasicInjectionTarget<T>(type, null, manager);
            injectionTargetService.validateProducer(injectionTarget);
            return injectionTarget;
        } catch (Throwable e) {
            throw new IllegalArgumentException(e);
        }
    }
}
