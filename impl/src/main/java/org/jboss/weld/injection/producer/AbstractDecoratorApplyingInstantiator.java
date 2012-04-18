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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.producer.ejb.ProxyDecoratorApplyingSessionBeanInstantiator;
import org.jboss.weld.logging.messages.BeanMessage;
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
public abstract class AbstractDecoratorApplyingInstantiator<T> implements Instantiator<T> {

    private final Instantiator<T> delegate;
    private final Bean<T> bean;
    private final Class<T> proxyClass;
    private final List<Decorator<?>> decorators;

    public AbstractDecoratorApplyingInstantiator(Instantiator<T> delegate, Bean<T> bean, List<Decorator<?>> decorators) {
        this.delegate = delegate;
        this.bean = bean;
        this.decorators = decorators;
        ProxyFactory<T> factory = new ProxyFactory<T>(bean.getBeanClass(), bean.getTypes(), bean);
        // eagerly generate the proxy class
        this.proxyClass = factory.getProxyClass();
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        InjectionPoint originalInjectionPoint = manager.getServices().get(CurrentInjectionPoint.class).peek();
        return applyDecorators(delegate.newInstance(ctx, manager), ctx, originalInjectionPoint, manager);
    }

    protected abstract T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint, BeanManagerImpl manager);

    protected T getOuterDelegate(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint, BeanManagerImpl manager) {
        TargetBeanInstance beanInstance = new TargetBeanInstance(bean, instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, bean, proxyClass, manager, manager.getServices().get(ContextualStore.class), decorators);
        DecorationHelper.push(decorationHelper);
        final T outerDelegate;
        try {
            outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
        } finally {
            DecorationHelper.pop();
        }
        if (outerDelegate == null) {
            throw new WeldException(BeanMessage.PROXY_INSTANTIATION_FAILED, this);
        }
        return outerDelegate;
    }

    protected void registerOuterDecorator(ProxyObject instance, T outerDelegate) {
        CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) instance.getHandler();
        wrapperMethodHandler.setOuterDecorator(outerDelegate);
    }

    public Instantiator<T> getDelegate() {
        return delegate;
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
    public boolean hasInterceptorSupport() {
        return delegate.hasInterceptorSupport();
    }

    @Override
    public boolean hasDecoratorSupport() {
        return true;
    }
}
