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
package org.jboss.weld.introspector;

import javax.enterprise.inject.spi.AnnotatedType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Represents a Class
 *
 * @author Pete Muir
 */
public interface WeldClass<T> extends WeldAnnotated<T, Class<T>>, AnnotatedType<T> {

    /**
     * Gets all fields on the type
     *
     * @return A set of abstracted fields
     */
    Collection<WeldField<?, ? super T>> getWeldFields();

    /**
     * Gets all fields on the type
     *
     * @return A set of abstracted fields
     */
    Collection<WeldMethod<?, ? super T>> getWeldMethods();

    /**
     * Gets all fields on the type
     *
     * @return A set of abstracted fields
     */
    Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethods();

    /**
     * Get a field by name
     *
     * @param <F>       the expected type of the field
     * @param fieldName the field name
     * @return the field
     */
    <F> WeldField<F, ?> getDeclaredWeldField(String fieldName);

    /**
     * Gets all fields which are annotated with the given annotation type on this
     * class and all super classes
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldField<?, ?>> getWeldFields(Class<? extends Annotation> annotationType);

    /**
     * Gets all fields declared on this class only.
     *
     * @return A set of abstracted fields. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldField<?, ? super T>> getDeclaredWeldFields();

    /**
     * Gets all fields which are annotated with the given annotation type on this
     * class only.
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldField<?, ? super T>> getDeclaredWeldFields(Class<? extends Annotation> annotationType);

    /**
     * Gets all constructors
     */
    Collection<WeldConstructor<T>> getWeldConstructors();

    /**
     * Gets all constructors which are annotated with annotationType
     *
     * @param annotationType The annotation type to match
     * @return A set of abstracted fields with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldConstructor<T>> getWeldConstructors(Class<? extends Annotation> annotationType);

    /**
     * Gets the no-args constructor
     *
     * @return The no-args constructor, or null if not defined
     */
    WeldConstructor<T> getNoArgsWeldConstructor();

    /**
     * Get the constructor which matches the argument list provided
     *
     * @param parameterTypes the parameters of the constructor
     * @return the matching constructor, or null if not defined
     */
    WeldConstructor<T> getDeclaredWeldConstructor(ConstructorSignature signature);

    /**
     * Gets all methods annotated with annotationType
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldMethod<?, ?>> getWeldMethods(Class<? extends Annotation> annotationType);

    /**
     * Gets all methods annotated with annotationType
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethods(Class<? extends Annotation> annotationType);

    /**
     * Find the annotated method for a given methodDescriptor
     *
     * @param methodDescriptor
     * @return
     */
    // TODO replace with MethodSignature variant
    @Deprecated
    WeldMethod<?, ?> getWeldMethod(Method method);

    /**
     * Get a method by name
     *
     * @param <M>       the expected return type
     * @param signature the name of the method
     * @return the method, or null if it doesn't exist
     */
    <M> WeldMethod<M, ?> getDeclaredWeldMethod(MethodSignature signature);

    /**
     * Get a method by name
     *
     * @param <M>       the expected return type
     * @param signature the name of the method
     * @return the method, or null if it doesn't exist
     */
    <M> WeldMethod<M, ?> getWeldMethod(MethodSignature signature);

    // TODO Replace with MethodSignature variant
    @Deprecated
    WeldMethod<?, ?> getDeclaredWeldMethod(Method method);

    /**
     * Gets declared with parameters annotated with annotationType
     *
     * @param annotationType The annotation to match
     * @return A set of abstracted methods with the given annotation. Returns an
     *         empty set if there are no matches
     */
    Collection<WeldMethod<?, ? super T>> getDeclaredWeldMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType);

    /**
     * Gets the superclass.
     *
     * @return The abstracted superclass, null if there is no superclass
     */
    WeldClass<? super T> getWeldSuperclass();

    boolean isParameterizedType();

    boolean isAbstract();

    boolean isEnum();

    boolean isMemberClass();

    boolean isLocalClass();

    boolean isAnonymousClass();

    boolean isSerializable();

    boolean isDiscovered();

    <S> S cast(Object object);

    <U> WeldClass<? extends U> asWeldSubclass(WeldClass<U> clazz);

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

}
