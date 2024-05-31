/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSessionBean;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;
import jakarta.enterprise.inject.spi.ProcessSyntheticObserverMethod;

import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.bootstrap.event.WeldAfterBeanDiscovery;
import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.event.EventMetadataAwareObserverMethod;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.event.SyntheticObserverMethod;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 */
public class Observers {

    /*
     * Contains only top superinterfaces of each chain of container lifecycle event types.
     */
    public static final Set<Class<?>> CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES = ImmutableSet.of(
            BeforeBeanDiscovery.class, AfterTypeDiscovery.class,
            AfterBeanDiscovery.class, AfterDeploymentValidation.class, BeforeShutdown.class, ProcessAnnotatedType.class,
            ProcessInjectionPoint.class,
            ProcessInjectionTarget.class, ProcessProducer.class, ProcessBeanAttributes.class, ProcessBean.class,
            ProcessObserverMethod.class);
    /*
     * Contains all container lifecycle event types
     */
    public static final Set<Class<?>> CONTAINER_LIFECYCLE_EVENT_TYPES = ImmutableSet.<Class<?>> builder()
            .addAll(CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES)
            .addAll(ProcessSyntheticAnnotatedType.class, ProcessSessionBean.class, ProcessManagedBean.class,
                    ProcessProducerMethod.class, ProcessProducerField.class, ProcessSyntheticBean.class,
                    ProcessSyntheticObserverMethod.class, WeldAfterBeanDiscovery.class, WeldProcessManagedBean.class)
            .build();

    private static final String NOTIFY_METHOD_NAME = "notify";

    private Observers() {
    }

    public static boolean isContainerLifecycleObserverMethod(ObserverMethod<?> method) {
        // case when the observed type clearly belongs to predefined set of types which make it a container lifecycle observer
        if (CONTAINER_LIFECYCLE_EVENT_TYPES.contains(Reflections.getRawType(method.getObservedType()))) {
            return true;
        }
        // the observer is in extension and looks something like this -> @Observes Object ob
        // then there are two cases in which we considerer such observer a container event observer
        if (Object.class.equals(method.getObservedType()) && method instanceof ContainerLifecycleEventObserverMethod) {

            // public void observe (@Observes Object ob){...} - this IS container event observer
            if (method.getObservedQualifiers().isEmpty()) {
                return true;
            }

            // public void observe (@Observes @Any Object ob){...} - this IS container event observer
            if (method.getObservedQualifiers().size() == 1 && method.getObservedQualifiers().contains(Any.Literal.INSTANCE)) {
                return true;
            }
        }
        // if none of the above fits, we are safe to say such observer is not a container event observer
        return false;
    }

    public static boolean isObserverMethodEnabled(ObserverMethod<?> method, BeanManagerImpl manager) {
        if (method instanceof ObserverMethodImpl<?, ?>) {
            Bean<?> declaringBean = Reflections.<ObserverMethodImpl<?, ?>> cast(method).getDeclaringBean();
            return manager.getServices().get(SpecializationAndEnablementRegistry.class)
                    .isCandidateForLifecycleEvent(declaringBean);
        }
        return true;
    }

    /**
     * Validates given external observer method.
     *
     * @param observerMethod the given observer method
     * @param beanManager
     * @param originalObserverMethod observer method replaced by given observer method (this parameter is optional)
     */
    public static void validateObserverMethod(ObserverMethod<?> observerMethod, BeanManager beanManager,
            ObserverMethod<?> originalObserverMethod) {
        Set<Annotation> qualifiers = observerMethod.getObservedQualifiers();
        if (observerMethod.getBeanClass() == null) {
            throw EventLogger.LOG.observerMethodsMethodReturnsNull("getBeanClass", observerMethod);
        }
        if (observerMethod.getObservedType() == null) {
            throw EventLogger.LOG.observerMethodsMethodReturnsNull("getObservedType", observerMethod);
        }
        Bindings.validateQualifiers(qualifiers, beanManager, observerMethod, "ObserverMethod.getObservedQualifiers");
        if (observerMethod.getReception() == null) {
            throw EventLogger.LOG.observerMethodsMethodReturnsNull("getReception", observerMethod);
        }
        if (observerMethod.getTransactionPhase() == null) {
            throw EventLogger.LOG.observerMethodsMethodReturnsNull("getTransactionPhase", observerMethod);
        }
        if (originalObserverMethod != null && (!observerMethod.getBeanClass().equals(originalObserverMethod.getBeanClass()))) {
            throw EventLogger.LOG.beanClassMismatch(originalObserverMethod, observerMethod);
        }
        if (!(observerMethod instanceof SyntheticObserverMethod)
                && !hasNotifyOverriden(observerMethod.getClass(), observerMethod)) {
            throw EventLogger.LOG.notifyMethodNotImplemented(observerMethod);
        }
    }

    /**
     * Determines whether the given observer method is either extension-provided or contains an injection point with
     * {@link EventMetadata} type.
     */
    public static boolean isEventMetadataRequired(ObserverMethod<?> observer) {
        if (observer instanceof EventMetadataAwareObserverMethod) {
            EventMetadataAwareObserverMethod<?> eventMetadataAware = (EventMetadataAwareObserverMethod<?>) observer;
            return eventMetadataAware.isEventMetadataRequired();
        } else {
            return true;
        }
    }

    /**
     *
     * @param observerMethod
     * @param event
     * @param metadata May be null
     */
    public static <T> void notify(ObserverMethod<? super T> observerMethod, T event, EventMetadata metadata) {
        observerMethod.notify(new EventContextImpl<>(event, metadata));
    }

    private static boolean hasNotifyOverriden(Class<?> clazz, ObserverMethod<?> observerMethod) {
        if (clazz.isInterface()) {
            return false;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (NOTIFY_METHOD_NAME.equals(method.getName()) && method.getParameterCount() == 1) {
                return true;
            }
        }
        return clazz.getSuperclass() != null ? hasNotifyOverriden(clazz.getSuperclass(), observerMethod) : false;
    }

    static class EventContextImpl<T> implements EventContext<T> {

        private final T event;

        private final EventMetadata metadata;

        EventContextImpl(T event, EventMetadata metadata) {
            this.event = event;
            this.metadata = metadata;
        }

        @Override
        public T getEvent() {
            return event;
        }

        @Override
        public EventMetadata getMetadata() {
            return metadata;
        }

    }
}
