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
            throw new DefinitionException(key, objects);
        }
    }
}
