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

import jakarta.enterprise.inject.spi.AnnotatedParameter;

/**
 * AnnotatedParameter provides a uniform access to a method parameter defined
 * either in Java or XML
 *
 * @param <T>
 * @author Pete Muir
 */
public interface EnhancedAnnotatedParameter<T, X> extends EnhancedAnnotated<T, Object>, AnnotatedParameter<X> {

    EnhancedAnnotatedCallable<?, X, ?> getDeclaringEnhancedCallable();

    EnhancedAnnotatedType<X> getDeclaringType();

    /**
     * Returns a lightweight implementation of {@link AnnotatedParameter} with minimal memory footprint.
     *
     * @return the slim version of this {@link AnnotatedParameter}
     */
    AnnotatedParameter<X> slim();

}
