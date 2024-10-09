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
package org.jboss.weld.annotated.enhanced;

import java.lang.annotation.Annotation;
import java.util.Collection;

import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;

/**
 * Represents a Class
 *
 * @author Pete Muir
 */
public interface EnhancedAnnotatedType<T> extends EnhancedAnnotated<T, Class<T>>, AnnotatedType<T> {

    /**
     * Gets all fields on the type
     *
     * @return A set of abstracted fields
     */
    Collection<EnhancedAnnotatedField<?, ? super T>> getEnhancedFields();

    /**
     * Gets all methods on the type including those declared on a superclass of {@link #getJavaClass()}. Overridden methods are
     * not returned.
     *
     * @return A set of abstracted methods
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods();

    /**
     * Gets all methods on the type
     *
     * @return A set of abstracted methods
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods();

    /**
     * Get a field by name
     *
     * @param <F> the expected type of the field
     * @param fieldName the field name
     * @return the field
     */
    <F> EnhancedAnnotatedField<F, ?> getDeclaredEnhancedField(String fieldName);

    /**
     * Gets all fields which are annotated with the given annotation type on this
     * class and all super classes
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedField<?, ?>> getEnhancedFields(Class<? extends Annotation> annotationType);

    /**
     * Gets all fields declared on this class only.
     *
     * @return A set of abstracted fields. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields();

    /**
     * Gets all fields which are annotated with the given annotation type on this
     * class only.
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedField<?, ? super T>> getDeclaredEnhancedFields(Class<? extends Annotation> annotationType);

    /**
     * Gets all constructors
     */
    Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors();

    /**
     * Gets all constructors which are annotated with annotationType
     *
     * @param annotationType The annotation type to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedConstructor<T>> getEnhancedConstructors(Class<? extends Annotation> annotationType);

    /**
     * Gets the no-args constructor
     *
     * @return The no-args constructor, or null if not defined
     */
    EnhancedAnnotatedConstructor<T> getNoArgsEnhancedConstructor();

    /**
     * Get the constructor which matches the argument list provided
     *
     * @param parameterTypes the parameters of the constructor
     * @return the matching constructor, or null if not defined
     */
    EnhancedAnnotatedConstructor<T> getDeclaredEnhancedConstructor(ConstructorSignature signature);

    /**
     * Gets all methods annotated with annotationType including those declared on a superclass of {@link #getJavaClass()}.
     * Overridden methods are not returned.
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethods(Class<? extends Annotation> annotationType);

    /**
     * Gets all methods annotated with annotationType
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethods(Class<? extends Annotation> annotationType);

    /**
     * Get a method by name
     *
     * @param <M> the expected return type
     * @param signature the name of the method
     * @return the method, or null if it doesn't exist
     */
    <M> EnhancedAnnotatedMethod<M, ?> getDeclaredEnhancedMethod(MethodSignature signature);

    /**
     * Get a method by name
     *
     * @param <M> the expected return type
     * @param signature the name of the method
     * @return the method, or null if it doesn't exist
     */
    <M> EnhancedAnnotatedMethod<M, ?> getEnhancedMethod(MethodSignature signature);

    /**
     * Gets declared with parameters annotated with annotationType
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getDeclaredEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType);

    /**
     * Gets all methods with parameters annotated with annotationType including those declared on a superclass of
     * {@link #getJavaClass()}. Overridden methods are not returned.
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<EnhancedAnnotatedMethod<?, ? super T>> getEnhancedMethodsWithAnnotatedParameters(
            Class<? extends Annotation> annotationType);

    /**
     * Gets the superclass.
     *
     * @return The abstracted superclass, null if there is no superclass
     */
    EnhancedAnnotatedType<? super T> getEnhancedSuperclass();

    boolean isParameterizedType();

    boolean isAbstract();

    boolean isEnum();

    boolean isMemberClass();

    boolean isLocalClass();

    boolean isAnonymousClass();

    /**
     * Indicates if this {@code EnhancedAnnotatedType} represents a sealed class/interface
     *
     * @return True if sealed, false otherwise
     */
    boolean isSealed();

    boolean isSerializable();

    boolean isDiscovered();

    <S> S cast(Object object);

    <U> EnhancedAnnotatedType<? extends U> asEnhancedSubclass(EnhancedAnnotatedType<U> clazz);

    /**
     * Check if this is equivalent to a java class
     *
     * @param clazz The Java class
     * @return true if equivalent
     */
    boolean isEquivalent(Class<?> clazz);

    String getSimpleName();

    /**
     * Gets all annotations which are declared on this annotated item with the
     * given meta annotation type
     *
     * @param The meta annotation to match
     * @return A set of matching meta-annotations. Returns an empty set if there
     *         are no matches.
     */
    Collection<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType);

    /**
     * Returns a lightweight implementation of {@link AnnotatedType} with minimal memory footprint.
     *
     * @return the slim version of this {@link AnnotatedType}
     */
    SlimAnnotatedType<T> slim();

}
