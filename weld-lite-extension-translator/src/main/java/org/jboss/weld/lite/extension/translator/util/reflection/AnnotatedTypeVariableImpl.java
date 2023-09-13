package org.jboss.weld.lite.extension.translator.util.reflection;

import java.lang.annotation.Annotation;

final class AnnotatedTypeVariableImpl implements java.lang.reflect.AnnotatedTypeVariable {
    private final java.lang.reflect.TypeVariable<?> typeVariable;

    AnnotatedTypeVariableImpl(java.lang.reflect.TypeVariable<?> typeVariable) {
        this.typeVariable = typeVariable;
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedBounds() {
        return typeVariable.getAnnotatedBounds();
    }

    // added in Java 9
    /*
     * @Override
     */
    public java.lang.reflect.AnnotatedType getAnnotatedOwnerType() {
        return null;
    }

    @Override
    public java.lang.reflect.Type getType() {
        return typeVariable;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return typeVariable.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return typeVariable.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return typeVariable.getDeclaredAnnotations();
    }

    @Override
    public String toString() {
        return typeVariable.toString();
    }
}
