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
package org.jboss.weld.test.util.annotated;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;

public abstract class ForwardingWeldClass<T> extends ForwardingWeldAnnotated<T, Class<T>> implements EnhancedAnnotatedType<T> {

    @Override
    protected abstract EnhancedAnnotatedType<T> delegate();

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods() {
        return delegate().getDeclaredEnhancedMethods();
    }

    public <M> EnhancedAnnotatedMethod<M, ?> getDeclaredEnhancedMethod(MethodSignature signature) {
        return delegate().getDeclaredEnhancedMethod(signature);
    }

    public Set<AnnotatedConstructor<T>> getConstructors() {
        return delegate().getConstructors();
    }

    public Set<AnnotatedMethod<? super T>> getMethods() {
        return delegate().getMethods();
    }

    public Set<AnnotatedField<? super T>> getFields() {
        return delegate().getFields();
    }

    public Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors(Class<? extends Annotation> annotationType) {
        return delegate().getEnhancedConstructors(annotationType);
    }

    public Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors() {
        return delegate().getEnhancedConstructors();
    }

    public Collection<EnhancedAnnotatedField<?, ?>> getEnhancedFields(Class<? extends Annotation> annotationType) {
        return delegate().getEnhancedFields(annotationType);
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods(Class<? extends Annotation> annotationType) {
        return delegate().getEnhancedMethods(annotationType);
    }

    public EnhancedAnnotatedConstructor<T> getNoArgsEnhancedConstructor() {
        return delegate().getNoArgsEnhancedConstructor();
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods() {
        return delegate().getEnhancedMethods();
    }

    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields(
            Class<? extends Annotation> annotationType) {
        return delegate().getDeclaredEnhancedFields(annotationType);
    }

    public Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields() {
        return delegate().getDeclaredEnhancedFields();
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods(
            Class<? extends Annotation> annotationType) {
        return delegate().getDeclaredEnhancedMethods(annotationType);
    }

    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType) {
        return delegate().getDeclaredEnhancedMethodsWithAnnotatedParameters(annotationType);
    }

    public Collection<EnhancedAnnotatedField<?, ? super T>> getEnhancedFields() {
        return delegate().getEnhancedFields();
    }

    public <M> EnhancedAnnotatedMethod<M, ?> getEnhancedMethod(MethodSignature signature) {
        return delegate().getEnhancedMethod(signature);
    }

    public EnhancedAnnotatedType<? super T> getEnhancedSuperclass() {
        return delegate().getEnhancedSuperclass();
    }

    public boolean isLocalClass() {
        return delegate().isLocalClass();
    }

    public boolean isMemberClass() {
        return delegate().isMemberClass();
    }

    public boolean isAnonymousClass() {
        return delegate().isAnonymousClass();
    }

    public boolean isSealed() {
        return delegate().isSealed();
    }

    @Override
    public boolean isParameterizedType() {
        return delegate().isParameterizedType();
    }

    public boolean isAbstract() {
        return delegate().isAbstract();
    }

    public boolean isEnum() {
        return delegate().isEnum();
    }

    public boolean isSerializable() {
        return delegate().isSerializable();
    }

    public <F> EnhancedAnnotatedField<F, ?> getDeclaredEnhancedField(String fieldName) {
        return delegate().getDeclaredEnhancedField(fieldName);
    }

    public <M> EnhancedAnnotatedMethod<M, ?> getDeclaredEnhancedMethod(MethodSignature signature,
            EnhancedAnnotatedType<M> expectedReturnType) {
        return delegate().getDeclaredEnhancedMethod(signature);
    }

    public EnhancedAnnotatedConstructor<T> getDeclaredEnhancedConstructor(ConstructorSignature signature) {
        return delegate().getDeclaredEnhancedConstructor(signature);
    }

    public <U> EnhancedAnnotatedType<? extends U> asEnhancedSubclass(EnhancedAnnotatedType<U> clazz) {
        return delegate().asEnhancedSubclass(clazz);
    }

    public <S> S cast(Object object) {
        return delegate().<S> cast(object);
    }

    public boolean isEquivalent(Class<?> clazz) {
        return delegate().isEquivalent(clazz);
    }

    public String getSimpleName() {
        return delegate().getSimpleName();
    }

    public Collection<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType) {
        return delegate().getDeclaredMetaAnnotations(metaAnnotationType);
    }

    public boolean isDiscovered() {
        return delegate().isDiscovered();
    }

    @Override
    public SlimAnnotatedType<T> slim() {
        return delegate().slim();
    }

    @Override
    public Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType) {
        return delegate().getEnhancedMethodsWithAnnotatedParameters(annotationType);
    }
}
