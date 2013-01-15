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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.injection.ForwardingInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

/**
 * Common supertype for {@link Beans} which are created based on extension-provided {@link Producer} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class AbstractSyntheticBean<T> extends CommonBean<T> {

    private final Class<?> beanClass;
    protected final Producer<T> producer;
    private final Set<InjectionPoint> injectionPoints;

    protected AbstractSyntheticBean(BeanAttributes<T> attributes, String id, BeanManagerImpl manager, Class<?> beanClass, Producer<T> producer) {
        super(attributes, id, manager);
        this.beanClass = beanClass;
        this.producer = producer;
        this.injectionPoints = wrapInjectionPoints(producer.getInjectionPoints());
    }

    protected static <T> String createId(BeanAttributes<T> attributes, Class<?> beanClass, Producer<T> producer) {
        return new StringBuilder().append(SyntheticClassBean.class.getName()).append(RIBean.BEAN_ID_SEPARATOR).append(beanClass.getName())
                .append(Beans.createBeanAttributesId(attributes)).toString();
    }

    /**
     * Wraps a set of injection points to reflect this bean within the {@link InjectionPoint#getBean()} method.
     */
    protected Set<InjectionPoint> wrapInjectionPoints(Set<InjectionPoint> injectionPoints) {
        Set<InjectionPoint> wrappedInjectionPoints = new HashSet<InjectionPoint>(injectionPoints.size());
        for (final InjectionPoint ip : injectionPoints) {
            wrappedInjectionPoints.add(new ForwardingInjectionPoint() {

                @Override
                public Bean<?> getBean() {
                    return AbstractSyntheticBean.this;
                }

                @Override
                protected InjectionPoint delegate() {
                    return ip;
                }
            });
        }
        return wrappedInjectionPoints;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    protected Producer<T> getProducer() {
        return producer;
    }
}
