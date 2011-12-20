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
package org.jboss.weld.injection.attributes;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.decorator.Delegate;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.introspector.WeldAnnotated;
import org.jboss.weld.serialization.BeanHolder;
import org.jboss.weld.util.reflection.Reflections;

public abstract class AbstractInferingInjectionPointAttributes<T, S> implements WeldInjectionPointAttributes<T, S>, Serializable {

    private static final long serialVersionUID = 7820718127728549436L;

    private final BeanHolder<?> bean;

    public AbstractInferingInjectionPointAttributes(Bean<?> bean) {
        this.bean = BeanHolder.of(bean);
    }

    @Override
    public Type getType() {
        return getAnnotated().getBaseType();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return getAnnotated().getQualifiers();
    }

    @Override
    public Bean<?> getBean() {
        return bean.get();
    }

    @Override
    public boolean isDelegate() {
        return getAnnotated().isAnnotationPresent(Delegate.class);
    }

    @Override
    public boolean isTransient() {
        return Reflections.isTransient(getMember());
    }

    @Override
    public abstract Member getMember();

    @Override
    public abstract WeldAnnotated<T, S> getAnnotated();

    @Override
    public String toString() {
        return getAnnotated().toString();
    }

    @Override
    public <X extends Annotation> X getQualifier(Class<X> annotationType) {
        X qualifier = getAnnotated().getAnnotation(annotationType);
        if (getQualifiers().contains(qualifier)) {
            return qualifier;
        } else {
            return null;
        }
    }
}
