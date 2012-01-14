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
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Represents a producer field or producer method created based on extension-provided {@link Producer} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SyntheticProducerBean<T> extends AbstractSyntheticBean<T> {

    protected SyntheticProducerBean(BeanAttributes<T> attributes, Class<T> beanClass, Producer<T> producer, BeanManagerImpl manager) {
        super(attributes, createId(attributes, beanClass, producer), manager, beanClass, producer);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        return getProducer().produce(creationalContext);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        try {
            getProducer().dispose(instance);
        } finally {
            creationalContext.release();
        }
    }

}
