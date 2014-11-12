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

    public abstract Decorator<T> delegate();

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
        public Decorator<T> delegate() {
            return cartridge.getBean();
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return cartridge.getAttributes();
        }
    }
}
