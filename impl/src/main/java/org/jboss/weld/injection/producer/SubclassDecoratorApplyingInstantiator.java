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

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.DecorationHelper;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bean.proxy.TargetBeanInstance;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.ContextualStore;

/**
 * A wrapper over {@link SubclassedComponentInstantiator} that registers decorators using the enhanced subclass. This is used
 * for enabling decorators on managed beans.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SubclassDecoratorApplyingInstantiator<T> extends AbstractDecoratorApplyingInstantiator<T> {

    public SubclassDecoratorApplyingInstantiator(Instantiator<T> delegate, Bean<T> bean, List<Decorator<?>> decorators) {
        super(delegate, bean, decorators);
    }

    @Override
    protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint, BeanManagerImpl manager) {
        TargetBeanInstance beanInstance = new TargetBeanInstance(getBean(), instance);
        DecorationHelper<T> decorationHelper = new DecorationHelper<T>(beanInstance, getBean(), getProxyClass(), manager, manager.getServices().get(ContextualStore.class), getDecorators());
        DecorationHelper.push(decorationHelper);
        final T outerDelegate;
        try {
            outerDelegate = decorationHelper.getNextDelegate(originalInjectionPoint, creationalContext);
        } finally {
            DecorationHelper.pop();
        }
        if (outerDelegate == null) {
            throw new WeldException(PROXY_INSTANTIATION_FAILED, this);
        }
        CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) ((ProxyObject) instance).getHandler();
        wrapperMethodHandler.setOuterDecorator(outerDelegate);
        return instance;
    }
}
