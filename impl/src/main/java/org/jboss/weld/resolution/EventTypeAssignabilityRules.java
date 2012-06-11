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
public class EventTypeAssignabilityRules implements AssignabilityRules {

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

    protected boolean processWildcardTypesAndTypeVariables(Type requiredType, Type beanType) {
        if (requiredType instanceof WildcardType) {
            return processWildcard((WildcardType) requiredType, beanType);
        }
        if (requiredType instanceof TypeVariable<?>) {
            return processTypeVariable((TypeVariable<?>) requiredType, beanType);
        }
        return false;
    }

    protected boolean processWildcard(WildcardType requiredType, Type beanType) {
        return isTypeInsideBounds(beanType, requiredType.getLowerBounds(), requiredType.getUpperBounds());
    }

    protected boolean processTypeVariable(TypeVariable<?> requiredType, Type beanType) {
        return isTypeInsideBounds(beanType, EMPTY_TYPES, requiredType.getBounds());
    }

    /**
     * Checks whether the given type is assignable from lower bounds and assignable to upper bounds.
     */
    public boolean isTypeInsideBounds(TypeHolder type, Type[] lowerBounds, Type[] upperBounds) {
        return (lowerBounds.length == 0 || isAssignableFrom(type, lowerBounds)) && (upperBounds.length == 0 || isAssignableTo(type, upperBounds));
    }

    /**
     * Checks whether the given type is assignable from lower bounds and assignable to upper bounds.
     */
    public boolean isTypeInsideBounds(Type type, Type[] lowerBounds, Type[] upperBounds) {
        return (lowerBounds.length == 0 || isAssignableFrom(type, lowerBounds)) && (upperBounds.length == 0 || isAssignableTo(type, upperBounds));
    }

    public boolean areTypesInsideBounds(Type[] types, Type[] lowerBounds, Type[] upperBounds) {
        for (Type type : types) {
            if (!isTypeInsideBounds(type, lowerBounds, upperBounds)) {
                return false;
            }
        }
        return true;
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
            if (isTypeInsideBounds(requiredType, EMPTY_TYPES, typeVariable.getBounds())) {
                return true;
            }
        }
        if (otherType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) otherType;
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (isAssignableFrom(requiredType, upperBound)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAssignableFrom(TypeHolder type1, Type[] types2) {
        for (Type type2 : types2) {
            if (isAssignableFrom(type1, type2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAssignableTo(TypeHolder type1, Type[] types2) {
        return isAssignableFrom(types2, type1);
    }

    public boolean isAssignableFrom(Type[] types1, TypeHolder type2) {
        for (Type type : types1) {
            TypeHolder holder = wrapWithinTypeHolder(type);
            if (holder != null && isAssignableFrom(holder, type2)) {
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
