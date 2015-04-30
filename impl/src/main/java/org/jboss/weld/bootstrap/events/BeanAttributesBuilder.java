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
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
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

    private static final String ARG_SCOPE = "scope";
    private static final String ARG_NAME = "name";
    private static final String ARG_TYPES = "types";
    private static final String ARG_TYPE = "type";
    private static final String ARG_QUALIFIERS = "qualifiers";
    private static final String ARG_QUALIFIER = "qualifier";
    private static final String ARG_STEREOTYPES = "stereotypes";
    private static final String ARG_STEREOTYPE = "stereotype";

    protected String name;

    protected Set<Annotation> qualifiers;

    protected Class<? extends Annotation> scope;

    protected Set<Class<? extends Annotation>> stereotypes;

    protected Set<Type> types;

    protected Boolean alternative;

    BeanAttributesBuilder() {
        qualifiers = new HashSet<Annotation>();
        qualifiers.add(AnyLiteral.INSTANCE);
        types = new HashSet<Type>();
        types.add(Object.class);
        stereotypes = new HashSet<Class<? extends Annotation>>();
    }

    /**
     *
     * @return the bean attributes
     */
    public BeanAttributes<T> build() {
        return new ImmutableBeanAttributes<T>(ImmutableSet.copyOf(stereotypes), alternative != null ? alternative : false, name,
                ImmutableSet.copyOf(qualifiers), ImmutableSet.copyOf(types), scope != null ? scope : Dependent.class);
    }

    public B addType(Type type) {
        Preconditions.checkArgumentNotNull(type, ARG_TYPE);
        this.types.add(type);
        return self();
    }

    public B addTypes(Type... types) {
        Preconditions.checkArgumentNotNull(types, ARG_TYPES);
        Collections.addAll(this.types, types);
        return self();
    }

    public B addTypes(Set<Type> types) {
        Preconditions.checkArgumentNotNull(types, ARG_TYPES);
        this.types.addAll(types);
        return self();
    }

    public B types(Type... types) {
        Preconditions.checkArgumentNotNull(types, ARG_TYPES);
        this.types.clear();
        Collections.addAll(this.types, types);
        return self();
    }

    public B types(Set<Type> types) {
        Preconditions.checkArgumentNotNull(types, ARG_TYPES);
        this.types.clear();
        this.types.addAll(types);
        return self();
    }

    public B scope(Class<? extends Annotation> scope) {
        Preconditions.checkArgumentNotNull(scope, ARG_SCOPE);
        this.scope = scope;
        return self();
    }

    public boolean hasScope() {
        return this.scope != null;
    }

    public B addQualifier(Annotation qualifier) {
        Preconditions.checkArgumentNotNull(qualifier, ARG_QUALIFIER);
        this.qualifiers.remove(DefaultLiteral.INSTANCE);
        this.qualifiers.add(qualifier);
        return self();
    }

    public B addQualifiers(Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(qualifiers, ARG_QUALIFIERS);
        this.qualifiers.remove(DefaultLiteral.INSTANCE);
        Collections.addAll(this.qualifiers, qualifiers);
        return self();
    }

    public B addQualifiers(Set<Annotation> qualifiers) {
        Preconditions.checkArgumentNotNull(qualifiers, ARG_QUALIFIERS);
        this.qualifiers.remove(DefaultLiteral.INSTANCE);
        this.qualifiers.addAll(qualifiers);
        return self();
    }

    public B qualifiers(Annotation... qualifiers) {
        Preconditions.checkArgumentNotNull(qualifiers, ARG_QUALIFIERS);
        this.qualifiers.clear();
        Collections.addAll(this.qualifiers, qualifiers);
        return self();
    }

    public B qualifiers(Set<Annotation> qualifiers) {
        Preconditions.checkArgumentNotNull(qualifiers, ARG_QUALIFIERS);
        this.qualifiers.clear();
        this.qualifiers.addAll(qualifiers);
        return self();
    }

    public boolean hasQualifiers() {
        return !qualifiers.isEmpty();
    }

    public B addStereotype(Class<? extends Annotation> stereotype) {
        Preconditions.checkArgumentNotNull(stereotype, ARG_STEREOTYPE);
        this.stereotypes.add(stereotype);
        return self();
    }

    public B addStereotypes(Set<Class<? extends Annotation>> stereotypes) {
        Preconditions.checkArgumentNotNull(stereotypes, ARG_STEREOTYPES);
        this.stereotypes.addAll(stereotypes);
        return self();
    }

    public B stereotypes(Set<Class<? extends Annotation>> stereotypes) {
        Preconditions.checkArgumentNotNull(stereotypes, ARG_STEREOTYPES);
        this.stereotypes.clear();
        this.stereotypes.addAll(stereotypes);
        return self();
    }

    public B name(String name) {
        Preconditions.checkArgumentNotNull(name, ARG_NAME);
        this.name = name;
        return self();
    }

    public B alternative() {
        return alternative(true);
    }

    public B alternative(boolean value) {
        this.alternative = value;
        return self();
    }

    protected abstract B self();

}
