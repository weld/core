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
package org.jboss.weld.bean;

import jakarta.decorator.Decorator;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.enterprise.inject.spi.ProducerFactory;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Creates a container-provided implementation of the {@link Bean} interfaces based on given {@link BeanAttributes},
 * {@link Class} and {@link Producer} implementations.
 *
 * @author Jozef Hartinger
 *
 */
public class SyntheticBeanFactory {

    private SyntheticBeanFactory() {
    }

    public static <T> AbstractSyntheticBean<T> create(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> factory, BeanManagerImpl manager) {
        if (attributes.getStereotypes().contains(Decorator.class)) {
            return createDecorator(attributes, beanClass, factory, manager);
        } else {
            return createClassBean(attributes, beanClass, factory, manager);
        }
    }

    public static <T, X> AbstractSyntheticBean<T> create(BeanAttributes<T> attributes, Class<X> beanClass,
            ProducerFactory<X> factory, BeanManagerImpl manager) {
        return createProducerBean(attributes, beanClass, factory, manager);
    }

    private static <T> AbstractSyntheticBean<T> createClassBean(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> factory, BeanManagerImpl manager) {
        if (Reflections.isSerializable(beanClass)) {
            return new PassivationCapableSyntheticClassBean<T>(attributes, beanClass, factory, manager);
        } else {
            return new SyntheticClassBean<T>(attributes, beanClass, factory, manager);
        }
    }

    private static <T> AbstractSyntheticBean<T> createDecorator(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> factory, BeanManagerImpl manager) {
        if (Reflections.isSerializable(beanClass)) {
            return new PassivationCapableSyntheticDecorator<T>(attributes, beanClass, factory, manager);
        } else {
            return new SyntheticDecorator<T>(attributes, beanClass, factory, manager);
        }
    }

    private static <T, X> AbstractSyntheticBean<T> createProducerBean(BeanAttributes<T> attributes, Class<X> beanClass,
            ProducerFactory<X> factory, BeanManagerImpl manager) {
        return new SyntheticProducerBean<T, X>(attributes, beanClass, factory, manager);
    }

    private static class PassivationCapableSyntheticClassBean<T> extends SyntheticClassBean<T> implements PassivationCapable {
        protected PassivationCapableSyntheticClassBean(BeanAttributes<T> attributes, Class<T> beanClass,
                InjectionTargetFactory<T> factory, BeanManagerImpl manager) {
            super(attributes, beanClass, factory, manager);
        }
    }

    private static class PassivationCapableSyntheticDecorator<T> extends SyntheticDecorator<T> implements PassivationCapable {
        protected PassivationCapableSyntheticDecorator(BeanAttributes<T> attributes, Class<T> beanClass,
                InjectionTargetFactory<T> factory, BeanManagerImpl manager) {
            super(attributes, beanClass, factory, manager);
        }
    }
}
