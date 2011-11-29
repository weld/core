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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.ProcessBeanAttributes;

/**
 * Delegating implementation of {@link Decorator}. Separate delegate is used for {@link BeanAttributes} methods, allowing this
 * class to be used for processing of extension-provided beans.
 *
 * @see ProcessBeanAttributes
 *
 * @author Jozef Hartinger
 *
 */
public abstract class IsolatedForwardingDecorator<T> extends IsolatedForwardingBean<T> implements Decorator<T> {

    protected abstract Decorator<T> delegate();

    @Override
    public Type getDelegateType() {
        return delegate().getDelegateType();
    }

    @Override
    public Set<Annotation> getDelegateQualifiers() {
        return delegate().getDelegateQualifiers();
    }

    @Override
    public Set<Type> getDecoratedTypes() {
        return delegate().getDecoratedTypes();
    }

    public static class Impl<T> extends IsolatedForwardingDecorator<T> {
        private final WrappedBeanHolder<T, Decorator<T>> cartridge;

        public Impl(WrappedBeanHolder<T, Decorator<T>> cartridge) {
            this.cartridge = cartridge;
        }

        @Override
        protected Decorator<T> delegate() {
            return cartridge.getBean();
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return cartridge.getAttributes();
        }
    }
}
