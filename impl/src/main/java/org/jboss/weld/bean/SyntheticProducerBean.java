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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.enterprise.inject.spi.ProducerFactory;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Represents a producer field or producer method created based on extension-provided {@link Producer} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SyntheticProducerBean<T, X> extends AbstractSyntheticBean<T> {

    private final Producer<T> producer;

    protected SyntheticProducerBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> factory,
            BeanManagerImpl manager) {
        super(attributes, manager, beanClass);
        this.producer = factory.createProducer(this);
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

    @Override
    protected Producer<T> getProducer() {
        return producer;
    }
}
