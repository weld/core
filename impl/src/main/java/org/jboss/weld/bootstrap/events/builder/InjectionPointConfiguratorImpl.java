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
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Delegate;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.builder.InjectionPointConfigurator;

import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * @author Martin Kouba
 */
public class InjectionPointConfiguratorImpl implements InjectionPointConfigurator {

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

    @Override
    public InjectionPointConfigurator read(Field field) {
        member(field);
        transientField(Reflections.isTransient(field));
        delegate(field.isAnnotationPresent(Delegate.class));
        qualifiers(Configurators.getQualifiers(field));
        type(field.getType());
        return this;
    }

    @Override
    public InjectionPointConfigurator read(Parameter param) {
        Executable executable = param.getDeclaringExecutable();
        member(executable);
        transientField(Reflections.isTransient(executable));
        delegate(executable.isAnnotationPresent(Delegate.class));
        qualifiers(Configurators.getQualifiers(executable));
        type(executable instanceof Method ? ((Method)executable).getReturnType() : param.getType());
        return this;
    }

    @Override
    public InjectionPointConfigurator read(AnnotatedField<?> field) {
        annotated(field);
        member(field.getJavaMember());
        transientField(Reflections.isTransient(field.getJavaMember()));
        delegate(field.isAnnotationPresent(Delegate.class));
        // TODO Special handling for AbstractEnhancedAnnotated
        qualifiers(Configurators.getQualifiers(field));
        type(field.getBaseType());
        return this;
    }

    @Override
    public InjectionPointConfigurator read(AnnotatedParameter<?> param) {
        annotated(param);
        member(param.getDeclaringCallable().getJavaMember());
        transientField(false);
        delegate(param.isAnnotationPresent(Delegate.class));
        // TODO Special handling for AbstractEnhancedAnnotated
        qualifiers(Configurators.getQualifiers(param));
        type(param.getBaseType());
        return this;
    }

    @Override
    public InjectionPointConfigurator read(InjectionPoint injectionPoint) {
        bean(injectionPoint.getBean());
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
        this.requiredType = type;
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifier(Annotation qualifier) {
        qualifiers.remove(Default.Literal.INSTANCE);
        qualifiers.add(qualifier);
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifiers(Annotation... qualifiers) {
        this.qualifiers.remove(Default.Literal.INSTANCE);
        Collections.addAll(this.qualifiers, qualifiers);
        return this;
    }

    @Override
    public InjectionPointConfigurator addQualifiers(Set<Annotation> qualifiers) {
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
    public InjectionPointConfigurator bean(Bean<?> bean) {
        this.bean = bean;
        return this;
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

    /**
     * @return the requiredType
     */
    Type getRequiredType() {
        return requiredType;
    }

    /**
     * @return the qualifiers
     */
    Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    /**
     * @return the bean
     */
    Bean<?> getBean() {
        return bean;
    }

    /**
     * @return the isDelegate
     */
    boolean isDelegate() {
        return isDelegate;
    }

    /**
     * @return the isTransient
     */
    boolean isTransient() {
        return isTransient;
    }

    /**
     * @return the member
     */
    Member getMember() {
        return member;
    }

    /**
     * @return the annotated
     */
    Annotated getAnnotated() {
        return annotated;
    }

}
