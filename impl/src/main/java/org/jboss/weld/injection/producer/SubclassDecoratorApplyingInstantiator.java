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

import java.util.List;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A wrapper over {@link SubclassedComponentInstantiator} that registers decorators using the enhanced subclass. This is used
 * for enabling decorators on managed beans.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SubclassDecoratorApplyingInstantiator<T> extends AbstractDecoratorApplyingInstantiator<T> {

    public SubclassDecoratorApplyingInstantiator(String contextId, Instantiator<T> delegate, Bean<T> bean,
            List<Decorator<?>> decorators, Class<? extends T> implementationClass) {
        super(contextId, delegate, bean, decorators, implementationClass);
    }

    public SubclassDecoratorApplyingInstantiator(String contextId, Instantiator<T> delegate, Bean<T> bean,
            List<Decorator<?>> decorators) {
        super(contextId, delegate, bean, decorators, Reflections.<Class<T>> cast(bean.getBeanClass()));
    }

    @Override
    protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint,
            BeanManagerImpl manager) {
        T outerDelegate = getOuterDelegate(instance, creationalContext, originalInjectionPoint, manager);
        registerOuterDecorator((ProxyObject) instance, outerDelegate);
        return instance;
    }
}
