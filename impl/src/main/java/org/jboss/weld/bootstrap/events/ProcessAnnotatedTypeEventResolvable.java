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

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

import com.google.common.collect.Sets;

/**
 * For {@link ProcessAnnotatedType} we need a special {@link Resolvable} in order to support {@link WithAnnotations} properly.
 *
 * @author Jozef Hartinger
 *
 */
public class ProcessAnnotatedTypeEventResolvable implements Resolvable {

    public static ProcessAnnotatedTypeEventResolvable forProcessAnnotatedType(Class<?> typeArgument, AnnotationDiscovery discovery) {
        ParameterizedType type = new ParameterizedTypeImpl(ProcessAnnotatedType.class, new Type[] { typeArgument }, null);
        return new ProcessAnnotatedTypeEventResolvable(Sets.<Type> newHashSet(Object.class, type), typeArgument, discovery);
    }

    public static ProcessAnnotatedTypeEventResolvable forProcessSyntheticAnnotatedType(Class<?> typeArgument, AnnotationDiscovery discovery) {
        ParameterizedType type1 = new ParameterizedTypeImpl(ProcessAnnotatedType.class, new Type[] { typeArgument }, null);
        ParameterizedType type2 = new ParameterizedTypeImpl(ProcessSyntheticAnnotatedType.class, new Type[] { typeArgument },
                null);
        return new ProcessAnnotatedTypeEventResolvable(Sets.<Type> newHashSet(Object.class, type1, type2), typeArgument, discovery);
    }

    private static final Set<QualifierInstance> QUALIFIERS = Collections.singleton(QualifierInstance.ANY);
    private final Set<Type> types;
    private final Class<?> javaClass;
    private final AnnotationDiscovery discovery;

    protected ProcessAnnotatedTypeEventResolvable(Set<Type> types, Class<?> javaClass, AnnotationDiscovery discovery) {
        this.types = types;
        this.javaClass = javaClass;
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
        return discovery.containsAnnotations(javaClass, requiredAnnotations);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return false;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return null;
    }

    @Override
    public boolean isAssignableTo(Class<?> clazz) {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ProcessAnnotatedTypeEventResolvable))
            return false;
        ProcessAnnotatedTypeEventResolvable other = (ProcessAnnotatedTypeEventResolvable) obj;
        if (types == null) {
            if (other.types != null)
                return false;
        } else if (!types.equals(other.types))
            return false;
        return true;
    }
}
