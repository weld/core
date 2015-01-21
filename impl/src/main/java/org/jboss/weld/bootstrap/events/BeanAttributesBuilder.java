/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.literal.DefaultLiteral;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 *
 * @author Martin Kouba
 * @param <T> the class of the bean instance
 * @param <B> the current builder class
 */
public abstract class BeanAttributesBuilder<T, B> {

    protected String name;

    protected Set<Annotation> qualifiers;

    protected Class<? extends Annotation> scope;

    protected Set<Class<? extends Annotation>> stereotypes;

    protected Set<Type> types;

    protected boolean alternative;

    BeanAttributesBuilder() {
        qualifiers = new HashSet<Annotation>();
        qualifiers.add(AnyLiteral.INSTANCE);
        types = new HashSet<Type>();
        types.add(Object.class);
        scope = Dependent.class;
        stereotypes = new HashSet<Class<? extends Annotation>>();
        alternative = false;
    }

    /**
     *
     * @return
     */
    public BeanAttributes<T> build() {
        return new ImmutableBeanAttributes<T>(name, qualifiers, scope, stereotypes, types, alternative);
    }

    public B addType(Type type) {
        this.types.add(type);
        return self();
    }

    public B types(Type... types) {
        this.types = new HashSet<Type>();
        Collections.addAll(this.types, types);
        return self();
    }

    public B types(Set<Type> types) {
        this.types = new HashSet<Type>(types);
        return self();
    }

    public B scope(Class<? extends Annotation> scope) {
        Preconditions.checkArgumentNotNull(scope, "scope");
        this.scope = scope;
        return self();
    }

    public boolean hasScope() {
        return this.scope != null;
    }

    public B addQualifier(Annotation qualifier) {
        if (!qualifier.annotationType().equals(Default.class)) {
            this.qualifiers.remove(DefaultLiteral.INSTANCE);
        }
        this.qualifiers.add(qualifier);
        return self();
    }

    public B qualifiers(Annotation... qualifiers) {
        this.qualifiers = new HashSet<Annotation>();
        Collections.addAll(this.qualifiers, qualifiers);
        return self();
    }

    public B qualifiers(Set<Annotation> qualifiers) {
        this.qualifiers = new HashSet<Annotation>(qualifiers);
        return self();
    }

    public boolean hasQualifiers() {
        return !qualifiers.isEmpty();
    }

    public B addStereotype(Class<? extends Annotation> stereotype) {
        this.stereotypes.add(stereotype);
        return self();
    }

    public B stereotypes(@SuppressWarnings("unchecked") Class<? extends Annotation>... stereotypes) {
        this.stereotypes = new HashSet<Class<? extends Annotation>>();
        Collections.addAll(this.stereotypes, stereotypes);
        return self();
    }

    public B stereotypes(Set<Class<? extends Annotation>> stereotypes) {
        this.stereotypes = new HashSet<Class<? extends Annotation>>(stereotypes);
        return self();
    }

    public B name(String name) {
        Preconditions.checkArgumentNotNull(name, "name");
        this.name = name;
        return self();
    }

    public B alternative(boolean value) {
        this.alternative = value;
        return self();
    }

    protected abstract B self();

    /**
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    static class ImmutableBeanAttributes<T> implements BeanAttributes<T> {

        private final String name;

        private final Set<Annotation> qualifiers;

        private final Class<? extends Annotation> scope;

        private final Set<Class<? extends Annotation>> stereotypes;

        private final Set<Type> types;

        private final boolean alternative;

        /**
         *
         * @param name
         * @param qualifiers
         * @param scope
         * @param stereotypes
         * @param types
         * @param alternative
         */
        ImmutableBeanAttributes(String name, Set<Annotation> qualifiers, Class<? extends Annotation> scope, Set<Class<? extends Annotation>> stereotypes,
                Set<Type> types, boolean alternative) {
            this.name = name;
            this.qualifiers = ImmutableSet.copyOf(qualifiers);
            this.scope = scope;
            this.stereotypes = ImmutableSet.copyOf(stereotypes);
            this.types = ImmutableSet.copyOf(types);
            this.alternative = alternative;
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return scope;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return stereotypes;
        }

        @Override
        public boolean isAlternative() {
            return alternative;
        }

    }

}
