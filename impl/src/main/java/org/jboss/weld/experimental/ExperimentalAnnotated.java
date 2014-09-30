/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.experimental;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;

import org.jboss.weld.util.collections.Arrays2;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link Annotated}.
 *
 * @author Jozef Hartinger
 *
 * @see WELD-1743
 *
 * @param <X>
 */
public interface ExperimentalAnnotated extends Annotated {

    /**
     * Equivalent of {@link AnnotatedElement#getAnnotationsByType(Class)}.
     *
     * @param annotationClass the Class object corresponding to the annotation type
     * @return all this element's annotations for the specified annotation type if associated with this element, else an array of length zero
     */
    @SuppressWarnings("unchecked")
    default <T extends Annotation> Set<T> getAnnotationsByType(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass, "annotationClass");
        // first, delegate to getAnnotation()
        final T annotation = getAnnotation(annotationClass);
        if (annotation != null) {
            return Collections.singleton(annotation);
        }
        // second, check if this annotation is repeatable
        final Repeatable repeatable = annotationClass.getAnnotation(Repeatable.class);
        if (repeatable != null) {
            Class<? extends Annotation> containerClass = repeatable.value();
            Annotation container = getAnnotation(containerClass);
            if (container != null) {
                // we found a container, extract the values
                Method value;
                try {
                    value = containerClass.getMethod("value");
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new RuntimeException(e); // TODO
                }
                try {
                    return Arrays2.asSet((T[]) value.invoke(container));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e); // TODO
                }
            }
        }
        return Collections.emptySet();
    }
}
