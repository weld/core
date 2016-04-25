/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events.builder;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.builder.BeanConfigurator;

import org.jboss.weld.bean.BeanIdentifiers;
import org.jboss.weld.bootstrap.events.builder.BeanConfiguratorImpl.CreateCallback;
import org.jboss.weld.bootstrap.events.builder.BeanConfiguratorImpl.DestroyCallback;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.bean.ForwardingBeanAttributes;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class BeanBuilderImpl<T> {
    // implements BeanBuilder<T> {

    private final BeanConfiguratorImpl<T> configurator;

    /**
     *
     * @param configurator
     */
    public BeanBuilderImpl(BeanConfiguratorImpl<T> configurator) {
        this.configurator = configurator;
    }

    // @Override
    public BeanConfigurator<T> configure() {
        return configurator;
    }

    // @Override
    public Bean<T> build() {
        // TODO validate?
        return new ImmutableBean<>(configurator);
    }

    public BeanManagerImpl getBeanManager() {
        return configurator.getBeanManager();
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T> the class of the bean instance
     */
    static class ImmutableBean<T> extends ForwardingBeanAttributes<T> implements Bean<T>, PassivationCapable {

        private final String id;

        private final BeanManagerImpl beanManager;

        private final Class<?> beanClass;

        private final BeanAttributes<T> attributes;

        private final Set<InjectionPoint> injectionPoints;

        private final CreateCallback<T> createCallback;

        private final DestroyCallback<T> destroyCallback;

        /**
         *
         * @param configurator
         */
        ImmutableBean(BeanConfiguratorImpl<T> configurator) {
            this.beanManager = configurator.getBeanManager();
            this.beanClass = configurator.getBeanClass();
            this.attributes = new BeanAttributesBuilderImpl<>(configurator.getAttributes()).build();
            this.injectionPoints = ImmutableSet.copyOf(configurator.getInjectionPoints());
            this.createCallback = configurator.getCreateCallback();
            this.destroyCallback = configurator.getDestroyCallback();
            if (configurator.getId() != null) {
                this.id = configurator.getId();
            } else {
                this.id = BeanIdentifiers.forBuilderBean(attributes, beanClass);
            }
        }

        @Override
        public T create(CreationalContext<T> creationalContext) {
            return createCallback.create(creationalContext, beanManager);
        }

        @Override
        public void destroy(T instance, CreationalContext<T> creationalContext) {
            if (destroyCallback != null) {
                destroyCallback.destroy(instance, creationalContext);
            }
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

        @Override
        public boolean isNullable() {
            return false;
        }

        @Override
        protected BeanAttributes<T> attributes() {
            return attributes;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Immutable Builder Bean [" + getBeanClass().toString() + ", types: " + Formats.formatTypes(getTypes()) + ", qualifiers: "
                    + Formats.formatAnnotations(getQualifiers()) + "]";
        }

    }

}
