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

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.configurator.InjectionPointConfigurator;

/**
 *
 * @author Martin Kouba
 */
public class InjectionPointConfiguratorImpl implements InjectionPointConfigurator, Configurator<InjectionPoint> {

    private Type requiredType;

    private final Set<Annotation> qualifiers;

    private Bean<?> bean;

    private boolean isDelegate;

    private boolean isTransient;

    private Member member;

    private Annotated annotated;

    public InjectionPointConfiguratorImpl() {
        this.qualifiers = new HashSet<>();
    }

    public InjectionPointConfiguratorImpl(InjectionPoint injectionPoint) {
        this();
        read(injectionPoint);
    }

    public InjectionPointConfigurator read(InjectionPoint injectionPoint) {
        bean = injectionPoint.getBean();
        type(injectionPoint.getType());
        qualifiers(injectionPoint.getQualifiers());
        delegate(injectionPoint.isDelegate());
        transientField(injectionPoint.isTransient());
        member(injectionPoint.getMember());
        annotated(injectionPoint.getAnnotated());
        return this;
    }

    @Override
    public InjectionPointConfigurator type(Type type) {
        checkArgumentNotNull(type);
        this.requiredType = type;
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifier(Annotation qualifier) {
        checkArgumentNotNull(qualifier);
        qualifiers.remove(Default.Literal.INSTANCE);
        qualifiers.add(qualifier);
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifiers(Annotation... qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.qualifiers.remove(Default.Literal.INSTANCE);
        Collections.addAll(this.qualifiers, qualifiers);
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifiers(Set<Annotation> qualifiers) {
        checkArgumentNotNull(qualifiers);
        this.qualifiers.remove(Default.Literal.INSTANCE);
        this.qualifiers.addAll(qualifiers);
        return this;
    }

    @Override
    public InjectionPointConfigurator qualifiers(Annotation... qualifiers) {
        this.qualifiers.clear();
        return addQualifiers(qualifiers);
    }

    @Override
    public InjectionPointConfigurator qualifiers(Set<Annotation> qualifiers) {
        this.qualifiers.clear();
        return addQualifiers(qualifiers);
    }

    @Override
    public InjectionPointConfigurator delegate(boolean delegate) {
        this.isDelegate = delegate;
        return this;
    }

    @Override
    public InjectionPointConfigurator transientField(boolean trans) {
        this.isTransient = trans;
        return this;
    }

    public InjectionPointConfigurator member(Member member) {
        this.member = member;
        return this;
    }

    public InjectionPointConfigurator annotated(Annotated annotated) {
        this.annotated = annotated;
        return this;
    }

    @Override
    public InjectionPoint complete() {
        return new ImmutableInjectionPoint(this);
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
            this.requiredType = configurator.requiredType;
            this.qualifiers = configurator.qualifiers;
            this.bean = configurator.bean;
            this.isDelegate = configurator.isDelegate;
            this.isTransient = configurator.isTransient;
            this.member = configurator.member;
            this.annotated = configurator.annotated;
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

        @Override
        public String toString() {
            return "InjectionPoint with type=" + requiredType + ", qualifiers=" + qualifiers +
                    ", delegate=" + isDelegate + ", transient=" + isTransient + ".";
        }
    }
}
