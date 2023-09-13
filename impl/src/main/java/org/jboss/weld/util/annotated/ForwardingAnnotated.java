/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;

/**
 * Base class to allow implementation of the decorator pattern
 *
 * @param <T> the base type
 * @param <S> the annotated element type
 * @author Pete Muir
 */
public abstract class ForwardingAnnotated implements Annotated {

    protected abstract Annotated delegate();

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return delegate().getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return delegate().getAnnotations();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return delegate().isAnnotationPresent(annotationType);
    }

    public Type getBaseType() {
        return delegate().getBaseType();
    }

    public Set<Type> getTypeClosure() {
        return delegate().getTypeClosure();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate().equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

}
