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
import java.lang.reflect.Method;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML
 *
 * @author Pete Muir
 */
public interface EnhancedAnnotatedMethod<T, X> extends EnhancedAnnotatedCallable<T, X, Method>, AnnotatedMethod<X> {

    /**
     * Get the parameter types as an array
     */
    Class<?>[] getParameterTypesAsArray();

    /**
     * Gets the property name
     *
     * @return The name
     */
    String getPropertyName();

    /**
     * Checks if a this is equivalent to a JLR method
     *
     * @param method The JLR method
     * @return true if equivalent
     */
    boolean isEquivalent(Method method);

    MethodSignature getSignature();

    /**
     * Get the parameters annotated with a given annotation type.
     */
    List<EnhancedAnnotatedParameter<?, X>> getEnhancedParameters(Class<? extends Annotation> annotationType);

    /**
     * Returns a lightweight implementation of {@link AnnotatedMethod} with minimal memory footprint.
     *
     * @return the slim version of this {@link AnnotatedMethod}
     */
    AnnotatedMethod<X> slim();

}
