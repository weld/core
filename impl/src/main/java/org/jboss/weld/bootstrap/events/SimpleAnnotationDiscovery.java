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

import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.spi.AnnotationDiscovery;

/**
 * Basic implementation of {@link AnnotationDiscovery} which uses Java Reflection. This is the fallback option if an annotation
 * index is not available.
 *
 * @author Jozef Hartinger
 *
 */
public class SimpleAnnotationDiscovery implements AnnotationDiscovery {

    private final ReflectionCache cache;

    public SimpleAnnotationDiscovery(ReflectionCache cache) {
        this.cache = cache;
    }

    @Override
    public boolean containsAnnotation(Class<?> javaClass, Class<? extends Annotation> requiredAnnotation) {
        for (Class<?> clazz = javaClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // class level annotations
            if (clazz == javaClass || requiredAnnotation.isAnnotationPresent(Inherited.class)) {
                if (containsAnnotations(cache.getAnnotations(clazz), requiredAnnotation)) {
                    return true;
                }
            }
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotations(cache.getAnnotations(field), requiredAnnotation)) {
                    return true;
                }
            }
            // constructors
            for (Constructor<?> constructor : clazz.getConstructors()) {
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
        return false;
    }

    private boolean containsAnnotations(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        return containsAnnotation(annotations, requiredAnnotation, true);
    }

    private boolean containsAnnotation(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation, boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (requiredAnnotation.equals(annotationType)) {
                return true;
            }
            if (checkMetaAnnotations && containsAnnotation(cache.getAnnotations(annotationType), requiredAnnotation, false)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void cleanup() {
    }
}
