/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.bean;

import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;

import org.jboss.weld.bean.WrappedContextual;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Delegating implementation of {@link Bean}. Separate delegate is used for {@link BeanAttributes} methods, allowing this class
 * to be used for processing of extension-provided beans.
 *
 * @see ProcessBeanAttributes
 *
 * @author Jozef Hartinger
 *
 */
public abstract class IsolatedForwardingBean<T> extends ForwardingBeanAttributes<T> implements Bean<T>, WrappedContextual<T> {

    public abstract Bean<T> delegate();

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return delegate().create(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        delegate().destroy(instance, creationalContext);
    }

    @Override
    public Class<?> getBeanClass() {
        return delegate().getBeanClass();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate().getInjectionPoints();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IsolatedForwardingBean<?>) {
            return delegate().equals(Reflections.<IsolatedForwardingBean<?>>cast(obj).delegate());
        }
        return delegate().equals(obj);
    }

    @Override
    public String toString() {
        return "ForwardingBean wrapping bean " + delegate().toString() + " and attributes " + attributes();
    }

    public static class Impl<T> extends IsolatedForwardingBean<T> {
        private final WrappedBeanHolder<T, Bean<T>> cartridge;

        public Impl(WrappedBeanHolder<T, Bean<T>> cartridge) {
            this.cartridge = cartridge;
        }

        @Override
        public Bean<T> delegate() {
            return cartridge.getBean();
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return cartridge.getAttributes();
        }
    }
}
