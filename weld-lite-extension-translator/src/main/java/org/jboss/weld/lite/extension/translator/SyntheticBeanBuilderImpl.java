package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

class SyntheticBeanBuilderImpl<T> extends SyntheticComponentBuilderBase<SyntheticBeanBuilderImpl<T>>
        implements SyntheticBeanBuilder<T> {
    Class<?> implementationClass;
    Set<java.lang.reflect.Type> types = new HashSet<>();
    Set<Annotation> qualifiers = new HashSet<>();
    Class<? extends Annotation> scope;
    boolean isAlternative;
    boolean isReserve;
    boolean isEager;
    boolean isAutoClose;
    int priority;
    String name;
    Set<Class<? extends Annotation>> stereotypes = new HashSet<>();
    List<InjectionPointDeclaration> injectionPoints = new ArrayList<>();
    Class<? extends SyntheticBeanCreator<T>> creatorClass;
    Class<? extends SyntheticBeanDisposer<T>> disposerClass;
    Class<? extends BuildCompatibleExtension> extensionClass;

    SyntheticBeanBuilderImpl(Class<?> implementationClass, Class<? extends BuildCompatibleExtension> extensionClass) {
        this.implementationClass = implementationClass;
        this.extensionClass = extensionClass;
    }

    @Override
    public SyntheticBeanBuilder<T> type(Class<?> type) {
        this.types.add(type);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(ClassInfo type) {
        this.types.add(((ClassInfoImpl) type).cdiDeclaration.getJavaClass());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(Type type) {
        this.types.add(((TypeImpl<?>) type).reflection.getType());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Class<? extends Annotation> qualifierAnnotation) {
        this.qualifiers.add(AnnotationProxy.create(qualifierAnnotation, Collections.emptyMap()));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).annotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(qualifierAnnotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> scope(Class<? extends Annotation> scopeAnnotation) {
        this.scope = scopeAnnotation;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> alternative(boolean isAlternative) {
        this.isAlternative = isAlternative;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> reserve(boolean isReserve) {
        this.isReserve = isReserve;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> eager(boolean isEager) {
        this.isEager = isEager;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> autoClose(boolean isAutoClose) {
        this.isAutoClose = isAutoClose;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(Class<? extends Annotation> stereotypeAnnotation) {
        this.stereotypes.add(stereotypeAnnotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(ClassInfo stereotypeAnnotation) {
        this.stereotypes
                .add((Class<? extends Annotation>) ((ClassInfoImpl) stereotypeAnnotation).cdiDeclaration.getJavaClass());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Class<?> type) {
        return withInjectionPoint(type, new Annotation[0]);
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Class<?> type, Annotation... qualifiers) {
        injectionPoints.add(new InjectionPointDeclaration(type, new HashSet<>(Arrays.asList(qualifiers))));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Class<?> type, AnnotationInfo... qualifiers) {
        Set<Annotation> annotations = new HashSet<>();
        for (AnnotationInfo info : qualifiers) {
            annotations.add(((AnnotationInfoImpl) info).annotation);
        }
        injectionPoints.add(new InjectionPointDeclaration(type, annotations));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Type type) {
        return withInjectionPoint(type, new Annotation[0]);
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Type type, Annotation... qualifiers) {
        java.lang.reflect.Type reflectionType = ((TypeImpl<?>) type).reflection.getType();
        injectionPoints.add(new InjectionPointDeclaration(reflectionType, new HashSet<>(Arrays.asList(qualifiers))));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withInjectionPoint(Type type, AnnotationInfo... qualifiers) {
        java.lang.reflect.Type reflectionType = ((TypeImpl<?>) type).reflection.getType();
        Set<Annotation> annotations = new HashSet<>();
        for (AnnotationInfo info : qualifiers) {
            annotations.add(((AnnotationInfoImpl) info).annotation);
        }
        injectionPoints.add(new InjectionPointDeclaration(reflectionType, annotations));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> createWith(Class<? extends SyntheticBeanCreator<T>> creatorClass) {
        this.creatorClass = creatorClass;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> disposeWith(Class<? extends SyntheticBeanDisposer<T>> disposerClass) {
        this.disposerClass = disposerClass;
        return this;
    }

    static class InjectionPointDeclaration {
        final java.lang.reflect.Type type;
        final Set<Annotation> qualifiers;

        InjectionPointDeclaration(java.lang.reflect.Type type, Set<Annotation> qualifiers) {
            this.type = type;
            this.qualifiers = qualifiers;
        }
    }
}
