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
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

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

    public SyntheticClassBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> factory,
            BeanManagerImpl manager) {
        super(attributes, manager, beanClass);
        this.producer = factory.createInjectionTarget(this);
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
    protected InjectionTarget<T> getProducer() {
        return producer;
    }

    @Override
    public String toString() {
        return "SyntheticClassBean [attributes=" + attributes() + ", injectionTarget=" + producer.getClass() + ", beanClass="
                + getBeanClass() + "]";
    }
}
