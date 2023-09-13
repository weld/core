/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.extensions.supertypes.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

/**
 * Implements the CDI AnnotatedType interface.
 *
 * @author robc
 */
public class AnnotatedTypeImpl<T> extends AnnotatedImpl implements AnnotatedType<T> {

    private Set<AnnotatedConstructor<T>> constructors;
    private Set<AnnotatedField<? super T>> fields;
    private Class<T> javaClass;
    private Set<AnnotatedMethod<? super T>> methods;

    public AnnotatedTypeImpl(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Class<T> javaClass) {
        super(baseType, typeClosure, annotations);
        this.javaClass = javaClass;
    }

    public AnnotatedTypeImpl(AnnotatedType<T> type) {
        this(type.getBaseType(), type.getTypeClosure(), type.getAnnotations(), type.getJavaClass());
    }

    public Set<AnnotatedConstructor<T>> getConstructors() {
        return constructors;
    }

    public void setConstructors(Set<AnnotatedConstructor<T>> constructors) {
        this.constructors = constructors;
    }

    public Set<AnnotatedField<? super T>> getFields() {
        return fields;
    }

    public void setFields(Set<AnnotatedField<? super T>> fields) {
        this.fields = fields;
    }

    public Class<T> getJavaClass() {
        return javaClass;
    }

    public Set<AnnotatedMethod<? super T>> getMethods() {
        return methods;
    }

    public void setMethods(Set<AnnotatedMethod<? super T>> methods) {
        this.methods = methods;
    }
}
