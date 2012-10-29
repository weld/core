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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.util.collections.Arrays2;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

public class DefaultReflectionCache extends AbstractBootstrapService implements ReflectionCache {

    protected Annotation[] internalGetAnnotations(AnnotatedElement element) {
        return element.getAnnotations();
    }

    protected Annotation[] internalGetDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    private static class Annotations {
        private final Annotation[] annotations;
        private final Set<Annotation> annotationSet;

        public Annotations(Annotation[] annotations) {
            if (annotations.length == 0) {
                this.annotations = Arrays2.EMPTY_ANNOTATION_ARRAY;
                this.annotationSet = Collections.emptySet();
            } else {
                this.annotations = annotations;
                this.annotationSet = ImmutableSet.copyOf(annotations);
            }
        }
    }

    private final Map<AnnotatedElement, Annotations> annotations;
    private final Map<AnnotatedElement, Annotations> declaredAnnotations;
    private final Map<Constructor<?>, Annotation[][]> constructorParameterAnnotations;
    private final Map<Method, Annotation[][]> methodParameterAnnotations;

    public DefaultReflectionCache() {
        MapMaker maker = new MapMaker();
        this.annotations = maker.makeComputingMap(new Function<AnnotatedElement, Annotations>() {
            @Override
            public Annotations apply(AnnotatedElement input) {
                return new Annotations(internalGetAnnotations(input));
            }
        });
        this.declaredAnnotations = maker.makeComputingMap(new Function<AnnotatedElement, Annotations>() {
            @Override
            public Annotations apply(AnnotatedElement input) {
                return new Annotations(internalGetDeclaredAnnotations(input));
            }
        });
        this.constructorParameterAnnotations = maker.makeComputingMap(new Function<Constructor<?>, Annotation[][]>() {
            @Override
            public Annotation[][] apply(Constructor<?> input) {
                return input.getParameterAnnotations();
            }
        });
        this.methodParameterAnnotations = maker.makeComputingMap(new Function<Method, Annotation[][]>() {
            @Override
            public Annotation[][] apply(Method input) {
                return input.getParameterAnnotations();
            }
        });
    }

    public Annotation[] getAnnotations(AnnotatedElement element) {
        return annotations.get(element).annotations;
    }

    public Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return declaredAnnotations.get(element).annotations;
    }

    @Override
    public Annotation[] getParameterAnnotations(Constructor<?> constructor, int parameterPosition) {
        return constructorParameterAnnotations.get(constructor)[parameterPosition];
    }

    @Override
    public Annotation[] getParameterAnnotations(Method method, int parameterPosition) {
        return methodParameterAnnotations.get(method)[parameterPosition];
    }

    @Override
    public void cleanupAfterBoot() {
        annotations.clear();
        declaredAnnotations.clear();
        constructorParameterAnnotations.clear();
        methodParameterAnnotations.clear();
    }

    @Override
    public Set<Annotation> getAnnotationSet(AnnotatedElement element) {
        return annotations.get(element).annotationSet;
    }

    @Override
    public Set<Annotation> getDeclaredAnnotationSet(AnnotatedElement element) {
        return declaredAnnotations.get(element).annotationSet;
    }

    @Override
    public Set<Annotation> getParameterAnnotationSet(Constructor<?> constructor, int parameterPosition) {
        return ImmutableSet.copyOf(getParameterAnnotations(constructor, parameterPosition));
    }

    @Override
    public Set<Annotation> getParameterAnnotationSet(Method method, int parameterPosition) {
        return ImmutableSet.copyOf(getParameterAnnotations(method, parameterPosition));
    }
}
