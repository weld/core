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
package org.jboss.weld.util.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

/**
 * Delegating {@link BeanAttributes}.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class ForwardingBeanAttributes<T> implements BeanAttributes<T> {

    protected abstract BeanAttributes<T> attributes();

    @Override
    public Set<Type> getTypes() {
        return attributes().getTypes();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return attributes().getQualifiers();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return attributes().getScope();
    }

    @Override
    public String getName() {
        return attributes().getName();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return attributes().getStereotypes();
    }

    @Override
    public boolean isAlternative() {
        return attributes().isAlternative();
    }

    @Override
    public boolean isNullable() {
        return attributes().isNullable();
    }

    @Override
    public int hashCode() {
        return attributes().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return attributes().equals(obj);
    }

}
