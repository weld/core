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
package org.jboss.weld.manager;

import static com.google.common.collect.Lists.transform;
import static java.util.Collections.unmodifiableCollection;
import static org.jboss.weld.logging.messages.ValidatorMessage.ALTERNATIVE_BEAN_CLASS_SPECIFIED_MULTIPLE_TIMES;
import static org.jboss.weld.logging.messages.ValidatorMessage.DECORATOR_SPECIFIED_TWICE;
import static org.jboss.weld.logging.messages.ValidatorMessage.INTERCEPTOR_SPECIFIED_TWICE;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.bootstrap.EnabledBuilder;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.logging.messages.ValidatorMessage;

/**
 * @author Nicklas Karlsson
 * @author Ales Justin
 * @author Jozef Hartinger
 *
 * @see EnabledBuilder
 */
public class Enabled {

    public static final Enabled EMPTY_ENABLED = new Enabled(Collections.<Metadata<Class<?>>>emptySet(), Collections.<Metadata<Class<?>>>emptyList(), Collections.<Metadata<Class<?>>>emptyList());

    private final Map<Class<?>, Metadata<Class<?>>> alternatives;
    private final Map<Class<?>, Metadata<Class<?>>> decorators;
    private final Map<Class<?>, Metadata<Class<?>>> interceptors;
    private final Comparator<Decorator<?>> decoratorComparator;
    private final Comparator<Interceptor<?>> interceptorComparator;

    public Enabled(Set<Metadata<Class<?>>> alternatives, List<Metadata<Class<?>>> decorators, List<Metadata<Class<?>>> interceptors) {
        this.alternatives = createMetadataMap(alternatives, ALTERNATIVE_BEAN_CLASS_SPECIFIED_MULTIPLE_TIMES);
        this.decorators = createMetadataMap(decorators, DECORATOR_SPECIFIED_TWICE);
        this.interceptors = createMetadataMap(interceptors, INTERCEPTOR_SPECIFIED_TWICE);
        final List<Class<?>> decoratorTypes = transform(decorators, new RemoveMetadataWrapperFunction<Class<?>>());
        final List<Class<?>> interceptorTypes = transform(interceptors, new RemoveMetadataWrapperFunction<Class<?>>());
        this.decoratorComparator = new Comparator<Decorator<?>>() {

            public int compare(Decorator<?> o1, Decorator<?> o2) {
                int p1 = decoratorTypes.indexOf(o1.getBeanClass());
                int p2 = decoratorTypes.indexOf(o2.getBeanClass());
                return p1 - p2;
            }

        };
        this.interceptorComparator = new Comparator<Interceptor<?>>() {

            public int compare(Interceptor<?> o1, Interceptor<?> o2) {
                int p1 = interceptorTypes.indexOf(o1.getBeanClass());
                int p2 = interceptorTypes.indexOf(o2.getBeanClass());
                return p1 - p2;
            }

        };
    }

    private static <T> Map<T, Metadata<T>> createMetadataMap(Collection<Metadata<T>> metadata, ValidatorMessage specifiedTwiceMessage) {
        Map<T, Metadata<T>> result = new HashMap<T, Metadata<T>>();
        for (Metadata<T> value : metadata) {
            if (result.containsKey(value.getValue())) {
                throw new DeploymentException(specifiedTwiceMessage, metadata);
            }
            result.put(value.getValue(), value);
        }
        return result;
    }

    public Collection<Metadata<Class<?>>> getAlternatives() {
        return unmodifiableCollection(alternatives.values());
    }

    public Metadata<Class<?>> getAlternative(Class<?> clazz) {
        return alternatives.get(clazz);
    }

    public Collection<Metadata<Class<?>>> getDecorators() {
        return unmodifiableCollection(decorators.values());
    }

    public Metadata<Class<?>> getDecorator(Class<?> clazz) {
        return decorators.get(clazz);
    }

    public Collection<Metadata<Class<?>>> getInterceptors() {
        return unmodifiableCollection(interceptors.values());
    }

    public Metadata<Class<?>> getInterceptor(Class<?> clazz) {
        return interceptors.get(clazz);
    }

    public Comparator<Decorator<?>> getDecoratorComparator() {
        return decoratorComparator;
    }

    public Comparator<Interceptor<?>> getInterceptorComparator() {
        return interceptorComparator;
    }

}
