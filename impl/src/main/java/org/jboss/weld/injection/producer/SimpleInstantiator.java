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
package org.jboss.weld.injection.producer;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Creates a new Java object by calling its class constructor. This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SimpleInstantiator<T> implements Instantiator<T> {

    private final ConstructorInjectionPoint<T> constructor;

    public SimpleInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl manager) {
        constructor = InjectionPointFactory.instance().createConstructorInjectionPoint(bean, type, manager);
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        return constructor.newInstance(manager, ctx);
    }

    public ConstructorInjectionPoint<T> getConstructor() {
        return constructor;
    }

    @Override
    public String toString() {
        return "SimpleInstantiator [constructor=" + constructor.getMember() + "]";
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
