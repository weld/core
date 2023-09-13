package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserverBuilder;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

class SyntheticObserverBuilderImpl<T> extends SyntheticComponentBuilderBase<SyntheticObserverBuilderImpl<T>>
        implements SyntheticObserverBuilder<T> {
    Class<?> declaringClass;
    java.lang.reflect.Type eventType;
    Set<Annotation> qualifiers = new HashSet<>();
    int priority = jakarta.enterprise.inject.spi.ObserverMethod.DEFAULT_PRIORITY;
    boolean isAsync;
    Reception reception = Reception.ALWAYS;
    TransactionPhase transactionPhase = TransactionPhase.IN_PROGRESS;
    Class<? extends SyntheticObserver<T>> observerClass;

    SyntheticObserverBuilderImpl(Class<?> extensionClass, java.lang.reflect.Type eventType) {
        this.declaringClass = extensionClass;
        this.eventType = eventType;
    }

    @Override
    public SyntheticObserverBuilder<T> declaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> declaringClass(ClassInfo declaringClass) {
        this.declaringClass = ((ClassInfoImpl) declaringClass).cdiDeclaration.getJavaClass();
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> qualifier(Class<? extends Annotation> qualifierAnnotation) {
        this.qualifiers.add(AnnotationProxy.create(qualifierAnnotation, Collections.emptyMap()));
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).annotation);
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(qualifierAnnotation);
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> async(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> transactionPhase(TransactionPhase transactionPhase) {
        this.transactionPhase = transactionPhase;
        return this;
    }

    @Override
    public SyntheticObserverBuilder<T> observeWith(Class<? extends SyntheticObserver<T>> observerClass) {
        this.observerClass = observerClass;
        return this;
    }
}
