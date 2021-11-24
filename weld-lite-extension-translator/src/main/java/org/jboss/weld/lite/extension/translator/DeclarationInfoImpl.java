package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.DeclarationInfo;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class DeclarationInfoImpl<ReflectionDeclaration extends java.lang.reflect.AnnotatedElement,
        CdiDeclaration extends jakarta.enterprise.inject.spi.Annotated> extends AnnotationTargetImpl<ReflectionDeclaration> implements DeclarationInfo {
    final CdiDeclaration cdiDeclaration; // may be null

    DeclarationInfoImpl(ReflectionDeclaration reflectionDeclaration, CdiDeclaration cdiDeclaration) {
        super(reflectionDeclaration, null);
        this.cdiDeclaration = cdiDeclaration;
    }

    static DeclarationInfo fromCdiDeclaration(jakarta.enterprise.inject.spi.Annotated cdiDeclaration) {
        Objects.requireNonNull(cdiDeclaration);

        if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedType) {
            return new ClassInfoImpl((jakarta.enterprise.inject.spi.AnnotatedType<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedCallable) {
            // method or constructor
            return new MethodInfoImpl((jakarta.enterprise.inject.spi.AnnotatedCallable<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedParameter) {
            return new ParameterInfoImpl((jakarta.enterprise.inject.spi.AnnotatedParameter<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedField) {
            return new FieldInfoImpl((jakarta.enterprise.inject.spi.AnnotatedField<?>) cdiDeclaration);
        } else {
            throw new IllegalArgumentException("Unknown declaration " + cdiDeclaration);
        }
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        if (cdiDeclaration == null) {
            return super.hasAnnotation(annotationType);
        }

        return cdiDeclaration.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        if (cdiDeclaration == null) {
            return super.hasAnnotation(predicate);
        }

        return cdiDeclaration.getAnnotations()
                .stream()
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        if (cdiDeclaration == null) {
            return super.annotation(annotationType);
        }

        T annotation = cdiDeclaration.getAnnotation(annotationType);
        return annotation == null ? null : new AnnotationInfoImpl(annotation);
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        if (cdiDeclaration == null) {
            return super.repeatableAnnotation(annotationType);
        }

        return cdiDeclaration.getAnnotations(annotationType)
                .stream()
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        if (cdiDeclaration == null) {
            return super.annotations(predicate);
        }

        return cdiDeclaration.getAnnotations()
                .stream()
                .map(AnnotationInfoImpl::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return cdiDeclaration != null
                ? cdiDeclaration.toString()
                : reflection.toString();
    }
}
