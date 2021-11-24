package org.jboss.weld.lite.extension.translator.util;

import java.lang.annotation.Annotation;

public final class AnnotationOverrides implements java.lang.reflect.AnnotatedElement {
    private static final Annotation[] EMPTY_ARRAY = new Annotation[0];

    private final Annotation[] annotations;

    public AnnotationOverrides(Annotation[] annotations) {
        this.annotations = annotations;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.annotationType())) {
                return annotationClass.cast(annotation);
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotations != null ? annotations : EMPTY_ARRAY;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotations != null ? annotations : EMPTY_ARRAY;
    }
}
