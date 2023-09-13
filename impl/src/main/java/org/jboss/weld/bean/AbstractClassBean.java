/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Producer;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Beans;

/**
 * An abstract bean representation common for class-based beans
 *
 * @param <T> the type of class for the bean
 * @author Pete Muir
 * @author David Allen
 * @author Jozef Hartinger
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>> implements DecorableBean<T>, ClassBean<T> {

    // The item representation
    protected final SlimAnnotatedType<T> annotatedType;
    protected volatile EnhancedAnnotatedType<T> enhancedAnnotatedItem;

    // Injection target for the bean
    private InjectionTarget<T> producer;

    /**
     * Constructor
     *
     * @param type The type
     * @param beanManager The Bean manager
     */
    protected AbstractClassBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, BeanIdentifier identifier,
            BeanManagerImpl beanManager) {
        super(attributes, identifier, beanManager);
        this.enhancedAnnotatedItem = type;
        this.annotatedType = type.slim();
        initType();
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        checkBeanImplementation();
    }

    public boolean hasDecorators() {
        return !getDecorators().isEmpty();
    }

    @Override
    public List<Decorator<?>> getDecorators() {
        if (isInterceptionCandidate()) {
            return beanManager.resolveDecorators(getTypes(), getQualifiers());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Initializes the bean type
     */
    protected void initType() {
        this.type = getEnhancedAnnotated().getJavaClass();
    }

    /**
     * Validates the bean implementation
     */
    protected void checkBeanImplementation() {
    }

    @Override
    protected void preSpecialize() {
        super.preSpecialize();
        Class<?> superclass = getAnnotated().getJavaClass().getSuperclass();
        if (superclass == null || superclass.equals(Object.class)) {
            throw BeanLogger.LOG.specializingBeanMustExtendABean(this);
        }
    }

    @Override
    public SlimAnnotatedType<T> getAnnotated() {
        return annotatedType;
    }

    /**
     * Gets the annotated item
     *
     * @return The annotated item
     */
    @Override
    public EnhancedAnnotatedType<T> getEnhancedAnnotated() {
        return Beans.checkEnhancedAnnotatedAvailable(enhancedAnnotatedItem);
    }

    @Override
    public void cleanupAfterBoot() {
        this.enhancedAnnotatedItem = null;
    }

    protected abstract boolean isInterceptionCandidate();

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return getProducer().getInjectionPoints();
    }

    public InterceptionModel getInterceptors() {
        if (isInterceptionCandidate()) {
            return beanManager.getInterceptorModelRegistry().get(getAnnotated());
        } else {
            return null;
        }
    }

    public boolean hasInterceptors() {
        return getInterceptors() != null;
    }

    @Override
    public InjectionTarget<T> getProducer() {
        return producer;
    }

    public void setProducer(InjectionTarget<T> producer) {
        this.producer = producer;
    }

    /**
     * Duplicate of {@link #getProducer()} - kept for backwards compatibility.
     */
    public InjectionTarget<T> getInjectionTarget() {
        return getProducer();
    }

    public void setInjectionTarget(InjectionTarget<T> injectionTarget) {
        setProducer(injectionTarget);
    }

    @Override
    public void setProducer(Producer<T> producer) {
        throw new IllegalArgumentException(
                "Class bean " + this + " requires an InjectionTarget but a Producer was provided instead " + producer);
    }
}
