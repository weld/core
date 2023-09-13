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
package org.jboss.weld.bean.builtin;

import java.io.Serializable;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bean.ForwardingInterceptor;
import org.jboss.weld.bean.StringBeanIdentifier;
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
        super(new StringBeanIdentifier(BeanIdentifiers.forBuiltInBean(beanManager, Interceptor.class, null)),
                Reflections.<Class<Interceptor<?>>> cast(Interceptor.class), beanManager);
    }

    @Override
    protected Interceptor<?> newInstance(InjectionPoint ip, CreationalContext<Interceptor<?>> creationalContext) {
        Contextual<?> bean = getParentCreationalContext(creationalContext).getContextual();
        if (bean instanceof Interceptor<?>) {
            return SerializableProxy.of(getBeanManager().getContextId(), (Interceptor<?>) bean);
        }
        throw new IllegalArgumentException("Unable to inject " + bean + " into " + ip);
    }

    private static class SerializableProxy<T> extends ForwardingInterceptor<T> implements Serializable {

        private static final long serialVersionUID = 8482112157695944011L;

        public static <T> SerializableProxy<T> of(String contextId, Bean<T> bean) {
            return new SerializableProxy<T>(contextId, bean);
        }

        private BeanHolder<T> holder;

        protected SerializableProxy(String contextId, Bean<T> bean) {
            this.holder = new BeanHolder<T>(contextId, bean);
        }

        @Override
        public Interceptor<T> delegate() {
            return (Interceptor<T>) holder.get();
        }
    }
}
