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

import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator;

/**
 * Configurator for {@link AnnotatedConstructor}.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class AnnotatedConstructorConfiguratorImpl<T>
        extends AnnotatedCallableConfigurator<T, AnnotatedConstructor<T>, AnnotatedConstructorConfiguratorImpl<T>>
        implements AnnotatedConstructorConfigurator<T> {

    /**
     *
     * @param annotatedMethod
     * @return
     */
    static <X> AnnotatedConstructorConfiguratorImpl<X> from(AnnotatedConstructor<X> annotatedConstructor) {
        return new AnnotatedConstructorConfiguratorImpl<>(annotatedConstructor);
    }

    /**
     *
     * @param annotatedMethod
     */
    private AnnotatedConstructorConfiguratorImpl(AnnotatedConstructor<T> annotatedConstructor) {
        super(annotatedConstructor);
    }

    @Override
    public List<AnnotatedParameterConfigurator<T>> params() {
        return cast(params);
    }

    @Override
    protected AnnotatedConstructorConfiguratorImpl<T> self() {
        return this;
    }

}
