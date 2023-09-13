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
package org.jboss.weld.tests.extensions.lifecycle.processSyntheticAnnotatedType;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;
import org.jboss.weld.util.reflection.Reflections;

public class AnnotatedTypeWrapper<T> extends ForwardingAnnotatedType<T> {

    private AnnotatedType<T> delegate;
    private Set<Annotation> annotations;

    public AnnotatedTypeWrapper(AnnotatedType<T> delegate, Annotation... annotations) {
        this.delegate = delegate;
        this.annotations = new HashSet<Annotation>(delegate.getAnnotations());
        Collections.addAll(this.annotations, annotations);
    }

    @Override
    public AnnotatedType<T> delegate() {
        return delegate;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
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
