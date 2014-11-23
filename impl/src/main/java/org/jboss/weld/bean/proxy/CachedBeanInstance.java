/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.context.cache.RequestScopedCache;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * BeanInstance wrapper that uses {@link RequestScopedCache} to cache bean instances. This wrapper can only be used on beans with known scoped.
 *
 * @author Stuart Douglas
 * @author Jozef Hartinger
 *
 */
public class CachedBeanInstance implements BeanInstance, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Set<Class<? extends Annotation>> CACHEABLE_SCOPES = ImmutableSet.of(RequestScoped.class, ConversationScoped.class, SessionScoped.class, ApplicationScoped.class);

    public static BeanInstance wrapIfCacheable(Bean<?> bean, BeanInstance beanInstance) {
        if (!CACHEABLE_SCOPES.contains(bean.getScope())) {
            return beanInstance;
        }
        return new CachedBeanInstance(beanInstance);
    }

    private final BeanInstance delegate;
    private final transient ThreadLocal<Object> instanceCache;

    private CachedBeanInstance(BeanInstance delegate) {
        this.delegate = delegate;
        this.instanceCache = new ThreadLocal<Object>();
    }

    @Override
    public Object getInstance() {
        Object instance = instanceCache.get();
        if (instance == null) {
            instance = delegate.getInstance();
            if (RequestScopedCache.addItemIfActive(instanceCache)) {
                instanceCache.set(instance);
            }
        }
        return instance;
    }

    @Override
    public Class<?> getInstanceType() {
        return delegate.getInstanceType();
    }

    @Override
    public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
        return delegate.invoke(instance, method, arguments);
    }

    private Object readResolve() throws ObjectStreamException {
        return new CachedBeanInstance(delegate);
    }
}
