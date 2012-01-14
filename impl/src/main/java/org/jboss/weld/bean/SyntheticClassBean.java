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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Represents a class bean created based on extension-provided {@link InjectionTarget} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SyntheticClassBean<T> extends AbstractSyntheticBean<T> {

    protected final InjectionTarget<T> producer;

    public SyntheticClassBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> producer, BeanManagerImpl manager) {
        super(attributes, createId(attributes, beanClass, producer), manager, beanClass, producer);
        this.producer = producer;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        T instance = producer.produce(creationalContext);
        producer.inject(instance, creationalContext);
        producer.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        try {
            producer.preDestroy(instance);
            producer.dispose(instance);
        } finally {
            creationalContext.release();
        }
    }

    @Override
    public String toString() {
        return "SyntheticClassBean [attributes=" + attributes() + ", injectionTarget=" + producer + ", beanClass=" + getBeanClass() + "]";
    }
}
