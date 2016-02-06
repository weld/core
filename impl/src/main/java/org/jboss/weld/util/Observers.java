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
import java.util.Set;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.ProcessSessionBean;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;

import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.event.ContainerLifecycleEventObserverMethod;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.experimental.ExperimentalProcessObserverMethod;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 */
public class Observers {

    /*
     * Contains all container lifecycle event types
     */
    public static final Set<Class<?>> CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES = ImmutableSet.of(
            BeforeBeanDiscovery.class,
            AfterTypeDiscovery.class,
            AfterBeanDiscovery.class,
            AfterDeploymentValidation.class,
            BeforeShutdown.class,
            ProcessAnnotatedType.class,
            ProcessInjectionPoint.class,
            ProcessInjectionTarget.class,
            ProcessProducer.class,
            ProcessBeanAttributes.class,
            ProcessBean.class,
            ProcessObserverMethod.class);
    /*
     * Contains only top superinterfaces of each chain of container lifecycle event types.
     */
    public static final Set<Class<?>> CONTAINER_LIFECYCLE_EVENT_TYPES = ImmutableSet.<Class<?>>builder()
            .addAll(CONTAINER_LIFECYCLE_EVENT_CANONICAL_SUPERTYPES)
            .addAll(
                    ProcessSyntheticAnnotatedType.class,
                    ProcessSessionBean.class,
                    ProcessManagedBean.class,
                    ProcessProducerMethod.class,
                    ProcessProducerField.class,
                    ExperimentalProcessObserverMethod.class
            ).build();

    private Observers() {
    }

    public static boolean isContainerLifecycleObserverMethod(ObserverMethod<?> method) {
        return CONTAINER_LIFECYCLE_EVENT_TYPES.contains(Reflections.getRawType(method.getObservedType()))
                || (Object.class.equals(method.getObservedType()) && method instanceof ContainerLifecycleEventObserverMethod);
    }

    public static boolean isObserverMethodEnabled(ObserverMethod<?> method, BeanManagerImpl manager) {
        if (method instanceof ObserverMethodImpl<?, ?>) {
            Bean<?> declaringBean = Reflections.<ObserverMethodImpl<?, ?>> cast(method).getDeclaringBean();
            return manager.getServices().get(SpecializationAndEnablementRegistry.class).isCandidateForLifecycleEvent(declaringBean);
        }
        return true;
    }

    /**
     * Validates given external observer method.
     * @param observerMethod the given observer method
     * @param beanManager
     * @param originalObserverMethod observer method replaced by given observer method (this parameter is optional)
     */
    public static void validateObserverMethod(ObserverMethod<?> observerMethod, BeanManager beanManager, ObserverMethod<?> originalObserverMethod) {
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
    }

    /**
     * Determines whether the given observer method is either extension-provided or contains an injection point with {@link EventMetadata} type.
     */
    public static boolean isEventMetadataRequired(ObserverMethod<?> observer) {
        if (observer instanceof ObserverMethodImpl<?, ?>) {
            ObserverMethodImpl<?, ?> observerImpl = (ObserverMethodImpl<?, ?>) observer;
            return observerImpl.isEventMetadataRequired();
        } else {
            return true;
        }
    }
}
