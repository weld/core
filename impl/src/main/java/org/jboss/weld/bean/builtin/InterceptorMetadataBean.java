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
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.ForwardingInterceptor;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.BeanHolder;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Allows an interceptor to obtain information about itself.
 *
 * @author Jozef Hartinger
 * @see CDI-92
 *
 */
public class InterceptorMetadataBean extends AbstractBuiltInMetadataBean<Interceptor<?>> {

    public InterceptorMetadataBean(BeanManagerImpl beanManager) {
        super(Interceptor.class.getSimpleName(), Reflections.<Class<Interceptor<?>>> cast(Interceptor.class), beanManager);
    }

    @Override
    protected Interceptor<?> newInstance(InjectionPoint ip, CreationalContext<Interceptor<?>> creationalContext) {
        Contextual<?> bean = getParentCreationalContext(creationalContext).getContextual();
        if (bean instanceof Interceptor<?>) {
            return SerializableProxy.of((Interceptor<?>) bean);
        }
        throw new IllegalArgumentException("Unable to inject " + bean + " into " + ip);
    }

    private static class SerializableProxy<T> extends ForwardingInterceptor<T> implements Serializable {

        private static final long serialVersionUID = 8482112157695944011L;

        public static <T> SerializableProxy<T> of(Bean<T> bean) {
            return new SerializableProxy<T>(bean);
        }

        private BeanHolder<T> holder;

        protected SerializableProxy(Bean<T> bean) {
            this.holder = new BeanHolder<T>(bean);
        }

        @Override
        protected Interceptor<T> delegate() {
            return (Interceptor<T>) holder.get();
        }
    }
}
