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
public abstract class AbstractSyntheticBean<T> extends CommonBean<T> implements Bean<T> {

    private final Class<T> beanClass;
    protected final Producer<T> producer;
    private final Set<InjectionPoint> injectionPoints;

    protected AbstractSyntheticBean(BeanAttributes<T> attributes, String id, BeanManagerImpl manager, Class<T> beanClass, Producer<T> producer) {
        super(attributes, id, manager);
        this.beanClass = beanClass;
        this.producer = producer;
        this.injectionPoints = wrapInjectionPoints(producer.getInjectionPoints());
    }

    protected static <T> String createId(BeanAttributes<T> attributes, Class<T> beanClass, Producer<T> producer) {
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
