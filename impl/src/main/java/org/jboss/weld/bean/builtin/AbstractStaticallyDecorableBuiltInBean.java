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
package org.jboss.weld.bean.builtin;

import java.util.List;

import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A decorated built-in bean for which, unlike {@link AbstractFacadeBean}, decorators only need to be resolved once.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class AbstractStaticallyDecorableBuiltInBean<T> extends AbstractDecorableBuiltInBean<T> {

    private List<Decorator<?>> decorators;
    private Class<T> proxyClass;

    protected AbstractStaticallyDecorableBuiltInBean(BeanManagerImpl beanManager, Class<T> type) {
        super(beanManager, type);
    }

    @Override
    protected List<Decorator<?>> getDecorators(InjectionPoint ip) {
        return decorators;
    }

    @Override
    protected Class<T> getProxyClass() {
        if (proxyClass == null) {
            // this should never happen
            throw new IllegalStateException(
                    "No decorators were resolved for this bean at boot time however there are some now");
        }
        return proxyClass;
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        this.decorators = beanManager.resolveDecorators(getTypes(), getQualifiers());
        if (!decorators.isEmpty()) {
            this.proxyClass = new ProxyFactory<T>(getBeanManager().getContextId(), getType(), getTypes(), this).getProxyClass();
        }
    }
}
