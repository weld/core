package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;

import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

abstract class AnnotationTargetImpl<Reflection extends java.lang.reflect.AnnotatedElement> implements AnnotationTarget {
    final Reflection reflection;
    final AnnotationOverrides overrides;
    protected final BeanManager bm;

    AnnotationTargetImpl(Reflection reflection, AnnotationOverrides overrides, BeanManager bm) {
        this.reflection = Objects.requireNonNull(reflection);
        this.overrides = overrides;
        this.bm = bm;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return annotations.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return Arrays.stream(annotations.getAnnotations())
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it, bm)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        T annotation = annotations.getAnnotation(annotationType);
        return annotation == null ? null : new AnnotationInfoImpl(annotation, bm);
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return Arrays.stream(annotations.getAnnotationsByType(annotationType))
                .map(t -> new AnnotationInfoImpl(t, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return Arrays.stream(annotations.getAnnotations())
                .map(annotation -> new AnnotationInfoImpl(annotation, bm))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotations(it -> true);
    }
}
