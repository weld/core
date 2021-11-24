package org.jboss.weld.lite.extension.translator.util.reflection;

import java.lang.annotation.Annotation;

abstract class AbstractEmptyAnnotatedType implements java.lang.reflect.AnnotatedType {
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }
}
