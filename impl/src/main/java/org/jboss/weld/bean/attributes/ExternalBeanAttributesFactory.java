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

import static org.jboss.weld.logging.messages.MetadataMessage.NOT_A_QUALIFIER;
import static org.jboss.weld.logging.messages.MetadataMessage.NOT_A_SCOPE;
import static org.jboss.weld.logging.messages.MetadataMessage.NOT_A_STEREOTYPE;
import static org.jboss.weld.logging.messages.MetadataMessage.QUALIFIERS_NULL;
import static org.jboss.weld.logging.messages.MetadataMessage.SCOPE_NULL;
import static org.jboss.weld.logging.messages.MetadataMessage.STEREOTYPES_NULL;
import static org.jboss.weld.logging.messages.MetadataMessage.TYPES_EMPTY;
import static org.jboss.weld.logging.messages.MetadataMessage.TYPES_NULL;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalArgumentException;

/**
 * Creates {@link BeanAttributes} based on BeanAttributes provided by an extension. This class handles creating a safe copy as
 * well as basic validation of provided {@link BeanAttributes}.
 *
 * @author Jozef Hartinger
 *
 */
public class ExternalBeanAttributesFactory {

    /**
     * @param source source {@link BeanAttributes}.
     * @return a safe copy of source {@link BeanAttributes}.
     */
    public static <T> BeanAttributes<T> of(BeanAttributes<T> source, BeanManager manager) {
        validateBeanAttributes(source, manager);
        BeanAttributes<T> attributes = new ImmutableBeanAttributes<T>(source.isNullable(), defensiveCopy(source.getStereotypes()), source.isAlternative(), source.getName(),
                defensiveCopy(source.getQualifiers()), defensiveCopy(source.getTypes()), source.getScope());
        return attributes;
    }

    private static <T> Set<T> defensiveCopy(Set<T> set) {
        return new HashSet<T>(set);
    }

    /**
     * Validates {@link BeanAttributes}.
     * @param attributes {@link BeanAttributes} to validate
     */
    public static void validateBeanAttributes(BeanAttributes<?> attributes, BeanManager manager) {
        validateStereotypes(attributes, manager);
        validateQualifiers(attributes, manager);
        validateTypes(attributes, manager);
        validateScope(attributes, manager);
    }

    public static void validateStereotypes(BeanAttributes<?> attributes, BeanManager manager) {
        checkNull(attributes.getStereotypes(), STEREOTYPES_NULL, attributes);
        for (Class<? extends Annotation> annotation : attributes.getStereotypes()) {
            if (!manager.isStereotype(annotation)) {
                throw new DefinitionException(NOT_A_STEREOTYPE, annotation, attributes);
            }
        }
    }

    public static void validateQualifiers(BeanAttributes<?> attributes, BeanManager manager) {
        checkNull(attributes.getQualifiers(), QUALIFIERS_NULL, attributes);
        for (Annotation annotation : attributes.getQualifiers()) {
            if (!manager.isQualifier(annotation.annotationType())) {
                throw new DefinitionException(NOT_A_QUALIFIER, annotation.annotationType(), attributes);
            }
        }
    }

    public static void validateTypes(BeanAttributes<?> attributes, BeanManager manager) {
        checkNull(attributes.getTypes(), TYPES_NULL, attributes);
        if (attributes.getTypes().isEmpty()) {
            throw new DefinitionException(TYPES_EMPTY, attributes);
        }
    }

    public static void validateScope(BeanAttributes<?> attributes, BeanManager manager) {
        checkNull(attributes.getScope(), SCOPE_NULL, attributes);
        if (!manager.isScope(attributes.getScope())) {
            throw new DefinitionException(NOT_A_SCOPE, attributes.getScope(), attributes);
        }
    }

    public static <E extends Enum<?>> void checkNull(Object object, E key, Object... objects) {
        if (object == null) {
            throw new IllegalArgumentException(key, objects);
        }
    }
}
