/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.util.bean;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

/**
 * Delegating implementation of {@link Bean}. Separate delegate is used for {@link BeanAttributes} methods, allowing this class
 * to be used for processing of extension-provided beans.
 *
 * @see ProcessBeanAttributes
 *
 * @author Jozef Hartinger
 *
 */
public abstract class IsolatedForwardingBean<T> extends ForwardingBeanAttributes<T> implements Bean<T> {

    protected abstract Bean<T> delegate();

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
        protected Bean<T> delegate() {
            return cartridge.getBean();
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return cartridge.getAttributes();
        }
    }
}
