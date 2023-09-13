/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events.configurator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.util.annotated.ForwardingAnnotatedConstructor;
import org.jboss.weld.util.annotated.ForwardingAnnotatedField;
import org.jboss.weld.util.annotated.ForwardingAnnotatedMethod;
import org.jboss.weld.util.annotated.ForwardingAnnotatedParameter;
import org.jboss.weld.util.annotated.ForwardingAnnotatedType;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
class AnnotatedTypeBuilderImpl<T> {

    private final AnnotatedTypeConfiguratorImpl<T> configurator;

    /**
     *
     * @param configurator
     */
    public AnnotatedTypeBuilderImpl(AnnotatedTypeConfiguratorImpl<T> configurator) {
        this.configurator = configurator;
    }

    public AnnotatedTypeConfigurator<T> configure() {
        return configurator;
    }

    public AnnotatedType<T> build() {
        return new AnnotatedTypeImpl<T>(configurator);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <X>
     */
    static class AnnotatedTypeImpl<X> extends ForwardingAnnotatedType<X> {

        private final Annotations annotations;

        private final AnnotatedType<X> delegate;

        private final Set<AnnotatedMethod<? super X>> methods;

        private final Set<AnnotatedField<? super X>> fields;

        private final Set<AnnotatedConstructor<X>> constructors;

        public AnnotatedTypeImpl(AnnotatedTypeConfiguratorImpl<X> configurator) {
            this.delegate = configurator.getAnnotated();
            this.annotations = new Annotations(configurator);
            this.methods = configurator.getMethods().stream().map(m -> new AnnotatedMethodImpl<>(m))
                    .collect(ImmutableSet.collector());
            this.fields = configurator.getFields().stream().map(f -> new AnnotatedFieldImpl<>(f))
                    .collect(ImmutableSet.collector());
            this.constructors = configurator.getConstructors().stream().map(c -> new AnnotatedConstructorImpl<>(c))
                    .collect(ImmutableSet.collector());
        }

        @Override
        public AnnotatedType<X> delegate() {
            return delegate;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return annotations.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations.get();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotations.isAnnotationPresent(annotationType);
        }

        @Override
        public Set<AnnotatedMethod<? super X>> getMethods() {
            return methods;
        }

        @Override
        public Set<AnnotatedField<? super X>> getFields() {
            return fields;
        }

        @Override
        public Set<AnnotatedConstructor<X>> getConstructors() {
            return constructors;
        }

        @Override
        public String toString() {
            return Formats.formatAnnotatedType(delegate);
        }

    }

    static class AnnotatedMethodImpl<X> extends ForwardingAnnotatedMethod<X> {

        private final Annotations annotations;

        private final AnnotatedMethod<X> delegate;

        private final List<AnnotatedParameter<X>> parameters;

        AnnotatedMethodImpl(AnnotatedMethodConfiguratorImpl<X> configurator) {
            this.delegate = configurator.getAnnotated();
            this.annotations = new Annotations(configurator);
            this.parameters = configurator.getParams().stream().map((c) -> new AnnotatedParameterImpl<X>(c))
                    .collect(ImmutableList.collector());
        }

        @Override
        public AnnotatedMethod<X> delegate() {
            return delegate;
        }

        @Override
        public List<AnnotatedParameter<X>> getParameters() {
            return parameters;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return annotations.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations.get();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotations.isAnnotationPresent(annotationType);
        }

        @Override
        public String toString() {
            return Formats.formatAnnotatedMethod(delegate);
        }

    }

    static class AnnotatedFieldImpl<X> extends ForwardingAnnotatedField<X> {

        private final Annotations annotations;

        private final AnnotatedField<X> delegate;

        AnnotatedFieldImpl(AnnotatedFieldConfiguratorImpl<X> configurator) {
            this.delegate = configurator.getAnnotated();
            this.annotations = new Annotations(configurator);
        }

        @Override
        public AnnotatedField<X> delegate() {
            return delegate;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return annotations.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations.get();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotations.isAnnotationPresent(annotationType);
        }

        @Override
        public String toString() {
            return Formats.formatAnnotatedField(delegate);
        }
    }

    static class AnnotatedConstructorImpl<X> extends ForwardingAnnotatedConstructor<X> {

        private final Annotations annotations;

        private final AnnotatedConstructor<X> delegate;

        private final List<AnnotatedParameter<X>> parameters;

        AnnotatedConstructorImpl(AnnotatedConstructorConfiguratorImpl<X> configurator) {
            this.delegate = configurator.getAnnotated();
            this.annotations = new Annotations(configurator);
            this.parameters = configurator.getParams().stream().map((c) -> new AnnotatedParameterImpl<X>(c))
                    .collect(ImmutableList.collector());
        }

        @Override
        public AnnotatedConstructor<X> delegate() {
            return delegate;
        }

        @Override
        public List<AnnotatedParameter<X>> getParameters() {
            return parameters;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return annotations.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations.get();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotations.isAnnotationPresent(annotationType);
        }

        @Override
        public String toString() {
            return Formats.formatAnnotatedConstructor(delegate);
        }
    }

    static class AnnotatedParameterImpl<X> extends ForwardingAnnotatedParameter<X> {

        private final Annotations annotations;

        private final AnnotatedParameter<X> delegate;

        public AnnotatedParameterImpl(AnnotatedParameterConfiguratorImpl<X> configurator) {
            this.delegate = configurator.getAnnotated();
            this.annotations = new Annotations(configurator);
        }

        @Override
        protected AnnotatedParameter<X> delegate() {
            return delegate;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return annotations.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return annotations.get();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotations.isAnnotationPresent(annotationType);
        }

        @Override
        public String toString() {
            return Formats.formatAnnotatedParameter(delegate);
        }

    }

    private static class Annotations {

        private final Set<Annotation> annotations;

        private final Map<Class<? extends Annotation>, Annotation> annotationsMap;

        private Annotations(AnnotatedConfigurator<?, ?, ?> configurator) {
            this.annotations = ImmutableSet.copyOf(configurator.getAnnotations());
            this.annotationsMap = this.annotations.stream()
                    .collect(ImmutableMap.collector((a) -> a.annotationType(), Function.identity()));
        }

        Set<Annotation> get() {
            return annotations;
        }

        boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return annotationsMap.containsKey(annotationType);
        }

        @SuppressWarnings("unchecked")
        <A extends Annotation> A getAnnotation(Class<A> annotationType) {
            return (A) annotationsMap.get(annotationType);
        }

    }

}
