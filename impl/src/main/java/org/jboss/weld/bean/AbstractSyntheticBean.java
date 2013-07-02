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

import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;

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

    protected AbstractSyntheticBean(BeanAttributes<T> attributes, BeanManagerImpl manager, Class<?> beanClass) {
        super(attributes, new StringBeanIdentifier(BeanIdentifiers.forSyntheticBean(attributes, beanClass)));
        this.beanClass = beanClass;
    }

    @Override
    public Class<?> getBeanClass() {
        return beanClass;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return getProducer().getInjectionPoints();
    }

    protected abstract Producer<T> getProducer();
}
