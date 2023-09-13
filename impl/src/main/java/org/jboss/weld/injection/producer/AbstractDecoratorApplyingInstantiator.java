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

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * Common functionality for an instantiator capable of applying decorators to a given instance.
 *
 * @author Jozef Hartinger
 *
 * @see SubclassDecoratorApplyingInstantiator
 * @see ProxyDecoratorApplyingSessionBeanInstantiator
 */
public abstract class AbstractDecoratorApplyingInstantiator<T> extends ForwardingInstantiator<T> {

    private final Bean<T> bean;
    private final Class<T> proxyClass;
    private final List<Decorator<?>> decorators;

    public AbstractDecoratorApplyingInstantiator(String contextId, Instantiator<T> delegate, Bean<T> bean,
            List<Decorator<?>> decorators, Class<? extends T> implementationClass) {
        super(delegate);
        this.bean = bean;
        this.decorators = decorators;
        ProxyFactory<T> factory = new ProxyFactory<T>(contextId, implementationClass, bean.getTypes(), bean, true);
        // eagerly generate the proxy class
        this.proxyClass = factory.getProxyClass();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        InjectionPoint originalInjectionPoint = manager.getServices().get(CurrentInjectionPoint.class).peek();
        return applyDecorators(delegate().newInstance(ctx, manager), ctx, originalInjectionPoint, manager);
    }

    protected abstract T applyDecorators(T instance, CreationalContext<T> creationalContext,
            InjectionPoint originalInjectionPoint, BeanManagerImpl manager);

    protected T getOuterDelegate(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint,
            BeanManagerImpl manager) {
        TargetBeanInstance beanInstance = new TargetBeanInstance(bean, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, bean, proxyClass, manager,
                manager.getServices().get(ContextualStore.class), decorators);
        DecorationHelper.push(decorationHelper);
        try {
            final T outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
            if (outerDelegate == null) {
                throw new WeldException(BeanLogger.LOG.proxyInstantiationFailed(this));
            }
            return outerDelegate;
        } finally {
            DecorationHelper.pop();
        }
    }

    protected void registerOuterDecorator(ProxyObject instance, T outerDelegate) {
        CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) instance
                .weld_getHandler();
        wrapperMethodHandler.setOuterDecorator(outerDelegate);
    }

    public Bean<T> getBean() {
        return bean;
    }

    public Class<T> getProxyClass() {
        return proxyClass;
    }

    public List<Decorator<?>> getDecorators() {
        return decorators;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return true;
    }
}
