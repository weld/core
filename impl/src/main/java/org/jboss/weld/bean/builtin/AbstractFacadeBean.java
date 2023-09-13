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
package org.jboss.weld.bean.builtin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.injection.EmptyInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * The built-in bean for facade objects. Since special rules are applied for resolving facade beans, we need to resolve
 * decorators per bean instance based on the required bean type.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class AbstractFacadeBean<T> extends AbstractDecorableBuiltInBean<T> {

    private Class<T> proxyClass;

    protected AbstractFacadeBean(BeanManagerImpl manager, Class<T> type) {
        super(manager, type);
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        super.destroy(instance, creationalContext);
        creationalContext.release();
    }

    @Override
    protected Class<T> getProxyClass() {
        return proxyClass;
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        this.proxyClass = new ProxyFactory<T>(getBeanManager().getContextId(), getType(), getTypes(), this).getProxyClass();
    }

    @Override
    protected List<Decorator<?>> getDecorators(InjectionPoint ip) {
        return beanManager.resolveDecorators(Collections.singleton(ip.getType()), getQualifiers());
    }

    @Override
    protected InjectionPoint getInjectionPoint(CurrentInjectionPoint cip) {
        InjectionPoint ip = super.getInjectionPoint(cip);
        if (ip == null) {
            ip = new DynamicLookupInjectionPoint(EmptyInjectionPoint.INSTANCE, getDefaultType(),
                    Collections.<Annotation> emptySet());
        }
        return ip;
    }

    protected abstract Type getDefaultType();
}
