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

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.NormalScope;
import javax.inject.Scope;

import org.jboss.weld.bootstrap.api.helpers.AbstractBootstrapService;
import org.jboss.weld.metadata.TypeStore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

public class DefaultReflectionCache extends AbstractBootstrapService implements ReflectionCache {

    private final TypeStore store;

    protected Annotation[] internalGetAnnotations(AnnotatedElement element) {
        return element.getAnnotations();
    }

    protected Annotation[] internalGetDeclaredAnnotations(AnnotatedElement element) {
        return element.getDeclaredAnnotations();
    }

    private final LoadingCache<AnnotatedElement, Set<Annotation>> annotations;
    private final LoadingCache<AnnotatedElement, Set<Annotation>> declaredAnnotations;
    private final LoadingCache<Class<?>, Set<Annotation>> backedAnnotatedTypeAnnotations;
    private final LoadingCache<Class<? extends Annotation>, Boolean> isScopeAnnotation;

    public DefaultReflectionCache(TypeStore store) {
        this.store = store;
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();
        this.annotations = cacheBuilder.build(new CacheLoader<AnnotatedElement, Set<Annotation>>() {
            @Override
            public Set<Annotation> load(AnnotatedElement input) {
                return ImmutableSet.copyOf(internalGetAnnotations(input));
            }
        });
        this.declaredAnnotations = cacheBuilder.build(new CacheLoader<AnnotatedElement, Set<Annotation>>() {
            @Override
            public Set<Annotation> load(AnnotatedElement input) {
                return ImmutableSet.copyOf(internalGetDeclaredAnnotations(input));
            }
        });
        this.backedAnnotatedTypeAnnotations = cacheBuilder.build(new BackedAnnotatedTypeAnnotationsFunction());
        this.isScopeAnnotation = cacheBuilder.build(new IsScopeAnnotationFunction());
    }



    @Override
    public void cleanupAfterBoot() {
        annotations.invalidateAll();
        declaredAnnotations.invalidateAll();
        backedAnnotatedTypeAnnotations.invalidateAll();
        isScopeAnnotation.invalidateAll();
    }

    @Override
    public Set<Annotation> getAnnotations(AnnotatedElement element) {
        return getCacheValue(annotations, element);
    }

    @Override
    public Set<Annotation> getDeclaredAnnotations(AnnotatedElement element) {
        return getCacheValue(declaredAnnotations, element);
    }

    @Override
    public Set<Annotation> getBackedAnnotatedTypeAnnotationSet(Class<?> javaClass) {
        return getCacheValue(backedAnnotatedTypeAnnotations, javaClass);
    }

    private class BackedAnnotatedTypeAnnotationsFunction extends CacheLoader<Class<?>, Set<Annotation>> {

        @Override
        public Set<Annotation> load(Class<?> javaClass) {
            Set<Annotation> annotations = getAnnotations(javaClass);
            boolean scopeFound = false;
            for (Annotation annotation : annotations) {
                boolean isScope = getCacheValue(isScopeAnnotation, annotation.annotationType());
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
                if (!getCacheValue(isScopeAnnotation, annotation.annotationType())) {
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
                    if (getCacheValue(isScopeAnnotation, annotation.annotationType())) {
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

    private class IsScopeAnnotationFunction extends CacheLoader<Class<? extends Annotation>, Boolean> {

        @Override
        public Boolean load(Class<? extends Annotation> input) {
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
