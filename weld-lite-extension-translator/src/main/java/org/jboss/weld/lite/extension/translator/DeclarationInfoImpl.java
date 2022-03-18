package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.DeclarationInfo;
import org.jboss.weld.lite.extension.translator.logging.LiteExtensionTranslatorLogger;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class DeclarationInfoImpl<ReflectionDeclaration extends java.lang.reflect.AnnotatedElement,
        CdiDeclaration extends jakarta.enterprise.inject.spi.Annotated> extends AnnotationTargetImpl<ReflectionDeclaration> implements DeclarationInfo {
    final CdiDeclaration cdiDeclaration; // may be null

    DeclarationInfoImpl(ReflectionDeclaration reflectionDeclaration, CdiDeclaration cdiDeclaration, BeanManager bm) {
        super(reflectionDeclaration, null, bm);
        this.cdiDeclaration = cdiDeclaration;
    }

    static DeclarationInfo fromCdiDeclaration(jakarta.enterprise.inject.spi.Annotated cdiDeclaration, BeanManager bm) {
        Objects.requireNonNull(cdiDeclaration);

        if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedType) {
            return new ClassInfoImpl((jakarta.enterprise.inject.spi.AnnotatedType<?>) cdiDeclaration, bm);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedCallable) {
            // method or constructor
            return new MethodInfoImpl((jakarta.enterprise.inject.spi.AnnotatedCallable<?>) cdiDeclaration, bm);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedParameter) {
            return new ParameterInfoImpl((jakarta.enterprise.inject.spi.AnnotatedParameter<?>) cdiDeclaration, bm);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedField) {
            return new FieldInfoImpl((jakarta.enterprise.inject.spi.AnnotatedField<?>) cdiDeclaration, bm);
        } else {
            throw LiteExtensionTranslatorLogger.LOG.unknownDeclaration(cdiDeclaration);
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
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it, bm)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        if (cdiDeclaration == null) {
            return super.annotation(annotationType);
        }

        T annotation = cdiDeclaration.getAnnotation(annotationType);
        return annotation == null ? null : new AnnotationInfoImpl(annotation, bm);
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        if (cdiDeclaration == null) {
            return super.repeatableAnnotation(annotationType);
        }

        return cdiDeclaration.getAnnotations(annotationType)
                .stream()
                .map(t -> new AnnotationInfoImpl(t, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        if (cdiDeclaration == null) {
            return super.annotations(predicate);
        }

        return cdiDeclaration.getAnnotations()
                .stream()
                .map(annotation -> new AnnotationInfoImpl(annotation, bm))
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
