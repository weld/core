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
package org.jboss.weld.bootstrap.events.configurator;

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.enterprise.event.ObserverException;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator;

import org.jboss.weld.event.SyntheticObserverMethod;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.resolution.CovariantTypes;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 *
 * @author Martin Kouba
 */
public class ObserverMethodConfiguratorImpl<T> implements ObserverMethodConfigurator<T>, Configurator<ObserverMethod<T>> {

    private Class<?> beanClass;

    private Type observedType;

    private final Set<Annotation> observedQualifiers;

    private Reception reception;

    private TransactionPhase txPhase;

    private int priority;

    private boolean isAsync;

    private EventConsumer<T> notifyCallback;

    private final Extension extension;

    public ObserverMethodConfiguratorImpl(Extension extension) {
        this.reception = Reception.ALWAYS;
        this.txPhase = TransactionPhase.IN_PROGRESS;
        this.observedQualifiers = new HashSet<>();
        this.priority = ObserverMethod.DEFAULT_PRIORITY;
        this.extension = extension;
        this.beanClass = extension.getClass();
    }

    public ObserverMethodConfiguratorImpl(ObserverMethod<T> observerMethod, Extension extension) {
        this(extension);
        read(observerMethod);
        notifyWith(e -> observerMethod.notify(e));
    }

    @Override
    public ObserverMethodConfigurator<T> read(Method method) {
        checkArgumentNotNull(method);
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
        checkArgumentNotNull(method);
        Set<AnnotatedParameter<?>> eventParameters = method.getParameters().stream()
                .filter((p) -> p.isAnnotationPresent(Observes.class) || p.isAnnotationPresent(ObservesAsync.class))
                .collect(Collectors.toSet());
        checkEventParams(eventParameters, method.getJavaMember());
        AnnotatedParameter<?> eventParameter = eventParameters.iterator().next();
        Observes observesAnnotation = eventParameter.getAnnotation(Observes.class);
        if (observesAnnotation != null) {
            reception(observesAnnotation.notifyObserver());
            transactionPhase(observesAnnotation.during());
            async(false);
        } else {
            reception(eventParameter.getAnnotation(ObservesAsync.class).notifyObserver());
            async(true);
        }
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
        checkArgumentNotNull(observerMethod);
        beanClass(observerMethod.getBeanClass());
        observedType(observerMethod.getObservedType());
        qualifiers(observerMethod.getObservedQualifiers());
        reception(observerMethod.getReception());
        transactionPhase(observerMethod.getTransactionPhase());
        priority(observerMethod.getPriority());
        async(observerMethod.isAsync());
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> beanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> observedType(Type type) {
        checkArgumentNotNull(type);
        if (observedType != null && !CovariantTypes.isAssignableFrom(observedType, type)) {
            EventLogger.LOG.originalObservedTypeIsNotAssignableFrom(observedType, type, extension);
        }
        observedType = type;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifier(Annotation qualifier) {
        checkArgumentNotNull(qualifier);
        this.observedQualifiers.add(qualifier);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifiers(Annotation... qualifiers) {
        checkArgumentNotNull(qualifiers);
        Collections.addAll(this.observedQualifiers, qualifiers);
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> addQualifiers(Set<Annotation> qualifiers) {
        checkArgumentNotNull(qualifiers);
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
        checkArgumentNotNull(reception);
        this.reception = reception;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> transactionPhase(TransactionPhase transactionPhase) {
        checkArgumentNotNull(transactionPhase);
        this.txPhase = transactionPhase;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> notifyWith(EventConsumer<T> callback) {
        checkArgumentNotNull(callback);
        this.notifyCallback = callback;
        return this;
    }

    @Override
    public ObserverMethodConfigurator<T> async(boolean async) {
        this.isAsync = async;
        return this;
    }

    @Override
    public ObserverMethod<T> complete() {
        return new ImmutableObserverMethod<>(this);
    }

    private <P> void checkEventParams(Set<P> eventParams, Method method) {
        if (eventParams.size() != 1) {
            EventLogger.LOG.noneOrMultipleEventParametersDeclared(method, Formats.formatAsStackTraceElement(method));
        }
    }

    /**
     *
     *
     * @param <T>
     */
    static class ImmutableObserverMethod<T> implements SyntheticObserverMethod<T> {

        private final Class<?> beanClass;

        private final Type observedType;

        private final Set<Annotation> observedQualifiers;

        private final Reception reception;

        private final TransactionPhase txPhase;

        private final int priority;

        private final boolean isAsync;

        private final EventConsumer<T> notifyCallback;

        /**
         *
         * @param configurator
         */
        ImmutableObserverMethod(ObserverMethodConfiguratorImpl<T> configurator) {
            if (configurator.notifyCallback == null) {
                throw EventLogger.LOG.notifyMethodNotImplemented(configurator);
            }
            this.beanClass = configurator.beanClass;
            this.observedType = configurator.observedType;
            this.observedQualifiers = ImmutableSet.copyOf(configurator.observedQualifiers);
            this.reception = configurator.reception;
            this.txPhase = configurator.txPhase;
            this.priority = configurator.priority;
            this.isAsync = configurator.isAsync;
            this.notifyCallback = configurator.notifyCallback;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Type getObservedType() {
            return observedType;
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return observedQualifiers;
        }

        @Override
        public Reception getReception() {
            return reception;
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return txPhase;
        }

        @Override
        public void notify(EventContext<T> eventContext) {
            try {
                notifyCallback.accept(eventContext);
            } catch (Exception e) {
                throw new ObserverException(e);
            }
        }

        @Override
        public boolean isAsync() {
            return isAsync;
        }

        @Override
        public boolean isEventMetadataRequired() {
            return true;
        }

        @Override
        public String toString() {
            return "Configurator observer method [Bean class = " + getBeanClass()
                    + ", type = " + getObservedType()
                    + ", qualifiers =" + Formats.formatAnnotations(getObservedQualifiers())
                    + ", priority =" + getPriority() + ", async =" + isAsync()
                    + ", reception =" + getReception() + ", transaction phase =" + getTransactionPhase() + "]";
        }

    }
}
