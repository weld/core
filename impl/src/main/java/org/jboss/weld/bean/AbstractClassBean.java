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

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Producer;
import jakarta.enterprise.invoke.Invokable;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Beans;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    protected Collection<AnnotatedMethod<? super T>> invokableMethods;

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
        initInvokableMethods();
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

    private void initInvokableMethods() {
        invokableMethods = new HashSet<>();
        // this collection is used to detect overriden methods; return type checking should not be needed in this case
        Collection<MethodSignature> encounteredMethods = new HashSet<>();
        MetaAnnotationStore metaAnnotationStore = getBeanManager().getServices().get(MetaAnnotationStore.class);

        EnhancedAnnotatedType<? super T> type = enhancedAnnotatedItem;
        while (type != null) {
            // inspect all class-level annotations and look for any invokable annotation
            boolean hasClassLevelInvokableAnnotation = false;
            for (Annotation an : type.getAnnotations()) {
                if (isInvokableAnnotation(an.annotationType(), metaAnnotationStore)) {
                    hasClassLevelInvokableAnnotation = true;
                    break;
                }
            }

            // iterate over all methods, if they belong to this type and either have the annotation or we know of a class
            // level invokable annotation, we register them
            for (AnnotatedMethod<? super T> method : type.getMethods()) {
                if (!method.getDeclaringType().equals(type)) {
                    continue;
                }
                MethodSignature signature = MethodSignatureImpl.of(method);
                if (!encounteredMethods.contains(signature) &&
                        (hasClassLevelInvokableAnnotation || method.getAnnotations().stream().anyMatch(a -> isInvokableAnnotation(a.annotationType(), metaAnnotationStore)))) {
                    invokableMethods.add(method);
                }
                encounteredMethods.add(signature);
            }

            // inspect super class in the same fashion
            type = type.getEnhancedSuperclass();
        }
    }

    /**
     * Checks if the given annotation, or any annotation declared on this annotation, is {@link Invokable}.
     *
     * @param invokableCandidate annotation class to inspect
     * @return true if the annotation is considered {@link Invokable}, false otherwise
     */
    private boolean isInvokableAnnotation(Class<? extends Annotation> invokableCandidate, MetaAnnotationStore metaAnnotationStore) {
        if (invokableCandidate.equals(Invokable.class)) {
            return true;
        } else {
            // validity here means that the annotation contains @Invokable meta-annotation
            // this accounts for both, actually present annotation in code, and added via extension
            if (metaAnnotationStore.getInvokableModel(invokableCandidate).isValid()) {
                return true;
            }
        }
        return false;
    }
}
