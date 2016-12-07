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
package org.jboss.weld.bootstrap.events.builder;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.configurator.BeanAttributesConfigurator;
import javax.inject.Named;

import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class BeanAttributesBuilderImpl<T> {
    // implements BeanAttributesBuilder<T> {

    static final Set<Annotation> DEFAULT_QUALIFIERS = ImmutableSet.of(Any.Literal.INSTANCE, Default.Literal.INSTANCE);

    private final BeanAttributesConfiguratorImpl<T> configurator;

    /**
     *
     * @param configurator
     */
    public BeanAttributesBuilderImpl(BeanAttributesConfiguratorImpl<T> configurator) {
        this.configurator = configurator;
    }

    // @Override
    public BeanAttributesConfigurator<T> configure() {
        return configurator;
    }

    // @Override
    public BeanAttributes<T> build() {
        return new ImmutableBeanAttributes<T>(ImmutableSet.copyOf(configurator.getStereotypes()), configurator.isAlternative(), configurator.getName(),
                initQualifiers(configurator.getQualifiers()), ImmutableSet.copyOf(configurator.getTypes()), initScope(configurator));
    }

    private Class<? extends Annotation> initScope(BeanAttributesConfiguratorImpl<T> configurator) {
        return configurator.getScope() != null ? configurator.getScope() : Dependent.class;
    }

    private Set<Annotation> initQualifiers(Set<Annotation> qualifiers) {
        if (qualifiers.isEmpty()) {
            return DEFAULT_QUALIFIERS;
        }
        Set<Annotation> normalized = new HashSet<Annotation>(qualifiers);
        normalized.remove(Any.Literal.INSTANCE);
        normalized.remove(Default.Literal.INSTANCE);
        if (normalized.isEmpty()) {
            normalized = DEFAULT_QUALIFIERS;
        } else {
            ImmutableSet.Builder<Annotation> builder = ImmutableSet.builder();
            if (normalized.size() == 1) {
                if (qualifiers.iterator().next().annotationType().equals(Named.class)) {
                    builder.add(Default.Literal.INSTANCE);
                }
            }
            builder.add(Any.Literal.INSTANCE);
            builder.addAll(qualifiers);
            normalized = builder.build();
        }
        return normalized;
    }

}
