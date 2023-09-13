/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.producer.Injector;
import org.jboss.weld.injection.producer.LifecycleCallbackInvoker;
import org.jboss.weld.injection.producer.NonProducibleInjectionTarget;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Martin Kouba
 */
public class InjectionTargets {

    private InjectionTargets() {
    }

    /**
     *
     * @param type
     * @param bean
     * @param beanManager
     * @return a {@link NonProducibleInjectionTarget} instance if necessary, <code>null</code> otherwise
     */
    public static <T> NonProducibleInjectionTarget<T> createNonProducibleInjectionTarget(EnhancedAnnotatedType<T> type,
            Bean<T> bean,
            BeanManagerImpl beanManager) {
        return createNonProducibleInjectionTarget(type, bean, null, null, beanManager);
    }

    /**
     *
     * @param type
     * @param bean
     * @param injector
     * @param invoker
     * @param beanManager
     * @return a {@link NonProducibleInjectionTarget} instance if necessary, <code>null</code> otherwise
     */
    public static <T> NonProducibleInjectionTarget<T> createNonProducibleInjectionTarget(EnhancedAnnotatedType<T> type,
            Bean<T> bean, Injector<T> injector,
            LifecycleCallbackInvoker<T> invoker, BeanManagerImpl beanManager) {
        try {
            if (type.isAbstract()) {
                if (type.getJavaClass().isInterface()) {
                    throw BeanLogger.LOG.injectionTargetCannotBeCreatedForInterface(type);
                }
                BeanLogger.LOG.injectionTargetCreatedForAbstractClass(type.getJavaClass());
                return NonProducibleInjectionTarget.create(type, bean, injector, invoker, beanManager);
            }
            if (!Reflections.isTopLevelOrStaticNestedClass(type.getJavaClass())) {
                BeanLogger.LOG.injectionTargetCreatedForNonStaticInnerClass(type.getJavaClass());
                return NonProducibleInjectionTarget.create(type, bean, injector, invoker, beanManager);
            }
            if (Beans.getBeanConstructor(type) == null) {
                if (bean != null) {
                    throw BeanLogger.LOG
                            .injectionTargetCreatedForClassWithoutAppropriateConstructorException(type.getJavaClass());
                }
                BeanLogger.LOG.injectionTargetCreatedForClassWithoutAppropriateConstructor(type.getJavaClass());
                return NonProducibleInjectionTarget.create(type, null, injector, invoker, beanManager);
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
        return null;
    }

}
