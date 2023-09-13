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

import java.util.Map;

import org.jboss.weld.util.collections.ImmutableMap;

/**
 *
 * @author Martin Kouba
 */
public final class Defaults {

    private static final Map<Class<?>, Object> JLS_PRIMITIVE_DEFAULT_VALUES;

    private Defaults() {
    }

    static {
        ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
        builder.put(boolean.class, false);
        builder.put(char.class, '\u0000');
        builder.put(byte.class, (byte) 0);
        builder.put(short.class, (short) 0);
        builder.put(int.class, 0);
        builder.put(long.class, 0L);
        builder.put(float.class, 0f);
        builder.put(double.class, 0d);
        JLS_PRIMITIVE_DEFAULT_VALUES = builder.build();
    }

    /**
     * See also JLS8, 4.12.5 Initial Values of Variables.
     *
     * @param type
     * @return the default value for the given type as defined by JLS
     */
    @SuppressWarnings("unchecked")
    public static <T> T getJlsDefaultValue(Class<T> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        return (T) JLS_PRIMITIVE_DEFAULT_VALUES.get(type);
    }

}
