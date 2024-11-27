/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.test.util.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.Annotated;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotated;
import org.jboss.weld.util.annotated.ForwardingAnnotated;

/**
 * Provides an abstraction for delegating access to an annotated item
 *
 * @param <T>
 * @param <S>
 * @author Pete Muir
 */
public abstract class ForwardingWeldAnnotated<T, S> extends ForwardingAnnotated implements EnhancedAnnotated<T, S> {

    public Type[] getActualTypeArguments() {
        return delegate().getActualTypeArguments();
    }

    public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType) {
        return delegate().getMetaAnnotations(metaAnnotationType);
    }

    public Set<Annotation> getQualifiers() {
        return delegate().getQualifiers();
    }

    public String getName() {
        return delegate().getName();
    }

    public Class<T> getJavaClass() {
        return delegate().getJavaClass();
    }

    public boolean isFinal() {
        return delegate().isFinal();
    }

    public boolean isStatic() {
        return delegate().isStatic();
    }

    public boolean isGeneric() {
        return delegate().isGeneric();
    }

    public boolean isPublic() {
        return delegate().isPublic();
    }

    public boolean isPrivate() {
        return delegate().isPrivate();
    }

    public boolean isPackagePrivate() {
        return delegate().isPackagePrivate();
    }

    public Package getPackage() {
        return delegate().getPackage();
    }

    @Override
    protected abstract EnhancedAnnotated<T, S> delegate();

    public Set<Type> getInterfaceClosure() {
        return delegate().getInterfaceClosure();
    }

    public boolean isParameterizedType() {
        return delegate().isParameterizedType();
    }

    public boolean isPrimitive() {
        return delegate().isPrimitive();
    }

    @Override
    public Annotated slim() {
        return delegate().slim();
    }
}
