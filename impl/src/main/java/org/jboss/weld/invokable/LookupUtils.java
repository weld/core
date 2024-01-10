package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;

import org.jboss.weld.inject.WeldInstance;

class LookupUtils {
    private LookupUtils() {
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

    static boolean hasPrimitiveArgLookup(Class<?>[] types, boolean[] argLookup) {
        for (int i = 0; i < Math.min(types.length, argLookup.length); i++) {
            if (types[i].isPrimitive() && argLookup[i]) {
                return true;
            }
        }
        return false;
    }

    static Object[] replacePrimitiveLookupNulls(Object[] values, Class<?>[] types, boolean[] argLookup) {
        for (int i = 0; i < values.length; i++) {
            Class<?> type = types[i];
            if (values[i] == null && type.isPrimitive() && argLookup[i]) {
                values[i] = PRIMITIVE_WRAPPER_ZERO_VALUES.get(type);
            }
        }
        return values;
    }

    static Object lookup(CleanupActions cleanup, BeanManager beanManager, Type type, Annotation[] qualifiers) {
        WeldInstance<Object> lookup = (WeldInstance<Object>) beanManager.createInstance();
        Instance.Handle<Object> result = lookup.select(type, qualifiers).getHandle();
        cleanup.addInstanceHandle(result);
        return result.get();
    }

    static Annotation[] extractQualifiers(Collection<Annotation> annotations, BeanManager bm) {
        List<Annotation> qualifiers = new ArrayList<>();
        for (Annotation a : annotations) {
            if (bm.isQualifier(a.annotationType())) {
                qualifiers.add(a);
            }
        }
        // add @Default when there are no qualifiers or just @Named
        if (qualifiers.isEmpty() || (qualifiers.size() == 1 && qualifiers.get(0).annotationType().equals(Named.class))) {
            qualifiers.add(Default.Literal.INSTANCE);
        }
        return qualifiers.toArray(Annotation[]::new);
    }
}
