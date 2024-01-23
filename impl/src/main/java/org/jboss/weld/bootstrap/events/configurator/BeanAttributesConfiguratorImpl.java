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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.configurator.BeanAttributesConfigurator;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Named;

import org.jboss.weld.bean.attributes.ImmutableBeanAttributes;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.BeanManagerLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.metadata.cache.StereotypeModel;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

/**
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class BeanAttributesConfiguratorImpl<T> implements BeanAttributesConfigurator<T>, Configurator<BeanAttributes<T>> {

    private final BeanManagerImpl beanManager;

    private String name;

    final Set<Annotation> qualifiers;

    private Class<? extends Annotation> scope;

    private final Set<Class<? extends Annotation>> stereotypes;

    final Set<Type> types;

    private boolean isAlternative;

    public BeanAttributesConfiguratorImpl(BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.qualifiers = new HashSet<Annotation>();
        this.types = new HashSet<Type>();
        this.types.add(Object.class);
        this.stereotypes = new HashSet<Class<? extends Annotation>>();
    }

    /**
     *
     * @param beanAttributes
     */
    public BeanAttributesConfiguratorImpl(BeanAttributes<T> beanAttributes, BeanManagerImpl beanManager) {
        this(beanManager);
        read(beanAttributes);
    }

    public BeanAttributesConfigurator<T> read(BeanAttributes<?> beanAttributes) {
        checkArgumentNotNull(beanAttributes);
        name(beanAttributes.getName());
        qualifiers(beanAttributes.getQualifiers());
        scope(beanAttributes.getScope());
        stereotypes(beanAttributes.getStereotypes());
        types(beanAttributes.getTypes());
        alternative(beanAttributes.isAlternative());
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addType(Type type) {
        checkArgumentNotNull(type);
        this.types.add(type);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addType(TypeLiteral<?> typeLiteral) {
        checkArgumentNotNull(typeLiteral);
        this.types.add(typeLiteral.getType());
        return null;
    }

    @Override
    public BeanAttributesConfigurator<T> addTypes(Type... types) {
        checkArgumentNotNull(types);
        Collections.addAll(this.types, types);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addTypes(Set<Type> types) {
        checkArgumentNotNull(types);
        this.types.addAll(types);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addTransitiveTypeClosure(Type type) {
        checkArgumentNotNull(type);
        this.types.addAll(Beans.getLegalBeanTypes(new HierarchyDiscovery(type).getTypeClosure(), type));
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> types(Type... types) {
        this.types.clear();
        return addTypes(types);
    }

    @Override
    public BeanAttributesConfigurator<T> types(Set<Type> types) {
        this.types.clear();
        return addTypes(types);
    }

    @Override
    public BeanAttributesConfigurator<T> scope(Class<? extends Annotation> scope) {
        checkArgumentNotNull(scope);
        this.scope = scope;
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addQualifier(Annotation qualifier) {
        checkArgumentNotNull(qualifier);
        removeDefaultQualifierIfNeeded(qualifier);
        qualifiers.add(qualifier);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addQualifiers(Annotation... qualifiers) {
        checkArgumentNotNull(qualifiers);
        for (Annotation annotation : qualifiers) {
            removeDefaultQualifierIfNeeded(annotation);
        }
        Collections.addAll(this.qualifiers, qualifiers);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addQualifiers(Set<Annotation> qualifiers) {
        checkArgumentNotNull(qualifiers);
        for (Annotation annotation : qualifiers) {
            removeDefaultQualifierIfNeeded(annotation);
        }
        this.qualifiers.addAll(qualifiers);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> qualifiers(Annotation... qualifiers) {
        this.qualifiers.clear();
        return addQualifiers(qualifiers);
    }

    @Override
    public BeanAttributesConfigurator<T> qualifiers(Set<Annotation> qualifiers) {
        this.qualifiers.clear();
        return addQualifiers(qualifiers);
    }

    @Override
    public BeanAttributesConfigurator<T> addStereotype(Class<? extends Annotation> stereotype) {
        checkArgumentNotNull(stereotype);
        this.stereotypes.add(stereotype);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> addStereotypes(Set<Class<? extends Annotation>> stereotypes) {
        checkArgumentNotNull(stereotypes);
        this.stereotypes.addAll(stereotypes);
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> stereotypes(Set<Class<? extends Annotation>> stereotypes) {
        this.stereotypes.clear();
        return addStereotypes(stereotypes);
    }

    @Override
    public BeanAttributesConfigurator<T> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public BeanAttributesConfigurator<T> alternative(boolean alternative) {
        this.isAlternative = alternative;
        return this;
    }

    @Override
    public BeanAttributes<T> complete() {
        return new ImmutableBeanAttributes<T>(ImmutableSet.copyOf(stereotypes), isAlternative, name,
                Bindings.normalizeBeanQualifiers(qualifiers),
                ImmutableSet.copyOf(types),
                initScope());
    }

    private void removeDefaultQualifierIfNeeded(Annotation qualifier) {
        if (!qualifier.annotationType().equals(Named.class)) {
            qualifiers.remove(Default.Literal.INSTANCE);
        }
    }

    private Class<? extends Annotation> initScope() {
        if (scope != null) {
            return scope;
        }
        if (!stereotypes.isEmpty()) {
            MetaAnnotationStore metaAnnotationStore = beanManager.getServices().get(MetaAnnotationStore.class);
            Set<Annotation> possibleScopeTypes = new HashSet<>();
            for (Class<? extends Annotation> stereotype : stereotypes) {
                StereotypeModel<? extends Annotation> model = metaAnnotationStore.getStereotype(stereotype);
                if (model.isValid()) {
                    possibleScopeTypes.add(model.getDefaultScopeType());
                } else {
                    throw BeanManagerLogger.LOG.notStereotype(stereotype);
                }
            }
            if (possibleScopeTypes.size() == 1) {
                return possibleScopeTypes.iterator().next().annotationType();
            } else {
                throw BeanLogger.LOG.multipleScopesFoundFromStereotypes(BeanAttributesConfigurator.class.getSimpleName(),
                        Formats.formatTypes(stereotypes, false), possibleScopeTypes, "");
            }
        }
        return Dependent.class;
    }

}
