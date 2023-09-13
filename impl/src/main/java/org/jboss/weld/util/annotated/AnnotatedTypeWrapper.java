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
package org.jboss.weld.util.annotated;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

public class AnnotatedTypeWrapper<T> extends ForwardingAnnotatedType<T> {

    private final AnnotatedType<T> delegate;
    private final Set<Annotation> annotations;

    public AnnotatedTypeWrapper(AnnotatedType<T> delegate, Annotation... additionalAnnotations) {
        this(delegate, true, additionalAnnotations);
    }

    public AnnotatedTypeWrapper(AnnotatedType<T> delegate, boolean keepOriginalAnnotations,
            Annotation... additionalAnnotations) {
        this.delegate = delegate;
        ImmutableSet.Builder<Annotation> builder = ImmutableSet.<Annotation> builder();
        if (keepOriginalAnnotations) {
            builder.addAll(delegate.getAnnotations());
        }
        for (Annotation annotation : additionalAnnotations) {
            builder.add(annotation);
        }
        this.annotations = builder.build();
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
        return annotations;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

}
