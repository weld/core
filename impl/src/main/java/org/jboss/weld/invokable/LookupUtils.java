package org.jboss.weld.invokable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;

import org.jboss.weld.inject.WeldInstance;

class LookupUtils {
    private LookupUtils() {
    }

    static Object lookup(CleanupActions cleanup, BeanManager beanManager, Type type, Annotation[] qualifiers) {
        WeldInstance<Object> lookup = (WeldInstance<Object>) beanManager.createInstance();
        Instance.Handle<Object> result = lookup.select(type, qualifiers).getHandle();
        cleanup.addInstanceHandle(result);
        return result.get();
    }

    static Annotation[] classQualifiers(Class<?> beanClass, BeanManager bm) {
        return findQualifiers(beanClass.getAnnotations(), bm);
    }

    static Annotation[] parameterQualifiers(Parameter parameter, BeanManager bm) {
        return findQualifiers(parameter.getAnnotations(), bm);
    }

    private static Annotation[] findQualifiers(Annotation[] annotations, BeanManager bm) {
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
