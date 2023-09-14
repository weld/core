package org.jboss.weld.invokable;

import java.util.Map;

class PrimitiveUtils {
    private PrimitiveUtils() {
    }

    private static final Map<Class<?>, Object> PRIMITIVE_WRAPPER_ZERO_VALUES = Map.ofEntries(
            Map.entry(boolean.class, false),
            Map.entry(byte.class, (byte) 0),
            Map.entry(short.class, (short) 0),
            Map.entry(int.class, 0),
            Map.entry(long.class, 0L),
            Map.entry(float.class, 0.0F),
            Map.entry(double.class, 0.0),
            Map.entry(char.class, (char) 0));

    static boolean hasPrimitive(Class<?>[] types) {
        for (Class<?> type : types) {
            if (type.isPrimitive()) {
                return true;
            }
        }
        return false;
    }

    static Object[] replacePrimitiveNulls(Object[] values, Class<?>[] types) {
        for (int i = 0; i < values.length; i++) {
            Class<?> type = types[i];
            if (values[i] == null && type.isPrimitive()) {
                values[i] = PRIMITIVE_WRAPPER_ZERO_VALUES.get(type);
            }
        }
        return values;
    }
}
