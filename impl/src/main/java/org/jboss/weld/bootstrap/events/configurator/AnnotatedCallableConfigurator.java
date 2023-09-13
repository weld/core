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

import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedCallable;

import org.jboss.weld.util.collections.ImmutableList;

/**
 * An abstract configurator for annotated elements representing callable members.
 *
 * @author Martin Kouba
 *
 * @param <T>
 * @param <A>
 * @param <C>
 */
abstract class AnnotatedCallableConfigurator<T, A extends AnnotatedCallable<T>, C extends AnnotatedCallableConfigurator<T, A, C>>
        extends AnnotatedConfigurator<T, A, C> {

    protected final List<AnnotatedParameterConfiguratorImpl<T>> params;

    AnnotatedCallableConfigurator(A annotatedCallable) {
        super(annotatedCallable);
        this.params = annotatedCallable.getParameters().stream().map(p -> AnnotatedParameterConfiguratorImpl.from(p))
                .collect(ImmutableList.collector());
    }

    List<AnnotatedParameterConfiguratorImpl<T>> getParams() {
        return params;
    }

}
