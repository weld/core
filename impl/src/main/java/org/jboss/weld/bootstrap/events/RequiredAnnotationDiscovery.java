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
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Wrapper over {@link ReflectionCache} capable of determining whether a given class
 * has a given annotation or not, as defined by the CDI 1.1 specification (11.5.6).
 *
 * @author Jozef Hartinger
 *
 */
public class RequiredAnnotationDiscovery implements Service {

    private final ReflectionCache cache;

    public RequiredAnnotationDiscovery(ReflectionCache cache) {
        this.cache = cache;
    }

    /**
     * <p>
     * Indicates whether the given class contains an annotation of the given annotation type.
     * </p>
     *
     * <p>
     * The set is referred to as <em>M</em> hereafter
     * </p>
     *
     * <p>
     * The given class is said to contain the given annotation if any of these applies:
     * </p>
     *
     * <ul>
     * <li>The required annotation or an annotation annotated with the required annotation is present on the class</li>
     * <li>The required annotation or an annotation annotated with the required annotation, which is annotated with
     * {@link Inherited}, is present on a direct or
     * indirect superclass of the given class</li>
     * <li>The required annotation or an annotation annotated with the required annotation is present on a field or method
     * declared by the given class or any
     * direct or indirect superclass of the given class</li>
     * <li>The required annotation or an annotation annotated with the required annotation is present on a parameter of a method
     * declared by the given class or
     * any direct or indirect superclass of the given class</li>
     * <li>The annotation or an annotation annotated with the required annotation is present on a default method or a parameter
     * of a default method declared by an interface directly or
     * indirectly implemented by the given class</li>
     * <li>The required annotation or an annotation annotated with the required annotation is present on a constructor declared
     * by the given class</li>
     * <li>The required annotation or an annotation annotated with the required annotation is present on a parameter of a
     * constructor declared by the given
     * class</li>
     * </ul>
     *
     * @param javaClass the given class
     * @param annotation the given annotation type
     * @return
     */
    public boolean containsAnnotation(BackedAnnotatedType<?> annotatedType, Class<? extends Annotation> requiredAnnotation) {
        // class level annotations
        if (containsAnnotation(annotatedType.getAnnotations(), requiredAnnotation, true)) {
            return true;
        }
        for (Class<?> clazz = annotatedType.getJavaClass(); clazz != null
                && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotations(cache.getAnnotations(field), requiredAnnotation)) {
                    return true;
                }
            }
            // constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (containsAnnotations(cache.getAnnotations(constructor), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : constructor.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
            // methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (containsAnnotations(cache.getAnnotations(method), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
        }

        // Also check default methods on interfaces
        for (Class<?> interfaceClazz : Reflections.getInterfaceClosure(annotatedType.getJavaClass())) {
            for (Method method : interfaceClazz.getDeclaredMethods()) {
                if (Reflections.isDefault(method)) {
                    if (containsAnnotations(cache.getAnnotations(method), requiredAnnotation)) {
                        return true;
                    }
                    for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                        if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean containsAnnotations(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        return containsAnnotation(annotations, requiredAnnotation, true);
    }

    private boolean containsAnnotation(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation,
            boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            if (containsAnnotation(annotation, requiredAnnotation, checkMetaAnnotations)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnnotations(Iterable<? extends Annotation> annotations,
            Class<? extends Annotation> requiredAnnotation) {
        return containsAnnotation(annotations, requiredAnnotation, true);
    }

    private boolean containsAnnotation(Iterable<? extends Annotation> annotations,
            Class<? extends Annotation> requiredAnnotation, boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            if (containsAnnotation(annotation, requiredAnnotation, checkMetaAnnotations)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAnnotation(Annotation annotation, Class<? extends Annotation> requiredAnnotation,
            boolean checkMetaAnnotations) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (requiredAnnotation.equals(annotationType)) {
            return true;
        }
        if (checkMetaAnnotations && containsAnnotation(cache.getAnnotations(annotationType), requiredAnnotation, false)) {
            return true;
        }
        return false;
    }

    @Override
    public void cleanup() {
    }
}
