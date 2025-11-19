/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean.attributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.util.reflection.Formats;

/**
 * Implementation of {@link BeanAttributes} used by Weld.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the class of the bean instance
 */
public class ImmutableBeanAttributes<T> implements BeanAttributes<T> {

    private final Set<Class<? extends Annotation>> stereotypes;
    private final boolean alternative;
    private final boolean reserve;
    private final String name;
    private final Set<Annotation> qualifiers;
    private final Set<Type> types;
    private final Class<? extends Annotation> scope;

    public ImmutableBeanAttributes(Set<Class<? extends Annotation>> stereotypes, boolean alternative, boolean reserve,
            String name, Set<Annotation> qualifiers, Set<Type> types, Class<? extends Annotation> scope) {
        this.stereotypes = stereotypes;
        this.alternative = alternative;
        this.reserve = reserve;
        this.name = name;
        this.qualifiers = qualifiers;
        this.types = types;
        this.scope = scope;
    }

    /**
     * Utility constructor used for overriding Bean qualifiers and name for specialization purposes.
     */
    public ImmutableBeanAttributes(Set<Annotation> qualifiers, String name, BeanAttributes<T> attributes) {
        this(attributes.getStereotypes(), attributes.isAlternative(), attributes.isReserve(), name, qualifiers,
                attributes.getTypes(),
                attributes.getScope());
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return stereotypes;
    }

    @Override
    public boolean isAlternative() {
        return alternative;
    }

    @Override
    public boolean isReserve() {
        return reserve;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "BeanAttributes with types [" + Formats.formatTypes(types) + "] and qualifiers ["
                + Formats.formatAnnotations(getQualifiers()) + "]";
    }
}
