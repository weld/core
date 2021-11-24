package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationTarget;
import org.jboss.weld.lite.extension.translator.util.AnnotationOverrides;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AnnotationTargetImpl<Reflection extends java.lang.reflect.AnnotatedElement> implements AnnotationTarget {
    final Reflection reflection;
    final AnnotationOverrides overrides;

    AnnotationTargetImpl(Reflection reflection, AnnotationOverrides overrides) {
        this.reflection = Objects.requireNonNull(reflection);
        this.overrides = overrides;
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
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        T annotation = annotations.getAnnotation(annotationType);
        return annotation == null ? null : new AnnotationInfoImpl(annotation);
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return Arrays.stream(annotations.getAnnotationsByType(annotationType))
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        java.lang.reflect.AnnotatedElement annotations = overrides != null ? overrides : reflection;
        return Arrays.stream(annotations.getAnnotations())
                .map(AnnotationInfoImpl::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotations(it -> true);
    }
}
