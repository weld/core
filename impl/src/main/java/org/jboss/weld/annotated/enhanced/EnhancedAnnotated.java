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
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.inject.Qualifier;
import jakarta.inject.Scope;
import jakarta.interceptor.InterceptorBinding;

import org.jboss.weld.util.collections.Arrays2;

/**
 * AnnotatedItem provides a uniform access to the annotations on an annotated
 * item defined either in Java or XML
 *
 * @author Pete Muir
 */

public interface EnhancedAnnotated<T, S> extends Annotated {

    /**
     * The set of meta-annotations to map
     */
    Set<Class<? extends Annotation>> MAPPED_METAANNOTATIONS = Arrays2.asSet(Qualifier.class, Stereotype.class, Scope.class,
            NormalScope.class, InterceptorBinding.class);

    /**
     * The set of declared meta-annotations to map
     */
    Set<Class<? extends Annotation>> MAPPED_DECLARED_METAANNOTATIONS = Arrays2.asSet(Scope.class, NormalScope.class);

    /**
     * Gets all annotations which are annotated with the given meta annotation
     * type
     *
     * @param The meta annotation to match
     * @return A set of matching meta-annotations. Returns an empty set if there
     *         are no matches.
     */
    Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType);

    /**
     * Gets the binding types for this element
     *
     * @returns A set of binding types present on the type. Returns an empty set
     *          if there are no matches.
     * @deprecated This reflection type should not know about JSR-299 binding
     *             types
     */
    @Deprecated
    Set<Annotation> getQualifiers();

    /**
     * Gets the binding types for this element
     *
     * @returns An array of binding types present on the type. Returns an empty
     *          array if there are no matches.
     * @deprecated This reflection type should not know about JSR-299 binding
     *             types
     */
    @Deprecated
    Annotation[] getBindingsAsArray();

    /**
     * Get the type hierarchy of any interfaces implemented by this class.
     * <p/>
     * The returned types should have any type parameters resolved to their
     * actual types.
     * <p/>
     * There is no guarantee this methods executes in O(1) time
     *
     * @return the type hierarchy
     */
    Set<Type> getInterfaceClosure();

    /**
     * Gets the type of the element
     *
     * @return The type of the element
     */
    Class<T> getJavaClass();

    /**
     * Gets the actual type arguments for any parameterized types that this
     * AnnotatedItem represents.
     *
     * @return An array of type arguments
     */
    Type[] getActualTypeArguments();

    /**
     * Indicates if this AnnotatedItem represents a static element
     *
     * @return True if static, false otherwise
     */
    boolean isStatic();

    boolean isGeneric();

    /**
     * Indicates if this AnnotatedItem represents a final element
     *
     * @return True if final, false otherwise
     */
    boolean isFinal();

    /**
     * Indicates if this annotated item is public
     *
     * @return if public, returns true
     */
    boolean isPublic();

    boolean isPrivate();

    boolean isPackagePrivate();

    Package getPackage();

    /**
     * Gets the name of this AnnotatedItem
     * <p/>
     * If it is not possible to determine the name of the underling element, a
     * IllegalArgumentException is thrown
     *
     * @return The name
     */
    String getName();

    boolean isParameterizedType();

    boolean isPrimitive();

    /**
     * Returns a lightweight implementation of {@link Annotated} with minimal memory footprint.
     *
     * @return the slim version of this {@link Annotated}
     */
    Annotated slim();

}
