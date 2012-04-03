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
import java.lang.reflect.Method;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedParameter;
import org.jboss.weld.annotated.enhanced.MethodSignature;

public abstract class ForwardingWeldMethod<T, X> extends ForwardingWeldMember<T, X, Method> implements EnhancedAnnotatedMethod<T, X> {

    @Override
    protected abstract EnhancedAnnotatedMethod<T, X> delegate();

    public List<EnhancedAnnotatedParameter<?, X>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType) {
        return delegate().getEnhancedParameters(metaAnnotationType);
    }

    public Class<?>[] getParameterTypesAsArray() {
        return delegate().getParameterTypesAsArray();
    }

    public List<? extends EnhancedAnnotatedParameter<?, X>> getEnhancedParameters() {
        return delegate().getEnhancedParameters();
    }

    public String getPropertyName() {
        return delegate().getPropertyName();
    }

    public boolean isEquivalent(Method method) {
        return delegate().isEquivalent(method);
    }

    public MethodSignature getSignature() {
        return delegate().getSignature();
    }

    public List<EnhancedAnnotatedParameter<?, X>> getEnhancedParameters(Class<? extends Annotation> metaAnnotationType) {
        return delegate().getEnhancedParameters(metaAnnotationType);
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return delegate().getParameters();
    }

    public Method getJavaMember() {
        return delegate().getJavaMember();
    }

    @Override
    public AnnotatedMethod<X> slim() {
        return delegate().slim();
    }
}
