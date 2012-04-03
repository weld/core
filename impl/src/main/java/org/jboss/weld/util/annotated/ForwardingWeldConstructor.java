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
package org.jboss.weld.util.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.ConstructorSignature;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;

public abstract class ForwardingWeldConstructor<T> extends ForwardingWeldMember<T, T, Constructor<T>> implements EnhancedAnnotatedConstructor<T> {

    @Override
    protected abstract EnhancedAnnotatedConstructor<T> delegate();

    public List<EnhancedAnnotatedParameter<?, T>> getEnhancedParameters(Class<? extends Annotation> annotationType) {
        return delegate().getEnhancedParameters(annotationType);
    }

    @Override
    public EnhancedAnnotatedType<T> getDeclaringType() {
        return delegate().getDeclaringType();
    }

    public List<? extends EnhancedAnnotatedParameter<?, T>> getEnhancedParameters() {
        return delegate().getEnhancedParameters();
    }

    public ConstructorSignature getSignature() {
        return delegate().getSignature();
    }

    public List<AnnotatedParameter<T>> getParameters() {
        return delegate().getParameters();
    }

    public Constructor<T> getJavaMember() {
        return delegate().getJavaMember();
    }

    @Override
    public AnnotatedConstructor<T> slim() {
        return delegate().slim();
    }
}
