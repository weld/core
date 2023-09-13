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
package org.jboss.weld.bean.builtin.ee;

import java.util.concurrent.Callable;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.builtin.AbstractStaticallyDecorableBuiltInBean;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.manager.BeanManagerImpl;

public abstract class AbstractEEBean<T> extends AbstractStaticallyDecorableBuiltInBean<T> {

    private final T proxy;

    protected AbstractEEBean(Class<T> type, Callable<T> callable, BeanManagerImpl beanManager) {
        super(beanManager, type);
        this.proxy = new ProxyFactory<T>(beanManager.getContextId(), type, getTypes(), this)
                .create(new EnterpriseTargetBeanInstance(type, new CallableMethodHandler(callable)));
    }

    @Override
    protected T newInstance(InjectionPoint ip, CreationalContext<T> creationalContext) {
        return proxy;
    }
}
