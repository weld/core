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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Wrapper over {@link ReflectionCache} capable of determining whether a given class
 * has a given annotation or not, as defined by the CDI 1.1 specification (11.5.6).
 *
 * @author Jozef Hartinger
 *
 */
public class RequiredAnnotationDiscovery extends AbstractBootstrapService {

    private final ReflectionCache cache;

    /**
     * For each class, the annotation types that make {@link #containsAnnotation} true. Every observer with
     * {@code @WithAnnotations} asks about each of its required annotations separately, so without this every one of
     * them would walk the whole class hierarchy and its members again.
     */
    private final ConcurrentMap<Class<?>, Set<Class<? extends Annotation>>> matchingAnnotations = new ConcurrentHashMap<>();

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
        return matchingAnnotations
                .computeIfAbsent(annotatedType.getJavaClass(), clazz -> findMatchingAnnotations(annotatedType))
                .contains(requiredAnnotation);
    }

    /**
     * Collects, in one walk, the type of every annotation that is present where {@link #containsAnnotation} looks,
     * and the types of the annotations that those annotations are annotated with.
     */
    private Set<Class<? extends Annotation>> findMatchingAnnotations(BackedAnnotatedType<?> annotatedType) {
        Set<Class<? extends Annotation>> found = new HashSet<>();
        // class level annotations
        addAnnotations(annotatedType.getAnnotations(), found);
        for (Class<?> clazz = annotatedType.getJavaClass(); clazz != null
                && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                addAnnotations(cache.getAnnotations(field), found);
            }
            // constructors
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                addAnnotations(cache.getAnnotations(constructor), found);
                addParameterAnnotations(constructor.getParameterAnnotations(), found);
            }
            // methods
            for (Method method : clazz.getDeclaredMethods()) {
                addAnnotations(cache.getAnnotations(method), found);
                addParameterAnnotations(method.getParameterAnnotations(), found);
            }
        }

        // Also check default methods on interfaces
        for (Class<?> interfaceClazz : Reflections.getInterfaceClosure(annotatedType.getJavaClass())) {
            for (Method method : interfaceClazz.getDeclaredMethods()) {
                if (Reflections.isDefault(method)) {
                    addAnnotations(cache.getAnnotations(method), found);
                    addParameterAnnotations(method.getParameterAnnotations(), found);
                }
            }
        }
        return found;
    }

    private void addParameterAnnotations(Annotation[][] parameterAnnotations, Set<Class<? extends Annotation>> found) {
        for (Annotation[] annotations : parameterAnnotations) {
            addAnnotations(annotations, found);
        }
    }

    private void addAnnotations(Annotation[] annotations, Set<Class<? extends Annotation>> found) {
        for (Annotation annotation : annotations) {
            addAnnotation(annotation, found);
        }
    }

    private void addAnnotations(Iterable<? extends Annotation> annotations, Set<Class<? extends Annotation>> found) {
        for (Annotation annotation : annotations) {
            addAnnotation(annotation, found);
        }
    }

    private void addAnnotation(Annotation annotation, Set<Class<? extends Annotation>> found) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        found.add(annotationType);
        for (Annotation metaAnnotation : cache.getAnnotations(annotationType)) {
            found.add(metaAnnotation.annotationType());
        }
    }

    @Override
    public void cleanupAfterBoot() {
        matchingAnnotations.clear();
    }
}
