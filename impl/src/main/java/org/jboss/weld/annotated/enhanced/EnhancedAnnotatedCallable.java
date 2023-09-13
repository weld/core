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
import java.lang.reflect.Member;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedCallable;

/**
 * @author pmuir
 */
public interface EnhancedAnnotatedCallable<T, X, S extends Member>
        extends EnhancedAnnotatedMember<T, X, S>, AnnotatedCallable<X> {

    /**
     * Gets the abstracted parameters of the method
     *
     * @return A list of parameters. Returns an empty list if no parameters are
     *         present.
     */
    List<? extends EnhancedAnnotatedParameter<?, X>> getEnhancedParameters();

    /**
     * Gets the list of annotated parameters for a given annotation
     *
     * @param annotationType The annotation to match
     * @return A set of matching parameter abstractions. Returns an empty list if
     *         there are no matches.
     */
    List<EnhancedAnnotatedParameter<?, X>> getEnhancedParameters(Class<? extends Annotation> annotationType);

    /**
     * Returns a lightweight implementation of {@link AnnotatedCallable} with minimal memory footprint.
     *
     * @return the slim version of this {@link AnnotatedCallable}
     */
    AnnotatedCallable<X> slim();
}
