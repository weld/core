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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.inject.Scope;

import org.jboss.weld.annotated.slim.backed.BackedAnnotatedParameter;
import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.util.Function;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.Arrays2;

import com.google.common.collect.ImmutableSet;

public class DefaultReflectionCache extends AbstractBootstrapService implements ReflectionCache {

    private final TypeStore store;

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

    private final ComputingCache<AnnotatedElement, Annotations> annotations;
    private final ComputingCache<AnnotatedElement, Annotations> declaredAnnotations;
    private final ComputingCache<Constructor<?>, Annotation[][]> constructorParameterAnnotations;
    private final ComputingCache<Method, Annotation[][]> methodParameterAnnotations;
    private final ComputingCache<BackedAnnotatedParameter<?>, Set<Annotation>> parameterAnnotationSet;
    private final ComputingCache<Class<?>, Set<Annotation>> backedAnnotatedTypeAnnotations;
    private final ComputingCache<Class<? extends Annotation>, Boolean> isScopeAnnotation;

    public DefaultReflectionCache(TypeStore store) {
        this.store = store;
        ComputingCacheBuilder cacheBuilder = ComputingCacheBuilder.newBuilder();
        this.annotations = cacheBuilder.build(new Function<AnnotatedElement, Annotations>() {
            @Override
            public Annotations apply(AnnotatedElement input) {
                return new Annotations(internalGetAnnotations(input));
            }
        });
        this.declaredAnnotations = cacheBuilder.build(new Function<AnnotatedElement, Annotations>() {
            @Override
            public Annotations apply(AnnotatedElement input) {
                return new Annotations(internalGetDeclaredAnnotations(input));
            }
        });
        this.constructorParameterAnnotations = cacheBuilder.build(new Function<Constructor<?>, Annotation[][]>() {
            @Override
            public Annotation[][] apply(Constructor<?> input) {
                return input.getParameterAnnotations();
            }
        });
        this.methodParameterAnnotations = cacheBuilder.build(new Function<Method, Annotation[][]>() {
            @Override
            public Annotation[][] apply(Method input) {
                return input.getParameterAnnotations();
            }
        });
        this.parameterAnnotationSet = cacheBuilder.build(new Function<BackedAnnotatedParameter<?>, Set<Annotation>>() {
            @Override
            public Set<Annotation> apply(BackedAnnotatedParameter<?> parameter) {
                final Member member = parameter.getDeclaringCallable().getJavaMember();
                if (member instanceof Method) {
                    return ImmutableSet.copyOf( getParameterAnnotations((Method) member, parameter.getPosition()));
                } else {
                    return ImmutableSet.copyOf( getParameterAnnotations((Constructor<?>) member, parameter.getPosition()));
                }
            }

        });
        this.backedAnnotatedTypeAnnotations = cacheBuilder.build(new BackedAnnotatedTypeAnnotationsFunction());
        this.isScopeAnnotation = cacheBuilder.build(new IsScopeAnnotationFunction());
    }

    @Override
    public Annotation[] getAnnotations(AnnotatedElement element) {
        return annotations.getValue(element).annotations;
    }

    @Override
    public Annotation[] getDeclaredAnnotations(AnnotatedElement element) {
        return declaredAnnotations.getValue(element).annotations;
    }

    @Override
    public Annotation[] getParameterAnnotations(Constructor<?> constructor, int parameterPosition) {
        return constructorParameterAnnotations.getValue(constructor)[parameterPosition];
    }

    @Override
    public Annotation[] getParameterAnnotations(Method method, int parameterPosition) {
        return methodParameterAnnotations.getValue(method)[parameterPosition];
    }

    @Override
    public void cleanupAfterBoot() {
        annotations.clear();
        declaredAnnotations.clear();
        constructorParameterAnnotations.clear();
        methodParameterAnnotations.clear();
        backedAnnotatedTypeAnnotations.clear();
        isScopeAnnotation.clear();
        parameterAnnotationSet.clear();
    }

    @Override
    public Set<Annotation> getAnnotationSet(AnnotatedElement element) {
        return annotations.getValue(element).annotationSet;
    }

    @Override
    public Set<Annotation> getDeclaredAnnotationSet(AnnotatedElement element) {
        return declaredAnnotations.getValue(element).annotationSet;
    }

    @Override
    public Set<Annotation> getParameterAnnotationSet(BackedAnnotatedParameter<?> parameter) {
        return parameterAnnotationSet.getValue(parameter);
    }

    @Override
    public Set<Annotation> getBackedAnnotatedTypeAnnotationSet(Class<?> javaClass) {
        return backedAnnotatedTypeAnnotations.getValue(javaClass);
    }

    private class BackedAnnotatedTypeAnnotationsFunction implements Function<Class<?>, Set<Annotation>> {

        @Override
        public Set<Annotation> apply(Class<?> javaClass) {
            Set<Annotation> annotations = getAnnotationSet(javaClass);
            boolean scopeFound = false;
            for (Annotation annotation : annotations) {
                boolean isScope = isScopeAnnotation.getValue(annotation.annotationType());
                if (isScope && scopeFound) {
                    // there are at least two scopes, we need to choose one using scope inheritance rules (4.1)
                    return applyScopeInheritanceRules(annotations, javaClass);
                }
                if (isScope) {
                    scopeFound = true;
                }
            }
            return annotations;
        }

        public Set<Annotation> applyScopeInheritanceRules(Set<Annotation> annotations, Class<?> javaClass) {
            Set<Annotation> result = new HashSet<Annotation>();
            for (Annotation annotation : annotations) {
                if (!isScopeAnnotation.getValue(annotation.annotationType())) {
                    result.add(annotation);
                }
            }
            result.addAll(findTopLevelScopeDefinitions(javaClass));
            return ImmutableSet.copyOf(result);
        }

        public Set<Annotation> findTopLevelScopeDefinitions(Class<?> javaClass) {
            for (Class<?> clazz = javaClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                Set<Annotation> scopes = new HashSet<Annotation>();
                for (Annotation annotation : getDeclaredAnnotations(clazz)) {
                    if (isScopeAnnotation.getValue(annotation.annotationType())) {
                        scopes.add(annotation);
                    }
                }
                if (scopes.size() > 0) {
                    return scopes;
                }
            }
            throw new IllegalStateException();
        }
    }

    private class IsScopeAnnotationFunction implements Function<Class<? extends Annotation>, Boolean> {

        @Override
        public Boolean apply(Class<? extends Annotation> input) {
            if (input.isAnnotationPresent(NormalScope.class)) {
                return true;
            }
            if (input.isAnnotationPresent(Scope.class)) {
                return true;
            }
            return store.isExtraScope(input);
        }
    }
}
