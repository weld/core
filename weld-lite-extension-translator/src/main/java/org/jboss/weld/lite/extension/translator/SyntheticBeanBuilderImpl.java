package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class SyntheticBeanBuilderImpl<T> extends SyntheticComponentBuilderBase<SyntheticBeanBuilderImpl<T>> implements SyntheticBeanBuilder<T> {
    Class<?> implementationClass;
    Set<java.lang.reflect.Type> types = new HashSet<>();
    Set<Annotation> qualifiers = new HashSet<>();
    Class<? extends Annotation> scope;
    boolean isAlternative;
    int priority;
    String name;
    Set<Class<? extends Annotation>> stereotypes = new HashSet<>();
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
    public SyntheticBeanBuilder<T> priority(int priority) {
        this.priority = priority;
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
        this.stereotypes.add((Class<? extends Annotation>) ((ClassInfoImpl) stereotypeAnnotation).cdiDeclaration.getJavaClass());
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
}
