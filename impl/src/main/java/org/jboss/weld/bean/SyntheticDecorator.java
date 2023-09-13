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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;

import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Represents a {@link Decorator} created based on extension-provided {@link InjectionTarget} implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class SyntheticDecorator<T> extends SyntheticClassBean<T> implements Decorator<T> {

    private final InjectionPoint delegate;
    private final Set<Type> decoratedTypes;

    public SyntheticDecorator(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> factory,
            BeanManagerImpl manager) {
        super(attributes, beanClass, factory, manager);
        this.delegate = identifyDelegateInjectionPoint(getInjectionPoints());
        this.decoratedTypes = ImmutableSet.copyOf(getDecoratedTypes(attributes.getTypes()));
    }

    protected InjectionPoint identifyDelegateInjectionPoint(Set<InjectionPoint> injectionPoints) {
        InjectionPoint delegate = null;
        for (InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.isDelegate()) {
                if (delegate != null) {
                    throw BeanLogger.LOG.tooManyDelegateInjectionPoints(getBeanClass());
                }
                delegate = injectionPoint;
            }
        }
        if (delegate == null) {
            throw BeanLogger.LOG.noDelegateInjectionPoint(getBeanClass());
        }
        return delegate;
    }

    protected Set<Type> getDecoratedTypes(Set<Type> types) {
        Set<Type> decoratedTypes = new HashSet<Type>();
        for (Type type : types) {
            Class<?> rawType = Reflections.getRawType(type);
            if (rawType.isInterface() && !Serializable.class.equals(rawType)) {
                decoratedTypes.add(type);
            }
        }
        return decoratedTypes;
    }

    @Override
    public Type getDelegateType() {
        return delegate.getType();
    }

    @Override
    public Set<Annotation> getDelegateQualifiers() {
        return delegate.getQualifiers();
    }

    @Override
    public Set<Type> getDecoratedTypes() {
        return decoratedTypes;
    }
}
