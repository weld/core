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

import static org.jboss.weld.logging.messages.BeanMessage.NO_DELEGATE_FOR_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.TOO_MANY_DELEGATES_FOR_DECORATOR;
import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.manager.BeanManagerImpl;
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

    public SyntheticDecorator(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTarget<T> producer, BeanManagerImpl manager) {
        super(attributes, beanClass, producer, manager);
        this.delegate = identifyDelegateInjectionPoint(getInjectionPoints());
        this.decoratedTypes = immutableSet((getDecoratedTypes(attributes.getTypes())));
    }

    protected InjectionPoint identifyDelegateInjectionPoint(Set<InjectionPoint> injectionPoints) {
        InjectionPoint delegate = null;
        for (InjectionPoint injectionPoint : injectionPoints) {
            if (injectionPoint.isDelegate()) {
                if (delegate != null) {
                    throw new DefinitionException(TOO_MANY_DELEGATES_FOR_DECORATOR, getBeanClass());
                }
                delegate = injectionPoint;
            }
        }
        if (delegate == null) {
            throw new DefinitionException(NO_DELEGATE_FOR_DECORATOR, getBeanClass());
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
