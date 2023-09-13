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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Configurator for {@link AnnotatedType}.
 *
 * @author Martin Kouba
 */
public class AnnotatedTypeConfiguratorImpl<T>
        extends AnnotatedConfigurator<T, AnnotatedType<T>, AnnotatedTypeConfiguratorImpl<T>>
        implements AnnotatedTypeConfigurator<T>, Configurator<AnnotatedType<T>> {

    private final Set<AnnotatedMethodConfiguratorImpl<? super T>> methods;

    private final Set<AnnotatedFieldConfiguratorImpl<? super T>> fields;

    private final Set<AnnotatedConstructorConfiguratorImpl<T>> constructors;

    public AnnotatedTypeConfiguratorImpl(AnnotatedType<T> annotatedType) {
        super(annotatedType);
        this.constructors = annotatedType.getConstructors().stream().map(c -> AnnotatedConstructorConfiguratorImpl.from(c))
                .collect(ImmutableSet.collector());
        this.methods = annotatedType.getMethods().stream().map(m -> AnnotatedMethodConfiguratorImpl.from(m))
                .collect(ImmutableSet.collector());
        this.fields = annotatedType.getFields().stream().map(f -> AnnotatedFieldConfiguratorImpl.from(f))
                .collect(ImmutableSet.collector());
    }

    @Override
    public Set<AnnotatedMethodConfigurator<? super T>> methods() {
        return cast(methods);
    }

    @Override
    public Set<AnnotatedFieldConfigurator<? super T>> fields() {
        return cast(fields);
    }

    @Override
    public Set<AnnotatedConstructorConfigurator<T>> constructors() {
        return cast(constructors);
    }

    @Override
    protected AnnotatedTypeConfiguratorImpl<T> self() {
        return this;
    }

    @Override
    public AnnotatedType<T> complete() {
        return new AnnotatedTypeBuilderImpl<>(this).build();
    }

    Set<AnnotatedMethodConfiguratorImpl<? super T>> getMethods() {
        return methods;
    }

    Set<AnnotatedFieldConfiguratorImpl<? super T>> getFields() {
        return fields;
    }

    Set<AnnotatedConstructorConfiguratorImpl<T>> getConstructors() {
        return constructors;
    }

}
