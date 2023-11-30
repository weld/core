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
package org.jboss.weld.bean;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.producer.ProducerMethodProducer;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;

/**
 * Represents a producer method bean
 *
 * @param <T>
 * @author Pete Muir
 */
public class ProducerMethod<X, T> extends AbstractProducerBean<X, T, Method> {

    private final boolean proxiable;

    private final AnnotatedMethod<? super X> annotatedMethod;
    private volatile EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod;

    /**
     * Creates a producer method Web Bean
     *
     * @param method The underlying method abstraction
     * @param declaringBean The declaring bean abstraction
     * @param beanManager the current manager
     * @return A producer Web Bean
     */
    public static <X, T> ProducerMethod<X, T> of(BeanAttributes<T> attributes, EnhancedAnnotatedMethod<T, ? super X> method,
            AbstractClassBean<X> declaringBean, DisposalMethod<X, ?> disposalMethod, BeanManagerImpl beanManager,
            ServiceRegistry services) {
        return new ProducerMethod<X, T>(createId(attributes, method, declaringBean), attributes, method, declaringBean,
                disposalMethod, beanManager, services);
    }

    private static BeanIdentifier createId(BeanAttributes<?> attributes, EnhancedAnnotatedMethod<?, ?> method,
            AbstractClassBean<?> declaringBean) {
        if (Dependent.class.equals(attributes.getScope()) || ApplicationScoped.class.equals(attributes.getScope())) {
            return new ProducerMethodIdentifier(method, declaringBean);
        } else {
            return new StringBeanIdentifier(BeanIdentifiers.forProducerMethod(method, declaringBean));
        }
    }

    protected ProducerMethod(BeanIdentifier identifier, BeanAttributes<T> attributes,
            EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean,
            DisposalMethod<X, ?> disposalMethod, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, identifier, declaringBean, beanManager, services);
        this.enhancedAnnotatedMethod = method;
        this.annotatedMethod = method.slim();
        initType();
        this.proxiable = Proxies.isTypesProxyable(method.getTypeClosure(), beanManager.getServices());
        setProducer(new ProducerMethodProducer<X, T>(method, disposalMethod) {

            @Override
            public BeanManagerImpl getBeanManager() {
                return ProducerMethod.this.beanManager;
            }

            @Override
            public Bean<X> getDeclaringBean() {
                return ProducerMethod.this.getDeclaringBean();
            }

            @Override
            public Bean<T> getBean() {
                return ProducerMethod.this;
            }
        });
        processExplicitPriority();
    }

    @Override
    public AnnotatedMethod<? super X> getAnnotated() {
        return annotatedMethod;
    }

    /**
     * Gets the annotated item representing the method
     *
     * @return The annotated item
     */
    @Override
    public EnhancedAnnotatedMethod<T, ? super X> getEnhancedAnnotated() {
        return Beans.checkEnhancedAnnotatedAvailable(enhancedAnnotatedMethod);
    }

    @Override
    public void cleanupAfterBoot() {
        this.enhancedAnnotatedMethod = null;
    }

    @Override
    protected void specialize() {
        Set<? extends AbstractBean<?, ?>> specializedBeans = getSpecializedBeans();
        if (specializedBeans.isEmpty()) {
            throw BeanLogger.LOG.producerMethodNotSpecializing(this,
                    Formats.formatAsStackTraceElement(annotatedMethod.getJavaMember()));
        }
    }

    @Override
    public String toString() {
        return "Producer Method [" + Formats.formatType(getAnnotated().getBaseType()) + "] with qualifiers ["
                + Formats.formatAnnotations(getQualifiers()) + "] declared as [" + getAnnotated() + "]";
    }

    @Override
    public boolean isProxyable() {
        return proxiable;
    }
}
