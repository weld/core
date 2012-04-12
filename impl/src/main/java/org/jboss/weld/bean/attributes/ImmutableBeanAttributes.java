/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.bean.attributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.util.reflection.Formats;

/**
 * Implementation of {@link BeanAttributes} used by Weld.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the class of the bean instance
 */
public class ImmutableBeanAttributes<T> implements BeanAttributes<T> {

    private final boolean nullable;
    private final Set<Class<? extends Annotation>> stereotypes;
    private final boolean alternative;
    private final String name;
    private final Set<Annotation> qualifiers;
    private final Set<Type> types;
    private final Class<? extends Annotation> scope;

    public ImmutableBeanAttributes(boolean nullable, Set<Class<? extends Annotation>> stereotypes, boolean alternative, String name, Set<Annotation> qualifiers, Set<Type> types,
            Class<? extends Annotation> scope) {
        this.nullable = nullable;
        this.stereotypes = stereotypes;
        this.alternative = alternative;
        this.name = name;
        this.qualifiers = qualifiers;
        this.types = types;
        this.scope = scope;
    }

    /**
     * Utility constructor used for overriding Bean qualifiers and name for specialization purposes.
     */
    public ImmutableBeanAttributes(Set<Annotation> qualifiers, String name, BeanAttributes<T> attributes) {
        this(attributes.isNullable(), attributes.getStereotypes(), attributes.isAlternative(), name, qualifiers, attributes.getTypes(), attributes.getScope());
    }

    @Override
    public boolean isNullable() {
        return nullable;
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
        return "BeanAttributes with types [" + Formats.formatTypes(types) + "] and qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "]";
    }
}
