/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resolution;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Set;

import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Assignability rules for observer method resolution. Serves as a superclass for stricter {@link BeanTypeAssignabilityRules}.
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class EventTypeAssignabilityRules {

    protected EventTypeAssignabilityRules() {
    }

    private static final EventTypeAssignabilityRules INSTANCE = new EventTypeAssignabilityRules();

    public static EventTypeAssignabilityRules instance() {
        return INSTANCE;
    }

    public static final Type[] EMPTY_TYPES = {};

    public boolean isAssignableTo(Type type1, Type[] types2) {
        return isAssignableFrom(types2, type1);
    }

    public boolean isAssignableFrom(Type type1, Set<? extends Type> types2) {
        for (Type type2 : types2) {
            if (isAssignableFrom(type1, type2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssignableFrom(Type type1, Type[] types2) {
        for (Type type2 : types2) {
            if (isAssignableFrom(type1, type2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssignableFrom(Type type1, Type type2) {
        TypeHolder typeHolder1 = wrapWithinTypeHolder(type1);
        if (typeHolder1 != null && isAssignableFrom(typeHolder1, type2)) {
            return true;
        }

        return processWildcardTypesAndTypeVariables(type1, type2);
    }

    public boolean isAssignableFrom(Type[] types1, Type type2) {
        for (Type type : types1) {
            if (isAssignableFrom(type, type2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether whether any of the requiredTypes matches a type in beanTypes
     *
     * @param requiredTypes the requiredTypes
     * @param beanTypes     the beanTypes
     * @return can we assign any type from requiredTypes to beanTypes
     */
    public boolean matches(Set<Type> requiredTypes, Set<Type> beanTypes) {
        for (Type requiredType : requiredTypes) {
            if (matches(requiredType, beanTypes)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(Type requiredType, Set<? extends Type> beanTypes) {
        for (Type beanType : beanTypes) {
            if (matches(requiredType, beanType)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(Type requiredType, Type beanType) {
        TypeHolder requiredTypeHolder = wrapWithinTypeHolder(requiredType);
        if (requiredTypeHolder != null && this.matches(requiredTypeHolder, beanType)) {
            return true;
        }

        return processWildcardTypesAndTypeVariables(requiredType, beanType);
    }

    private boolean processWildcardTypesAndTypeVariables(Type requiredType, Type beanType) {
        if (requiredType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) requiredType;
            if (isTypeInsideBounds(beanType, wildcardType.getLowerBounds(), wildcardType.getUpperBounds())) {
                return true;
            }
        }
        if (beanType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) beanType;
            if (isTypeInsideBounds(requiredType, wildcardType.getUpperBounds(), wildcardType.getLowerBounds())) {
                return true;
            }
        }
        if (requiredType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) requiredType;
            if (isTypeInsideBounds(beanType, EMPTY_TYPES, typeVariable.getBounds())) {
                return true;
            }
        }
        if (beanType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) beanType;
            if (isTypeInsideBounds(requiredType, typeVariable.getBounds(), EMPTY_TYPES)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTypeInsideBounds(Type type, Type[] lowerBounds, Type[] upperBounds) {
        return (lowerBounds.length == 0 || isAssignableFrom(type, lowerBounds)) && (upperBounds.length == 0 || isAssignableTo(type, upperBounds));
    }

    /**
     * This is a helper class that holds the raw type and the actual type arguments of a Type. In case of arrays, the raw type
     * is the raw type of the array, while the actualTypeArguments are the actualTypeArguments of the component type of the
     * array.
     */
    protected static class TypeHolder {

        private Class<?> rawType;
        private Type[] actualTypeArguments;

        private TypeHolder(Class<?> rawType, Type[] actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        public Class<?> getRawType() {
            return rawType;
        }

        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        private Class<?> getBoxedRawType() {
            return Types.boxedClass(getRawType());
        }
    }

    protected boolean isAssignableFrom(TypeHolder requiredType, Type otherType) {
        TypeHolder otherTypeHolder = wrapWithinTypeHolder(otherType);
        if (otherTypeHolder != null) {
            return this.isAssignableFrom(requiredType, otherTypeHolder);
        }

        // TODO: this doesn't look OK!
        if (otherType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) otherType;
            if (isTypeInsideBounds(requiredType.getRawType(), EMPTY_TYPES, typeVariable.getBounds())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isAssignableFrom(TypeHolder requiredType, TypeHolder otherTypeHolder) {
        return requiredType.getBoxedRawType().isAssignableFrom(otherTypeHolder.getBoxedRawType()) && areActualTypeArgumentsAssignableFrom(requiredType, otherTypeHolder.getActualTypeArguments());
    }

    protected boolean matches(TypeHolder requiredType, Type otherType) {
        TypeHolder otherTypeHolder = wrapWithinTypeHolder(otherType);
        return otherTypeHolder != null && this.matches(requiredType, otherTypeHolder);
    }

    protected boolean matches(TypeHolder requiredType, TypeHolder otherTypeHolder) {
        return requiredType.getBoxedRawType().equals(otherTypeHolder.getBoxedRawType()) && areActualTypeArgumentsAssignableFrom(requiredType, otherTypeHolder.getActualTypeArguments());
    }

    protected boolean areActualTypeArgumentsAssignableFrom(TypeHolder requiredType, Type[] otherActualTypeArguments) {
        for (int i = 0; i < requiredType.getActualTypeArguments().length; i++) {
            Type type1 = requiredType.getActualTypeArguments()[i];
            Type type2 = otherActualTypeArguments.length > i ? otherActualTypeArguments[i] : Object.class;
            if (!isAssignableFrom(type1, type2)) {
                return false;
            }
        }
        return true;
    }

    protected TypeHolder wrapWithinTypeHolder(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> clazz = (Class<?>) rawType;
                return new TypeHolder(clazz, parameterizedType.getActualTypeArguments());
            }
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            return new TypeHolder(clazz, EMPTY_TYPES);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Type genericComponentType = arrayType.getGenericComponentType();
            Class<?> rawComponentType = Reflections.getRawType(genericComponentType);
            if (rawComponentType != null) {
                Class<?> arrayClass = Array.newInstance(rawComponentType, 0).getClass();
                return new TypeHolder(arrayClass, Reflections.getActualTypeArguments(genericComponentType));
            }
        }
        return null;
    }
}
