/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.weld.bean.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.contexts.SerializableContextualInstanceImpl;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author Marius Bogoevici
 * @author Ales Justin
 */
public class DecorationHelper<T> {
    private static ThreadLocal<Stack<DecorationHelper<?>>> helperStackHolder = new ThreadLocal<Stack<DecorationHelper<?>>>() {
        @Override
        protected Stack<DecorationHelper<?>> initialValue() {
            return new Stack<DecorationHelper<?>>();
        }
    };

    private final Class<T> proxyClassForDecorator;

    private final TargetBeanInstance targetBeanInstance;

    private T originalInstance;

    private T previousDelegate;

    private int counter;

    private final BeanManagerImpl beanManager;
    private final ContextualStore contextualStore;
    private final Bean<?> bean;
    private final ProxyInstantiator instantiator;

    List<Decorator<?>> decorators;

    public DecorationHelper(TargetBeanInstance originalInstance, Bean<?> bean, Class<T> proxyClassForDecorator,
            BeanManagerImpl beanManager, ContextualStore contextualStore, List<Decorator<?>> decorators) {
        this.originalInstance = Reflections.<T> cast(originalInstance.getInstance());
        this.targetBeanInstance = originalInstance;
        this.beanManager = beanManager;
        this.contextualStore = contextualStore;
        this.decorators = new LinkedList<Decorator<?>>(decorators);
        this.proxyClassForDecorator = proxyClassForDecorator;
        this.bean = bean;
        this.instantiator = beanManager.getServices().get(ProxyInstantiator.class);
        counter = 0;
    }

    public static void push(DecorationHelper<?> helper) {
        helperStackHolder.get().push(helper);
    }

    public static DecorationHelper<?> peek() {
        return helperStackHolder.get().peek();
    }

    public static void pop() {
        final Stack<DecorationHelper<?>> stack = helperStackHolder.get();
        stack.pop();
        if (stack.isEmpty()) {
            helperStackHolder.remove();
        }
    }

    public T getNextDelegate(InjectionPoint injectionPoint, CreationalContext<?> creationalContext) {
        if (counter == decorators.size()) {
            previousDelegate = originalInstance;
            return originalInstance;
        } else {
            T proxy = createProxy(injectionPoint, creationalContext);
            previousDelegate = proxy;
            return proxy;
        }
    }

    private T createProxy(InjectionPoint injectionPoint, CreationalContext<?> creationalContext) {
        try {
            final T proxy = instantiator.newInstance(proxyClassForDecorator);
            TargetBeanInstance newTargetBeanInstance = new TargetBeanInstance(targetBeanInstance);
            Decorator<Object> decorator = Reflections.cast(decorators.get(counter++));
            DecoratorProxyMethodHandler methodHandler = createMethodHandler(injectionPoint, creationalContext, decorator);
            newTargetBeanInstance.setInterceptorsHandler(methodHandler);
            ProxyFactory.setBeanInstance(beanManager.getContextId(), proxy, newTargetBeanInstance, bean);
            return proxy;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new DefinitionException(BeanLogger.LOG.proxyInstantiationFailed(this), e.getCause());
        } catch (IllegalAccessException e) {
            throw new DefinitionException(BeanLogger.LOG.proxyInstantiationBeanAccessFailed(this), e.getCause());
        }
    }

    public DecoratorProxyMethodHandler createMethodHandler(InjectionPoint injectionPoint,
            CreationalContext<?> creationalContext, Decorator<Object> decorator) {
        Object decoratorInstance = beanManager.getInjectableReference(injectionPoint, decorator, creationalContext);
        assert previousDelegate != null
                : "previousDelegate should have been set when calling beanManager.getReference(), but it wasn't!";
        SerializableContextualInstanceImpl<Decorator<Object>, Object> serializableContextualInstance = new SerializableContextualInstanceImpl<Decorator<Object>, Object>(
                decorator, decoratorInstance, null, contextualStore);
        return new DecoratorProxyMethodHandler(serializableContextualInstance, previousDelegate);
    }

}
