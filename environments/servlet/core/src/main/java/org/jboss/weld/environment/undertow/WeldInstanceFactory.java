/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.undertow;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Unmanaged;
import jakarta.enterprise.inject.spi.Unmanaged.UnmanagedInstance;
import jakarta.servlet.ServletContext;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
import org.jboss.weld.util.reflection.Reflections;

import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;

/**
 * {@link InstanceFactory} implementation that uses CDI's {@link Unmanaged} internally to obtain Weld-managed instances.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
class WeldInstanceFactory<T> implements InstanceFactory<T> {

    static <T> WeldInstanceFactory<T> of(InstanceFactory<T> delegate, ServletContext context, Class<?> clazz) {
        return new WeldInstanceFactory<T>(delegate, context, Reflections.<Class<T>> cast(clazz));
    }

    private final InstanceFactory<T> delegate;
    private final ServletContext context;
    private final Class<T> clazz;

    private WeldInstanceFactory(InstanceFactory<T> delegate, ServletContext context, Class<T> clazz) {
        this.delegate = delegate;
        this.context = context;
        this.clazz = clazz;
    }

    @Override
    public InstanceHandle<T> createInstance() throws InstantiationException {
        Object manager = context.getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
        if (manager instanceof BeanManager) {
            UnmanagedInstance<T> instance = new Unmanaged<T>(BeanManagerProxy.unwrap((BeanManager) manager), clazz)
                    .newInstance();
            instance.produce().inject().postConstruct();
            return new WeldInstanceHandle<T>(instance);
        } else {
            // fallback
            return delegate.createInstance();
        }
    }

    @Override
    public String toString() {
        return "WeldInstanceFactory [clazz=" + clazz + "]";
    }

    private static class WeldInstanceHandle<T> implements InstanceHandle<T> {

        private final UnmanagedInstance<T> instance;

        WeldInstanceHandle(UnmanagedInstance<T> instance) {
            this.instance = instance;
        }

        @Override
        public T getInstance() {
            return instance.get();
        }

        @Override
        public void release() {
            instance.preDestroy();
            instance.dispose();
        }

        @Override
        public String toString() {
            return "WeldInstanceHandle [instance=" + instance + "]";
        }
    }
}
