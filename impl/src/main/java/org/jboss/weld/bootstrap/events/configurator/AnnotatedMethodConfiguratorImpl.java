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

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator;

/**
 * Configurator for {@link AnnotatedMethod}.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class AnnotatedMethodConfiguratorImpl<T>
        extends AnnotatedCallableConfigurator<T, AnnotatedMethod<T>, AnnotatedMethodConfiguratorImpl<T>>
        implements AnnotatedMethodConfigurator<T> {

    /**
     *
     * @param annotatedMethod
     * @return
     */
    static <X> AnnotatedMethodConfiguratorImpl<X> from(AnnotatedMethod<X> annotatedMethod) {
        return new AnnotatedMethodConfiguratorImpl<>(annotatedMethod);
    }

    /**
     *
     * @param annotatedMethod
     */
    private AnnotatedMethodConfiguratorImpl(AnnotatedMethod<T> annotatedMethod) {
        super(annotatedMethod);
    }

    @Override
    public List<AnnotatedParameterConfigurator<T>> params() {
        return cast(params);
    }

    @Override
    protected AnnotatedMethodConfiguratorImpl<T> self() {
        return this;
    }

}
