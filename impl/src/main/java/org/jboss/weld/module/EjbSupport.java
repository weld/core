/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.module;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * This service provides EJB support. It is implemented by the weld-ejb module. This is a per-archive service.
 *
 * @author Jozef Hartinger
 *
 */
public interface EjbSupport extends Service {

    /**
     * Creates a {@link BeanAttributes} object for a session bean from the given annotated type and ejb descriptor.
     *
     * @param type annotated type that defines the session bean
     * @param descriptor session bean descriptor
     * @param manager the bean manager
     * @return BeanAttributes representation of a given session bean
     */
    <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> type, BeanManagerImpl manager);

    /**
     * Creates an {@link InjectionTarget} implementation for a given session bean.
     *
     * @param type annotated type that defines the session bean
     * @param descriptor session bean descriptor
     * @param manager the bean manager
     * @return InjectionTarget implementation for a given session bean
     */
    <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean,
            BeanManagerImpl manager);

    /**
     * Creates an {@link InjectionTarget} implementation for a message-driven bean.
     *
     * @param type annotated type that defines the message-driven bean
     * @param descriptor message-driven bean descriptor
     * @param manager the bean manager
     * @return InjectionTarget implementation for a given message-driven bean
     */
    <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type, EjbDescriptor<T> descriptor,
            BeanManagerImpl manager);

    /**
     * Creates session beans and registers them within the given environment.
     *
     * @param environment
     * @param classes
     * @param manager
     */
    void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> classes,
            BeanManagerImpl manager);

    /**
     * Returns the class object for the {@link jakarta.ejb.Timeout} annotation.
     *
     * @return the class object for the Timeout annotation or null if the annotation is not present
     */
    Class<? extends Annotation> getTimeoutAnnotation();

    /**
     * Initializes interception model for MDBs and propagates them to
     * {@link EjbServices#registerInterceptors(org.jboss.weld.ejb.spi.EjbDescriptor, org.jboss.weld.ejb.spi.InterceptorBindings)}.
     *
     * @param environment
     * @param manager
     */
    void registerCdiInterceptorsForMessageDrivenBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager);

    /**
     * Indicates whether an {@link EjbDescriptor} is known for a given class.
     *
     * @param beanClass
     * @return true if an EjbDescriptor for the given class exists
     */
    boolean isEjb(Class<?> beanClass);

    /**
     * Returns an {@link EjbDescriptor} identified by the given name or null if no such descriptor exists.
     *
     * @param beanName
     * @return descriptor identified by the given name or null if no such descriptor exists
     */
    <T> EjbDescriptor<T> getEjbDescriptor(String beanName);

    /**
     * Returns a collection of all known EJB descriptors
     *
     * @return a collection of all known EJB descriptors
     */
    Collection<? extends EjbDescriptor<?>> getEjbDescriptors();

    /**
     * @param instance
     * @return <code>true</code> if the given instance represents an internal reference to a session bean (proxy),
     *         <code>false</code> otherwise
     */
    boolean isSessionBeanProxy(Object instance);

    EjbSupport NOOP_IMPLEMENTATION = new EjbSupport() {

        @Override
        public void cleanup() {
        }

        private <T> T fail() {
            throw new IllegalStateException("Cannot process session bean. weld-ejb module not registered.");
        }

        @Override
        public <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean,
                BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> type, BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type,
                EjbDescriptor<T> descriptor,
                BeanManagerImpl manager) {
            return fail();
        }

        @Override
        public void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> classes,
                BeanManagerImpl manager) {
        }

        @Override
        public Class<? extends Annotation> getTimeoutAnnotation() {
            return null;
        }

        @Override
        public void registerCdiInterceptorsForMessageDrivenBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager) {
        }

        @Override
        public Collection<EjbDescriptor<?>> getEjbDescriptors() {
            return Collections.emptyList();
        }

        @Override
        public boolean isEjb(Class<?> beanClass) {
            return false;
        }

        @Override
        public <T> EjbDescriptor<T> getEjbDescriptor(String beanName) {
            return null;
        }

        @Override
        public boolean isSessionBeanProxy(Object instance) {
            return false;
        }

    };
}
