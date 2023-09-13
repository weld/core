package org.jboss.weld.lite.extension.translator.util.reflection;

import java.lang.annotation.Annotation;

final class AnnotatedTypeImpl implements java.lang.reflect.AnnotatedType {
    private final Class<?> clazz;

    AnnotatedTypeImpl(Class<?> clazz) {
        this.clazz = clazz;
    }

    // added in Java 9
    /*
     * @Override
     */
    public java.lang.reflect.AnnotatedType getAnnotatedOwnerType() {
        if (clazz.isPrimitive() || clazz == Void.TYPE) {
            return null;
        }

        Class<?> declaringClass = clazz.getDeclaringClass();
        return declaringClass == null ? null : AnnotatedTypes.from(declaringClass);
    }

    @Override
    public java.lang.reflect.Type getType() {
        return clazz;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return clazz.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return clazz.getDeclaredAnnotations();
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}
