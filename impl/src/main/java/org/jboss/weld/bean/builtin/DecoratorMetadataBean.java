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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.bean.ForwardingDecorator;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.BeanHolder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Allows a decorator to obtain information about itself.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 *
 */
public class DecoratorMetadataBean extends AbstractBuiltInMetadataBean<Decorator<?>> {

    public DecoratorMetadataBean(BeanManagerImpl beanManager) {
        super(Decorator.class.getSimpleName(), Reflections.<Class<Decorator<?>>>cast(Decorator.class), beanManager);
    }

    @Override
    protected Decorator<?> newInstance(InjectionPoint ip, CreationalContext<Decorator<?>> creationalContext) {
        Contextual<?> bean = getParentCreationalContext(creationalContext).getContextual();
        if (bean instanceof Decorator<?>) {
            return SerializableProxy.of((Decorator<?>) bean);
        }
        throw new IllegalArgumentException("Unable to inject " + bean + " into " + ip);
    }

    private static class SerializableProxy<T> extends ForwardingDecorator<T> implements Serializable {

        private static final long serialVersionUID = 398927939412634913L;

        public static <T> SerializableProxy<T> of(Bean<T> bean) {
            return new SerializableProxy<T>(bean);
        }

        private BeanHolder<T> holder;

        protected SerializableProxy(Bean<T> bean) {
            this.holder = new BeanHolder<T>(bean);
        }

        @Override
        protected Decorator<T> delegate() {
            return (Decorator<T>) holder.get();
        }
    }
}
