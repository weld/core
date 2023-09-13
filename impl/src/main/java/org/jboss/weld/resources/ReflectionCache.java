/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.metadata.TypeStore;

public interface ReflectionCache extends Service {

    Set<Annotation> getAnnotations(AnnotatedElement element);

    Set<Annotation> getDeclaredAnnotations(AnnotatedElement element);

    /**
     * Returns the set of annotations for a {@link BackedAnnotatedType}. This are all annotations declared directly on the
     * {@link BackedAnnotatedType#getJavaClass()} and all {@link Inherited} annotations. In addition, scope annotation
     * inheritance rules (as defined in section 4.1) are applied.
     *
     * @param javaClass
     * @return the set of type-level annotations of a given type
     */
    Set<Annotation> getBackedAnnotatedTypeAnnotationSet(Class<?> javaClass);

    <T extends Annotation> AnnotationClass<T> getAnnotationClass(Class<T> clazz);

    /**
     * Represents cached metadata about an annotation class. Compared to {@link TypeStore} this class holds
     * low-level metadata such as whether this annotation is a container for repeatable annotations.
     *
     * @author Jozef Hartinger
     *
     * @param <T> type the type of the annotation
     */
    interface AnnotationClass<T extends Annotation> {

        /**
         * @return the set of meta-annotations this annotation class is annotated with
         */
        Set<Annotation> getMetaAnnotations();

        /**
         * @return true iff this annotation represents a scope annotation
         */
        boolean isScope();

        /**
         * @return true iff this annotation represents a container for repeatable annotations
         */
        boolean isRepeatableAnnotationContainer();

        /**
         * Retrieves repeatable annotations held by this container annotation instance.
         *
         * @return repeatable annotations held by this container annotation instance
         * @throws IllegalStateException if this annotation class is not a repeatable annotation container
         */
        Annotation[] getRepeatableAnnotations(Annotation annotation);
    }
}
