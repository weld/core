/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.alternative.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

/**
 * {@link org.jboss.weld.util.annotated.ForwardingAnnotatedType} is not used intentionally to simulate extension-provided
 * implementation that we have no control of.
 *
 * @author Jozef Hartinger
 *
 * @param <X>
 */
public class ForwardingAnnotatedType<X> implements AnnotatedType<X> {

    private AnnotatedType<X> delegate;

    public ForwardingAnnotatedType(AnnotatedType<X> delegate) {
        this.delegate = delegate;
    }

    protected AnnotatedType<X> delegate() {
        return delegate;
    }

    public Type getBaseType() {
        return delegate().getBaseType();
    }

    public Set<Type> getTypeClosure() {
        return delegate().getTypeClosure();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return delegate().getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return delegate().getAnnotations();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return delegate().isAnnotationPresent(annotationType);
    }

    public Class<X> getJavaClass() {
        return delegate().getJavaClass();
    }

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return delegate().getConstructors();
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return delegate().getMethods();
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return delegate().getFields();
    }
}
