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
package org.jboss.weld.bean;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.inject.spi.Producer;

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

    public static <T> AbstractSyntheticBean<T> create(BeanAttributes<?> attrs, Class<?> clazz, Producer<?> producer, BeanManagerImpl manager) {
        BeanAttributes<T> attributes = Reflections.cast(attrs);
        Class<T> beanClass = Reflections.cast(clazz);

        if (producer instanceof InjectionTarget<?>) {
            if (attributes.getStereotypes().contains(Decorator.class)) {
                return createDecorator(attributes, beanClass, Reflections.<InjectionTarget<T>> cast(producer), manager);
            } else {
                return createClassBean(attributes, beanClass, Reflections.<InjectionTarget<T>> cast(producer), manager);
            }
        } else {
            return createProducerBean(attributes, beanClass, Reflections.<Producer<T>> cast(producer), manager);
        }
    }

    private static <T> AbstractSyntheticBean<T> createClassBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> injectionTarget, BeanManagerImpl manager) {
        if (Reflections.isSerializable(beanClass)) {
            return new PassivationCapableSyntheticClassBean<T>(attributes, beanClass, injectionTarget, manager);
        } else {
            return new SyntheticClassBean<T>(attributes, beanClass, injectionTarget, manager);
        }
    }

    private static <T> AbstractSyntheticBean<T> createDecorator(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> injectionTarget, BeanManagerImpl manager) {
        if (Reflections.isSerializable(beanClass)) {
            return new PassivationCapableSyntheticDecorator<T>(attributes, beanClass, injectionTarget, manager);
        } else {
            return new SyntheticDecorator<T>(attributes, beanClass, injectionTarget, manager);
        }
    }

    private static <T> AbstractSyntheticBean<T> createProducerBean(BeanAttributes<T> attributes, Class<T> beanClass, Producer<T> producer, BeanManagerImpl manager) {
        return new SyntheticProducerBean<T>(attributes, beanClass, producer, manager);
    }

    private static class PassivationCapableSyntheticClassBean<T> extends SyntheticClassBean<T> implements PassivationCapable {
        protected PassivationCapableSyntheticClassBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> injectionTarget, BeanManagerImpl manager) {
            super(attributes, beanClass, injectionTarget, manager);
        }
    }

    private static class PassivationCapableSyntheticDecorator<T> extends SyntheticDecorator<T> implements PassivationCapable {
        protected PassivationCapableSyntheticDecorator(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> producer, BeanManagerImpl manager) {
            super(attributes, beanClass, producer, manager);
        }
    }
}
