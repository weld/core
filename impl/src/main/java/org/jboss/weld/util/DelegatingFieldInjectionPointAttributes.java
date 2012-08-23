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
package org.jboss.weld.util;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.injection.attributes.FieldInjectionPointAttributes;
import org.jboss.weld.injection.attributes.ForwardingFieldInjectionPointAttributes;

/**
 * Forwarding implementation of {@link FieldInjectionPointAttributes}. A different name was chosen since
 * {@link ForwardingFieldInjectionPointAttributes} already exists with different semantics.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 * @param <X>
 */
public abstract class DelegatingFieldInjectionPointAttributes<T, X> implements FieldInjectionPointAttributes<T, X> {

    protected abstract FieldInjectionPointAttributes<T, X> delegate();

    @Override
    public <A extends Annotation> A getQualifier(Class<A> annotationType) {
        return delegate().getQualifier(annotationType);
    }

    @Override
    public Type getType() {
        return delegate().getType();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return delegate().getQualifiers();
    }

    @Override
    public Bean<?> getBean() {
        return delegate().getBean();
    }

    @Override
    public Member getMember() {
        return delegate().getMember();
    }

    @Override
    public boolean isDelegate() {
        return delegate().isDelegate();
    }

    @Override
    public boolean isTransient() {
        return delegate().isTransient();
    }

    @Override
    public AnnotatedField<X> getAnnotated() {
        return delegate().getAnnotated();
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DelegatingFieldInjectionPointAttributes<?, ?>) {
            DelegatingFieldInjectionPointAttributes<?, ?> they = cast(obj);
            return delegate().equals(they.delegate());
        }
        return delegate().equals(obj);
    }

}
