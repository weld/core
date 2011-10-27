/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.interceptors.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.util.reflection.HierarchyDiscovery;

/**
 * Utility class for extension-provided interceptor tests.
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 *
 */
public abstract class AbstractInterceptor<T> implements Interceptor<T> {

    public Set<Type> getTypes() {
        return new HierarchyDiscovery(getBeanClass()).getTypeClosure();
    }

    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    public String getName() {
        return null;
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    public boolean isAlternative() {
        return false;
    }

    public boolean isNullable() {
        return false;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public T create(CreationalContext<T> creationalContext) {
        try {
            return (T) getBeanClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating an instance of " + getBeanClass());
        }
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        creationalContext.release();
    }
}
