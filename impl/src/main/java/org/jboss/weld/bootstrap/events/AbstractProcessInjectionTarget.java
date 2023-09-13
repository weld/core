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
package org.jboss.weld.bootstrap.events;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractProcessInjectionTarget<T> extends AbstractDefinitionContainerEvent {

    protected static <X> void fire(BeanManagerImpl beanManager, AbstractClassBean<X> bean) {
        if (beanManager.isBeanEnabled(bean)) {
            new ProcessBeanInjectionTarget<X>(beanManager, bean) {
            }.fire();
        }
    }

    protected static <X> InjectionTarget<X> fire(BeanManagerImpl beanManager, AnnotatedType<X> annotatedType,
            InjectionTarget<X> injectionTarget) {
        ProcessSimpleInjectionTarget<X> processSimpleInjectionTarget = new ProcessSimpleInjectionTarget<X>(beanManager,
                annotatedType, injectionTarget) {
        };
        processSimpleInjectionTarget.fire();
        return processSimpleInjectionTarget.getInjectionTargetInternal();
    }

    protected final AnnotatedType<T> annotatedType;

    protected AbstractProcessInjectionTarget(BeanManagerImpl beanManager, AnnotatedType<T> annotatedType) {
        super(beanManager, ProcessInjectionTarget.class, new Type[] { annotatedType.getBaseType() });
        this.annotatedType = annotatedType;
    }

    public List<Throwable> getDefinitionErrors() {
        return super.getErrors();
    }

    public AnnotatedType<T> getAnnotatedType() {
        checkWithinObserverNotification();
        return annotatedType;
    }

}
