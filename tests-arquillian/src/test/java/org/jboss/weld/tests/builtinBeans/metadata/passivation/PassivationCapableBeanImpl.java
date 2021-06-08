/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.builtinBeans.metadata.passivation;

import java.io.Serializable;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.util.bean.ForwardingBeanAttributes;

/**
 * An implementation of {@link Bean} which delegates to {@link InjectionTarget} and {@link BeanAttributes}. The bean is
 * passivation capable however the {@link PassivationCapableBeanImpl} class is intentionally not {@link Serializable} (it does
 * not have to).
 *
 * The container is required to wrap this non-serializable instance with a serialization proxy in order to make the bean a
 * passivation capable dependency.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class PassivationCapableBeanImpl<T> extends ForwardingBeanAttributes<T> implements Bean<T>, PassivationCapable {

    private final InjectionTarget<T> injectionTarget;
    private final Class<?> beanClass;
    private final BeanAttributes<T> attributes;

    public PassivationCapableBeanImpl(Class<?> beanClass, BeanAttributes<T> attributes, InjectionTargetFactory<T> factory) {
        this.beanClass = beanClass;
        this.attributes = attributes;
        this.injectionTarget = factory.createInjectionTarget(this);
    }

    @Override
    public T create(CreationalContext<T> ctx) {
        T instance = injectionTarget.produce(ctx);
        injectionTarget.inject(instance, ctx);
        injectionTarget.postConstruct(instance);
        return instance;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> ctx) {
        injectionTarget.dispose(instance);
        ctx.release();
    }

    @Override
    public String getId() {
        return beanClass.getCanonicalName();
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionTarget.getInjectionPoints();
    }

    @Override
    protected BeanAttributes<T> attributes() {
        return attributes;
    }
}
