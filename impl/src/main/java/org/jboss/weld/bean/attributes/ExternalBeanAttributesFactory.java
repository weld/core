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
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.util.Bindings;

/**
 * Creates {@link BeanAttributes} based on BeanAttributes provided by an extension. This class handles creating a safe copy as
 * well as basic validation of provided {@link BeanAttributes}.
 *
 * @author Jozef Hartinger
 * @author Marko Luksa
 *
 */
public class ExternalBeanAttributesFactory {

    private ExternalBeanAttributesFactory() {
    }

    /**
     * @param source source {@link BeanAttributes}.
     * @return a safe copy of source {@link BeanAttributes}.
     */
    public static <T> BeanAttributes<T> of(BeanAttributes<T> source, BeanManager manager) {
        validateBeanAttributes(source, manager);
        BeanAttributes<T> attributes = new ImmutableBeanAttributes<T>(defensiveCopy(source.getStereotypes()),
                source.isAlternative(), source.getName(),
                defensiveCopy(source.getQualifiers()), defensiveCopy(source.getTypes()), source.getScope());
        return attributes;
    }

    private static <T> Set<T> defensiveCopy(Set<T> set) {
        return new HashSet<T>(set);
    }

    /**
     * Validates {@link BeanAttributes}.
     *
     * @param attributes {@link BeanAttributes} to validate
     */
    public static void validateBeanAttributes(BeanAttributes<?> attributes, BeanManager manager) {
        validateStereotypes(attributes, manager);
        validateQualifiers(attributes, manager);
        validateTypes(attributes, manager);
        validateScope(attributes, manager);
    }

    public static void validateStereotypes(BeanAttributes<?> attributes, BeanManager manager) {
        if (attributes.getStereotypes() == null) {
            throw MetadataLogger.LOG.stereotypesNull(attributes);
        }
        for (Class<? extends Annotation> annotation : attributes.getStereotypes()) {
            if (!manager.isStereotype(annotation)) {
                throw MetadataLogger.LOG.notAStereotype(annotation, attributes);
            }
        }
    }

    public static void validateQualifiers(BeanAttributes<?> attributes, BeanManager manager) {
        Set<Annotation> qualifiers = attributes.getQualifiers();
        Bindings.validateQualifiers(qualifiers, manager, attributes, "BeanAttributes.getQualifiers");
    }

    public static void validateTypes(BeanAttributes<?> attributes, BeanManager manager) {
        if (attributes.getTypes() == null) {
            throw MetadataLogger.LOG.typesNull(attributes);
        }
        if (attributes.getTypes().isEmpty()) {
            throw MetadataLogger.LOG.typesEmpty(attributes);
        }
        for (Type type : attributes.getTypes()) {
            validateBeanType(type, attributes);
        }
    }

    private static void validateBeanType(Type type, BeanAttributes<?> attributes) {
        checkBeanTypeNotATypeVariable(type, type, attributes);
        checkBeanTypeForWildcardsAndTypeVariables(type, type, attributes);
    }

    private static void checkBeanTypeNotATypeVariable(Type beanType, Type type, BeanAttributes<?> attributes) {
        if (type instanceof TypeVariable<?>) {
            throw MetadataLogger.LOG.typeVariableIsNotAValidBeanType(beanType, attributes);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            checkBeanTypeNotATypeVariable(beanType, arrayType.getGenericComponentType(), attributes);
        }
    }

    private static void checkBeanTypeForWildcardsAndTypeVariables(Type beanType, Type type, BeanAttributes<?> attributes) {
        if (type instanceof TypeVariable<?>) {
            if (!attributes.getScope().equals(Dependent.class)) {
                throw MetadataLogger.LOG.beanWithParameterizedTypeContainingTypeVariablesMustBeDependentScoped(beanType,
                        attributes);
            }
        } else if (type instanceof WildcardType) {
            throw MetadataLogger.LOG.parameterizedTypeContainingWildcardParameterIsNotAValidBeanType(beanType, attributes);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                checkBeanTypeForWildcardsAndTypeVariables(beanType, typeArgument, attributes);
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            checkBeanTypeForWildcardsAndTypeVariables(beanType, arrayType.getGenericComponentType(), attributes);
        }
    }

    public static void validateScope(BeanAttributes<?> attributes, BeanManager manager) {
        if (attributes.getScope() == null) {
            throw MetadataLogger.LOG.scopeNull(attributes);
        }
        if (!manager.isScope(attributes.getScope())) {
            throw MetadataLogger.LOG.notAScope(attributes.getScope(), attributes);
        }
    }

}
