/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.util.collections.ImmutableMap;

/**
 *
 * @author Martin Kouba
 */
public final class Primitives {

    private static final String ARG_TYPE = "type";

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE;

    private Primitives() {
    }

    static {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<Class<?>, Class<?>>();
        Map<Class<?>, Class<?>> wrapperToPrimitive = new HashMap<Class<?>, Class<?>>();

        put(primitiveToWrapper, wrapperToPrimitive, boolean.class, Boolean.class);
        put(primitiveToWrapper, wrapperToPrimitive, char.class, Character.class);
        put(primitiveToWrapper, wrapperToPrimitive, short.class, Short.class);
        put(primitiveToWrapper, wrapperToPrimitive, int.class, Integer.class);
        put(primitiveToWrapper, wrapperToPrimitive, long.class, Long.class);
        put(primitiveToWrapper, wrapperToPrimitive, double.class, Double.class);
        put(primitiveToWrapper, wrapperToPrimitive, float.class, Float.class);
        put(primitiveToWrapper, wrapperToPrimitive, byte.class, Byte.class);

        PRIMITIVE_TO_WRAPPER = ImmutableMap.copyOf(primitiveToWrapper);
        WRAPPER_TO_PRIMITIVE = ImmutableMap.copyOf(wrapperToPrimitive);
    }

    private static void put(Map<Class<?>, Class<?>> primitiveToWrapper, Map<Class<?>, Class<?>> wrapperToPrimitive,
            Class<?> primitive, Class<?> wrapper) {
        primitiveToWrapper.put(primitive, wrapper);
        wrapperToPrimitive.put(wrapper, primitive);
    }

    /**
     *
     * @param type
     * @return the wrapper type of the given type if it is a primitive, or the type itself otherwise
     */
    public static <T> Class<T> wrap(Class<T> type) {
        checkArgumentNotNull(type, ARG_TYPE);
        Class<T> wrapped = cast(PRIMITIVE_TO_WRAPPER.get(type));
        return wrapped != null ? wrapped : type;
    }

    /**
     *
     * @param type
     * @return the primitive type of the given type if it is a wrapper, or the type itself otherwise
     */
    public static <T> Class<T> unwrap(Class<T> type) {
        checkArgumentNotNull(type, ARG_TYPE);
        Class<T> primitive = cast(WRAPPER_TO_PRIMITIVE.get(type));
        return primitive != null ? primitive : type;
    }

}
