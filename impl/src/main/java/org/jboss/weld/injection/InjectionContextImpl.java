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
package org.jboss.weld.injection;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author pmuir
 */
public abstract class InjectionContextImpl<T> implements InjectionContext<T> {

    private final BeanManagerImpl beanManager;
    private final InjectionTarget<T> injectionTarget;
    private final AnnotatedType<T> annotatedType;
    private final T target;

    public InjectionContextImpl(BeanManagerImpl beanManager, InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType,
            T target) {
        this.beanManager = beanManager;
        this.injectionTarget = injectionTarget;
        this.annotatedType = annotatedType;
        this.target = target;
    }

    public void run() {
        final InjectionServices injectionServices = beanManager.getServices().get(InjectionServices.class);
        if (injectionServices != null) {
            injectionServices.aroundInject(this);
        } else {
            proceed();
        }
    }

    public InjectionTarget<T> getInjectionTarget() {
        return injectionTarget;
    }

    public AnnotatedType<T> getAnnotatedType() {
        return annotatedType;
    }

    public T getTarget() {
        return target;
    }

}
