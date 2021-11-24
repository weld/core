package org.jboss.weld.lite.extension.translator;

import jakarta.decorator.Decorator;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.Interceptor;

import java.lang.annotation.Annotation;

// TODO this is mostly a hack
final class BeanManagerAccess {
    private static jakarta.enterprise.inject.spi.BeanManager beanManager;

    static void set(jakarta.enterprise.inject.spi.BeanManager beanManager) {
        BeanManagerAccess.beanManager = beanManager;
    }

    static void remove() {
        BeanManagerAccess.beanManager = null;
    }

    static <T> jakarta.enterprise.inject.spi.AnnotatedType<T> createAnnotatedType(Class<T> clazz) {
        if (beanManager == null) {
            throw new IllegalStateException("BeanManagerAccess.createAnnotatedType can only be called within an extension method");
        }

        return beanManager.createAnnotatedType(clazz);
    }

    static boolean isBeanDefiningAnnotation(Class<? extends Annotation> annotationType) {
        if (beanManager == null) {
            throw new IllegalStateException("BeanManagerAccess.isBeanDefiningAnnotation can only be called within an extension method");
        }

        return beanManager.isNormalScope(annotationType)
                || beanManager.isStereotype(annotationType)
                || Dependent.class.equals(annotationType)
                || Interceptor.class.equals(annotationType)
                || Decorator.class.equals(annotationType);
    }
}
