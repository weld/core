/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jboss.weld.resolution.CovariantTypes;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.GenericArrayTypeImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Utility class for Types
 *
 * @author Pete Muir
 */
public class Types {

    public static final Function<Type, Class<?>> TYPE_TO_CLASS_FUNCTION = Reflections::getRawType;

    private Types() {
    }

    /**
     * Gets the boxed type of a class
     *
     * @param type The type
     * @return The boxed type
     */
    public static Type boxedType(Type type) {
        if (type instanceof Class<?>) {
            return boxedClass((Class<?>) type);
        } else {
            return type;
        }
    }

    public static Class<?> boxedClass(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        } else if (type.equals(Boolean.TYPE)) {
            return Boolean.class;
        } else if (type.equals(Character.TYPE)) {
            return Character.class;
        } else if (type.equals(Byte.TYPE)) {
            return Byte.class;
        } else if (type.equals(Short.TYPE)) {
            return Short.class;
        } else if (type.equals(Integer.TYPE)) {
            return Integer.class;
        } else if (type.equals(Long.TYPE)) {
            return Long.class;
        } else if (type.equals(Float.TYPE)) {
            return Float.class;
        } else if (type.equals(Double.TYPE)) {
            return Double.class;
        } else if (type.equals(Void.TYPE)) {
            return Void.class;
        } else {
            // Vagaries of if/else statement, can't be reached ;-)
            return type;
        }
    }

    public static String getTypeId(Type type) {
        if (type instanceof Class<?>) {
            return Reflections.<Class<?>> cast(type).getName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            StringBuilder builder = new StringBuilder(getTypeId(pt.getRawType()));
            builder.append("<");
            for (int i = 0; i < pt.getActualTypeArguments().length; i++) {
                if (i > 0) {
                    builder.append(",");
                }
                builder.append(getTypeId(pt.getActualTypeArguments()[i]));
            }
            builder.append(">");
            return builder.toString();
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            StringBuilder builder = new StringBuilder(getTypeId(arrayType.getGenericComponentType()));
            builder.append("[]");
            return builder.toString();
        }
        throw new IllegalArgumentException("Cannot create type id for " + type.toString());
    }

    /**
     * Returns a canonical type for a given class.
     *
     * If the class is a raw type of a parameterized class, the matching {@link ParameterizedType} (with unresolved type
     * variables) is resolved.
     *
     * If the class is an array then the component type of the array is canonicalized
     *
     * Otherwise, the class is returned.
     *
     * @return
     */
    public static Type getCanonicalType(Class<?> clazz) {
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            Type resolvedComponentType = getCanonicalType(componentType);
            if (componentType != resolvedComponentType) {
                // identity check intentional
                // a different identity means that we actually replaced the component Class with a ParameterizedType
                return new GenericArrayTypeImpl(resolvedComponentType);
            }
        }
        if (clazz.getTypeParameters().length > 0) {
            Type[] actualTypeParameters = clazz.getTypeParameters();
            return new ParameterizedTypeImpl(clazz, actualTypeParameters, clazz.getDeclaringClass());
        }
        return clazz;
    }

    /**
     *
     * @param type
     * @return
     */
    public static Type getCanonicalType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            return getCanonicalType(clazz);
        }
        return type;
    }

    public static boolean containsTypeVariable(Type type) {
        type = Types.getCanonicalType(type);
        if (type instanceof TypeVariable<?>) {
            return true;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type t : parameterizedType.getActualTypeArguments()) {
                if (containsTypeVariable(t)) {
                    return true;
                }
            }
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return containsTypeVariable(genericArrayType.getGenericComponentType());
        }
        return false;
    }

    public static Set<Class<?>> getRawTypes(Set<Type> types) {
        return types.stream().map(Reflections::getRawType).collect(ImmutableSet.collector());
    }

    public static Class<?>[] getRawTypes(Type[] types) {
        if (types.length == 0) {
            return Arrays2.EMPTY_CLASS_ARRAY;
        }
        Class<?>[] result = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            result[i] = TYPE_TO_CLASS_FUNCTION.apply(types[i]);
        }
        return result;
    }

    /**
     * Builds (class name -> class) map for given classes.
     */
    @SuppressWarnings("all")
    public static <C extends Class<?>> Map<String, C> buildClassNameMap(Iterable<C> set) {
        Map<String, C> classNameMap = new HashMap<String, C>();
        for (C javaClass : set) {
            classNameMap.put(javaClass.getName(), javaClass);
        }
        return classNameMap;
    }

    /**
     * Determines whether the given type is an actual type. A type is considered actual if it is a raw type, a parameterized
     * type
     * or an array type.
     *
     * @param type the given type
     * @return true if and only if the given type is an actual type
     */
    public static boolean isActualType(Type type) {
        return (type instanceof Class<?>) || (type instanceof ParameterizedType) || (type instanceof GenericArrayType);
    }

    /**
     * Determines whether the given type is an array type.
     *
     * @param type the given type
     * @return true if the given type is a subclass of java.lang.Class or implements GenericArrayType
     */
    public static boolean isArray(Type type) {
        return (type instanceof GenericArrayType) || (type instanceof Class<?> && ((Class<?>) type).isArray());
    }

    /**
     * Determines the component type for a given array type.
     *
     * @param type the given array type
     * @return the component type of a given array type
     */
    public static Type getArrayComponentType(Type type) {
        if (type instanceof GenericArrayType) {
            return GenericArrayType.class.cast(type).getGenericComponentType();
        }
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isArray()) {
                return clazz.getComponentType();
            }
        }
        throw new IllegalArgumentException("Not an array type " + type);
    }

    /**
     * Determines whether the given array only contains unbounded type variables or Object.class.
     *
     * @param types the given array of types
     * @return true if and only if the given array only contains unbounded type variables or Object.class
     */
    public static boolean isArrayOfUnboundedTypeVariablesOrObjects(Type[] types) {
        for (Type type : types) {
            if (Object.class.equals(type)) {
                continue;
            }
            if (type instanceof TypeVariable<?>) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                if (bounds == null || bounds.length == 0 || (bounds.length == 1 && Object.class.equals(bounds[0]))) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isRawGenericType(Type type) {
        if (!(type instanceof Class<?>)) {
            return false;
        }
        Class<?> clazz = (Class<?>) type;
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            return isRawGenericType(componentType);
        }
        return clazz.getTypeParameters().length > 0;
    }

    /**
     *
     * @param beanType
     * @return <code>true</code> if the given type is not a legal bean type, <code>false</code> otherwise
     */
    public static boolean isIllegalBeanType(Type beanType) {
        boolean result = false;
        if (beanType instanceof TypeVariable<?>) {
            result = true;
        } else if (beanType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) beanType;
            for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                if (typeArgument instanceof TypeVariable<?>) {
                    // Parameterized type with type variable is legal
                    continue;
                } else if (typeArgument instanceof WildcardType || isIllegalBeanType(typeArgument)) {
                    result = true;
                    break;
                }
            }
        } else if (beanType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) beanType;
            result = isIllegalBeanType(arrayType.getGenericComponentType());
        }
        return result;
    }

    /**
     *
     * @param type1
     * @param type2
     * @return <code>true</code> if the first type is more specific than the second type (is a subtype of), <code>false</code>
     *         otherwise
     */
    public static boolean isMoreSpecific(Type type1, Type type2) {
        if (type1.equals(type2)) {
            return false;
        }
        return CovariantTypes.isAssignableFrom(type2, type1);
    }
}
