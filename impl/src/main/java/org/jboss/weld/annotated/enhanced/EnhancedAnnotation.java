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
import java.util.Set;

/**
 * Represents a meta annotation
 *
 * @author Pete Muir
 */
public interface EnhancedAnnotation<T extends Annotation> extends EnhancedAnnotatedType<T> {
    /**
     * Gets all members
     *
     * @return A set of abstracted members
     */
    Set<EnhancedAnnotatedMethod<?, ?>> getMembers();

    /**
     * Gets all the members annotated with annotationType
     *
     * @param annotationType The annotation type to match
     * @return A set of abstracted members with the annotation type
     */
    Set<EnhancedAnnotatedMethod<?, ?>> getMembers(Class<? extends Annotation> annotationType);

}
