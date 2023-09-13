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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.annotated.slim.unbacked.UnbackedAnnotatedType;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.util.collections.Sets;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

/**
 * For {@link ProcessAnnotatedType} we need a special {@link Resolvable} in order to support {@link WithAnnotations} properly.
 *
 * @author Jozef Hartinger
 *
 */
public class ProcessAnnotatedTypeEventResolvable implements Resolvable {

    public static ProcessAnnotatedTypeEventResolvable of(ProcessAnnotatedTypeImpl<?> event,
            RequiredAnnotationDiscovery discovery) {
        if (event instanceof ProcessSyntheticAnnotatedType) {
            return forProcessSyntheticAnnotatedType(event.getOriginalAnnotatedType(), discovery);
        } else {
            return forProcessAnnotatedType(event.getOriginalAnnotatedType(), discovery);
        }
    }

    public static ProcessAnnotatedTypeEventResolvable forProcessAnnotatedType(SlimAnnotatedType<?> annotatedType,
            RequiredAnnotationDiscovery discovery) {
        ParameterizedType type = new ParameterizedTypeImpl(ProcessAnnotatedType.class,
                new Type[] { annotatedType.getJavaClass() }, null);
        return new ProcessAnnotatedTypeEventResolvable(Sets.<Type> newHashSet(Object.class, type), annotatedType, discovery);
    }

    public static ProcessAnnotatedTypeEventResolvable forProcessSyntheticAnnotatedType(SlimAnnotatedType<?> annotatedType,
            RequiredAnnotationDiscovery discovery) {
        ParameterizedType type1 = new ParameterizedTypeImpl(ProcessAnnotatedType.class,
                new Type[] { annotatedType.getJavaClass() }, null);
        ParameterizedType type2 = new ParameterizedTypeImpl(ProcessSyntheticAnnotatedType.class,
                new Type[] { annotatedType.getJavaClass() }, null);
        return new ProcessAnnotatedTypeEventResolvable(Sets.<Type> newHashSet(Object.class, type1, type2), annotatedType,
                discovery);
    }

    private static final Set<QualifierInstance> QUALIFIERS = Collections.singleton(QualifierInstance.ANY);
    private final Set<Type> types;
    private final SlimAnnotatedType<?> annotatedType;
    private final RequiredAnnotationDiscovery discovery;

    protected ProcessAnnotatedTypeEventResolvable(Set<Type> types, SlimAnnotatedType<?> annotatedType,
            RequiredAnnotationDiscovery discovery) {
        this.types = types;
        this.annotatedType = annotatedType;
        this.discovery = discovery;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<QualifierInstance> getQualifiers() {
        return QUALIFIERS;
    }

    /**
     * Returns true if and only if the underlying {@link AnnotatedType} contains any of the given annotation types.
     */
    public boolean containsRequiredAnnotations(Collection<Class<? extends Annotation>> requiredAnnotations) {
        if (annotatedType instanceof BackedAnnotatedType<?>) {
            return containsAnnotation((BackedAnnotatedType<?>) annotatedType, requiredAnnotations);
        } else if (annotatedType instanceof UnbackedAnnotatedType<?>) {
            return containsAnnotation((UnbackedAnnotatedType<?>) annotatedType, requiredAnnotations);
        } else {
            throw new IllegalArgumentException(
                    "Unknown SlimAnnotatedType implementation: " + annotatedType.getClass().toString());
        }
    }

    protected boolean containsAnnotation(UnbackedAnnotatedType<?> annotatedType,
            Collection<Class<? extends Annotation>> requiredAnnotations) {
        for (final Class<? extends Annotation> requiredAnnotation : requiredAnnotations) {
            if (apply(annotatedType, requiredAnnotation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEqualOrAnnotated(Class<? extends Annotation> requiredAnnotation, Annotation annotation) {
        return annotation.annotationType().equals(requiredAnnotation)
                || annotation.annotationType().isAnnotationPresent(requiredAnnotation);
    }

    /**
     * @return true if predicate returns true for any annotation defined anywhere on the annotatedType
     */
    protected boolean apply(UnbackedAnnotatedType<?> annotatedType, Class<? extends Annotation> requiredAnnotation) {
        // type annotations
        for (Annotation annotation : annotatedType.getAnnotations()) {
            if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                return true;
            }
            if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                return true;
            }
        }
        for (AnnotatedField<?> field : annotatedType.getFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                    return true;
                }
            }
        }
        for (AnnotatedConstructor<?> constructor : annotatedType.getConstructors()) {
            for (Annotation annotation : constructor.getAnnotations()) {
                if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                    return true;
                }
            }
            for (AnnotatedParameter<?> parameter : constructor.getParameters()) {
                for (Annotation annotation : parameter.getAnnotations()) {
                    if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                        return true;
                    }
                }
            }
        }
        for (AnnotatedMethod<?> method : annotatedType.getMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                    return true;
                }
            }
            for (AnnotatedParameter<?> parameter : method.getParameters()) {
                for (Annotation annotation : parameter.getAnnotations()) {
                    if (isEqualOrAnnotated(requiredAnnotation, annotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean containsAnnotation(BackedAnnotatedType<?> annotatedType,
            Collection<Class<? extends Annotation>> requiredAnnotations) {
        for (Class<? extends Annotation> requiredAnnotation : requiredAnnotations) {
            if (discovery.containsAnnotation(annotatedType, requiredAnnotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?> getJavaClass() {
        return null;
    }

    @Override
    public Bean<?> getDeclaringBean() {
        return null;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((types == null) ? 0 : types.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ProcessAnnotatedTypeEventResolvable)) {
            return false;
        }
        ProcessAnnotatedTypeEventResolvable other = (ProcessAnnotatedTypeEventResolvable) obj;
        if (types == null) {
            if (other.types != null) {
                return false;
            }
        } else if (!types.equals(other.types)) {
            return false;
        }
        return true;
    }
}
