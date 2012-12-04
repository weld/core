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

import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jboss.weld.util.reflection.RawType;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Utility class for Types
 *
 * @author Pete Muir
 */
public class Types {

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
            return Reflections.<Class<?>>cast(type).getName();
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
        throw new IllegalArgumentException("Cannot create type id for" + type.toString());
    }

    /**
     *
     * @param clazz
     * @return
     */
    public static Type resolveType(Class<?> clazz) {
        if (clazz.getTypeParameters().length > 0) {
            TypeVariable<?>[] actualTypeParameters = clazz.getTypeParameters();
            return new ParameterizedTypeImpl(clazz, actualTypeParameters, clazz.getDeclaringClass());
        }
        return clazz;
    }

    /**
     *
     * @param type
     * @return
     */
    public static Type resolveType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            return resolveType(clazz);
        }
        if (type instanceof RawType<?>) {
            RawType<?> rawType = (RawType<?>) type;
            return rawType.getType();
        }
        return type;
    }
}
