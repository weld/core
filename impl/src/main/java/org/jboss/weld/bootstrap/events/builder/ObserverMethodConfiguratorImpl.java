/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap.events.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.builder.ObserverMethodConfigurator;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.experimental.Priority;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 */
public class ObserverMethodConfiguratorImpl<T> implements ObserverMethodConfigurator<T> {

    // TODO replace with default value from ObserverMethod
    @SuppressWarnings("checkstyle:magicnumber")
    private static final int DEFAULT_PRIORITY = javax.interceptor.Interceptor.Priority.APPLICATION + 500;

    private Class<?> beanClass;

    private Type observedType;

    private final Set<Annotation> observedQualifiers;

    private Reception reception;

    private TransactionPhase txPhase;

    private int priority;

    private boolean isAsync;

    private Consumer<T> notifySimple;

    private BiConsumer<T, EventMetadata> notifyMetadata;

    public ObserverMethodConfiguratorImpl() {
        this.reception = Reception.ALWAYS;
        this.txPhase = TransactionPhase.IN_PROGRESS;
        this.observedQualifiers = new HashSet<>();
        this.priority = DEFAULT_PRIORITY;
    }

    public ObserverMethodConfiguratorImpl(ObserverMethod<T> observerMethod) {
        this();
        read(observerMethod);
    }

    @Override
    public ObserverMethodConfigurator<T> read(Method method) {
        Set<Parameter> eventParameters = Configurators.getAnnotatedParameters(method, Observes.class, ObservesAsync.class);
        checkEventParams(eventParameters, method);
        Parameter eventParameter = eventParameters.iterator().next();
        Observes observesAnnotation = eventParameter.getAnnotation(Observes.class);
        if (observesAnnotation != null) {
            reception(observesAnnotation.notifyObserver());
            transactionPhase(observesAnnotation.during());
        } else {
            reception(eventParameter.getAnnotation(ObservesAsync.class).notifyObserver());
        }
        // TODO replace with CDI API version
        Priority priority = method.getAnnotation(Priority.class);
        if (priority != null) {
            priority(priority.value());
        }
        beanClass(eventParameter.getDeclaringExecutable().getDeclaringClass());
        observedType(eventParameter.getType());
        qualifiers(Configurators.getQualifiers(eventParameter));
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> read(AnnotatedMethod<?> method) {
        Set<AnnotatedParameter<?>> eventParameters = method.getParameters().stream()
                .filter((p) -> p.isAnnotationPresent(Observes.class) || p.isAnnotationPresent(ObservesAsync.class)).collect(Collectors.toSet());
        checkEventParams(eventParameters, method.getJavaMember());
        AnnotatedParameter<?> eventParameter = eventParameters.iterator().next();
        Observes observesAnnotation = eventParameter.getAnnotation(Observes.class);
        if (observesAnnotation != null) {
            reception(observesAnnotation.notifyObserver());
            transactionPhase(observesAnnotation.during());
        } else {
            reception(eventParameter.getAnnotation(ObservesAsync.class).notifyObserver());
        }
        // TODO replace with CDI API version
        Priority priority = method.getAnnotation(Priority.class);
        if (priority != null) {
            priority(priority.value());
        }
        beanClass(eventParameter.getDeclaringCallable().getDeclaringType().getJavaClass());
        observedType(eventParameter.getBaseType());
        qualifiers(Configurators.getQualifiers(eventParameter));
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> read(final ObserverMethod<T> observerMethod) {
        beanClass(observerMethod.getBeanClass());
        observedType(observerMethod.getObservedType());
        qualifiers(observerMethod.getObservedQualifiers());
        reception(observerMethod.getReception());
        transactionPhase(observerMethod.getTransactionPhase());
        priority(observerMethod.getPriority());
        async(observerMethod.isAsync());
        notifyWith(e -> observerMethod.notify(e));
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> beanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> observedType(Type type) {
        this.observedType = type;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifier(Annotation qualifier) {
        this.observedQualifiers.add(qualifier);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifiers(Annotation... qualifiers) {
        Collections.addAll(this.observedQualifiers, qualifiers);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifiers(Set<Annotation> qualifiers) {
        this.observedQualifiers.addAll(qualifiers);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> qualifiers(Annotation... qualifiers) {
        this.observedQualifiers.clear();
        addQualifiers(qualifiers);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> qualifiers(Set<Annotation> qualifiers) {
        this.observedQualifiers.clear();
        addQualifiers(qualifiers);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> reception(Reception reception) {
        this.reception = reception;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> transactionPhase(TransactionPhase transactionPhase) {
        this.txPhase = transactionPhase;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> notifyWith(Consumer<T> callback) {
        this.notifySimple = callback;
        this.notifyMetadata = null;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> notifyWith(BiConsumer<T, EventMetadata> callback) {
        this.notifySimple = null;
        this.notifyMetadata = callback;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> async(boolean async) {
        this.isAsync = async;
        return this;
    }

    /**
     * @return the beanClass
     */
    Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * @return the observedType
     */
    Type getObservedType() {
        return observedType;
    }

    /**
     * @return the observedQualifiers
     */
    Set<Annotation> getObservedQualifiers() {
        return observedQualifiers;
    }

    /**
     * @return the reception
     */
    Reception getReception() {
        return reception;
    }

    /**
     * @return the txPhase
     */
    TransactionPhase getTxPhase() {
        return txPhase;
    }

    /**
     * @return the priority
     */
    int getPriority() {
        return priority;
    }

    /**
     * @return the isAsync
     */
    boolean isAsync() {
        return isAsync;
    }

    /**
     * @return the notifySimple
     */
    Consumer<T> getNotifySimple() {
        return notifySimple;
    }

    /**
     * @return the notifyMetadata
     */
    BiConsumer<T, EventMetadata> getNotifyMetadata() {
        return notifyMetadata;
    }

    private <P> void checkEventParams(Set<P> eventParams, Method method) {
        if (eventParams.size() != 1) {
            // TODO Move to EventLogger in case of the read methods remain in the spec
            throw new IllegalArgumentException(
                    "None or multiple event parameters declared on: " + method + "\n\tat " + Formats.formatAsStackTraceElement(method) + "\n StackTrace:");
        }
    }

}
