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
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.builder.InjectionPointConfigurator;

import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 */
public class InjectionPointBuilderImpl {
    // implements InjectionPointBuilder {

    private final InjectionPointConfiguratorImpl configurator;

    /**
     *
     * @param configurator
     */
    public InjectionPointBuilderImpl(InjectionPointConfiguratorImpl configurator) {
        this.configurator = configurator;
    }

    // @Override
    public InjectionPointConfigurator configure() {
        return configurator;
    }

    // @Override
    public InjectionPoint build() {
        return new ImmutableInjectionPoint(configurator);
    }

    /**
     *
     * @author Martin Kouba
     */
    static class ImmutableInjectionPoint implements InjectionPoint {

        private final Type requiredType;

        private final Set<Annotation> qualifiers;

        private final Bean<?> bean;

        private final boolean isDelegate;

        private final boolean isTransient;

        private final Member member;

        private final Annotated annotated;

        /**
         *
         * @param configurator
         */
        private ImmutableInjectionPoint(InjectionPointConfiguratorImpl configurator) {
            this.requiredType = configurator.getRequiredType();
            this.qualifiers = ImmutableSet.copyOf(configurator.getQualifiers());
            this.bean = configurator.getBean();
            this.isDelegate = configurator.isDelegate();
            this.isTransient = configurator.isTransient();
            this.member = configurator.getMember();
            this.annotated = configurator.getAnnotated();
        }

        @Override
        public Type getType() {
            return requiredType;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public Bean<?> getBean() {
            return bean;
        }

        @Override
        public Member getMember() {
            return member;
        }

        @Override
        public Annotated getAnnotated() {
            return annotated;
        }

        @Override
        public boolean isDelegate() {
            return isDelegate;
        }

        @Override
        public boolean isTransient() {
            return isTransient;
        }

    }

}
