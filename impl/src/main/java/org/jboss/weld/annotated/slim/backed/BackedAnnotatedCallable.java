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
package org.jboss.weld.annotated.slim.backed;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.resources.SharedObjectCache;

public abstract class BackedAnnotatedCallable<X, E extends Executable> extends BackedAnnotatedMember<X>
        implements AnnotatedCallable<X> {

    private final List<AnnotatedParameter<X>> parameters;
    private final E executable;

    public BackedAnnotatedCallable(E executable, Type baseType, BackedAnnotatedType<X> declaringType,
            SharedObjectCache sharedObjectCache) {
        super(baseType, declaringType, sharedObjectCache);
        this.executable = executable;
        this.parameters = initParameters(executable, sharedObjectCache);
    }

    protected List<AnnotatedParameter<X>> initParameters(E member, SharedObjectCache sharedObjectCache) {
        return BackedAnnotatedParameter.forExecutable(member, this, sharedObjectCache);
    }

    @Override
    public E getJavaMember() {
        return executable;
    }

    @Override
    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return executable.getAnnotation(annotationType);
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return executable;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return executable.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((executable == null) ? 0 : executable.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        BackedAnnotatedCallable<?, ?> other = (BackedAnnotatedCallable<?, ?>) obj;
        if (executable == null) {
            if (other.executable != null) {
                return false;
            }
        } else if (!executable.equals(other.executable)) {
            return false;
        }
        return true;
    }
}
