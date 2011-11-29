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
package org.jboss.weld.tests.beanManager.beanAttributes;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.introspector.ForwardingAnnotatedType;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Allows annotations of a type to be overriden easily.
 * 
 * @author Jozef Hartinger
 * 
 * @param <T>
 */
public class WrappedAnnotatedType<T> extends ForwardingAnnotatedType<T> {

    private AnnotatedType<T> delegate;
    private Set<Annotation> annotations;

    public WrappedAnnotatedType(AnnotatedType<T> delegate, Annotation... annotations) {
        this.delegate = delegate;
        this.annotations = new HashSet<Annotation>(Arrays.asList(annotations));
    }

    public AnnotatedType<T> delegate() {
        return delegate;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(annotationType)) {
                return Reflections.cast(annotation);
            }
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return Collections.unmodifiableSet(annotations);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }
}
