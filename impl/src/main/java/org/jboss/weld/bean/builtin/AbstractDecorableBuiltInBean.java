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

import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.DecorableBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Decorators;

/**
 * Built-in bean that can be decorated by a {@link Decorator}
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractDecorableBuiltInBean<T> extends AbstractBuiltInBean<T> implements DecorableBean<T> {

    private final CurrentInjectionPoint cip;

    protected AbstractDecorableBuiltInBean(BeanManagerImpl beanManager, Class<T> type) {
        super(new StringBeanIdentifier(BeanIdentifiers.forBuiltInBean(beanManager, type, null)), beanManager, type);
        this.cip = beanManager.getServices().get(CurrentInjectionPoint.class);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        InjectionPoint ip = getInjectionPoint(cip);
        List<Decorator<?>> decorators = getDecorators(ip);
        T instance = newInstance(ip, creationalContext);
        if (decorators == null) {
            decorators = beanManager.resolveDecorators(Collections.singleton(ip.getType()), getQualifiers());
        }
        if (decorators.isEmpty()) {
            return instance;
        }
        return Decorators.getOuterDelegate(this, instance, creationalContext, getProxyClass(), cip.peek(), getBeanManager(),
                decorators);
    }

    protected abstract T newInstance(InjectionPoint ip, CreationalContext<T> creationalContext);

    protected abstract List<Decorator<?>> getDecorators(InjectionPoint ip);

    protected abstract Class<T> getProxyClass();

    protected InjectionPoint getInjectionPoint(CurrentInjectionPoint cip) {
        InjectionPoint ip = cip.peek();
        return EmptyInjectionPoint.INSTANCE.equals(ip) ? null : ip;
    }

    @Override
    public Class<?> getBeanClass() {
        return getClass();
    }

    @Override
    public List<Decorator<?>> getDecorators() {
        return beanManager.resolveDecorators(getTypes(), getQualifiers());
    }
}
