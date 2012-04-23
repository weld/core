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

import static org.jboss.weld.logging.messages.BeanMessage.INCONSISTENT_ANNOTATIONS_ON_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.METHOD_NOT_BUSINESS_METHOD;
import static org.jboss.weld.logging.messages.BeanMessage.PRODUCER_METHOD_NOT_SPECIALIZING;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Represents a producer method bean
 *
 * @param <T>
 * @author Pete Muir
 */
public class ProducerMethod<X, T> extends AbstractProducerBean<X, T, Method> {
    // The underlying method
    private MethodInjectionPoint<T, ? super X> method;
    private ProducerMethod<?, ?> specializedBean;
    private final boolean proxiable;

    private volatile EnhancedAnnotatedMethod<T, ? super X> enhancedAnnotatedMethod;

    /**
     * Creates a producer method Web Bean
     *
     * @param method        The underlying method abstraction
     * @param declaringBean The declaring bean abstraction
     * @param beanManager   the current manager
     * @return A producer Web Bean
     */
    public static <X, T> ProducerMethod<X, T> of(BeanAttributes<T> attributes, EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean, BeanManagerImpl beanManager, ServiceRegistry services) {
        return new ProducerMethod<X, T>(attributes, method, declaringBean, beanManager, services);
    }

    protected ProducerMethod(BeanAttributes<T> attributes, EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean, BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, createId(method, declaringBean), declaringBean, beanManager, services);
        this.enhancedAnnotatedMethod = method;
        this.method = MethodInjectionPoint.of(method, this, beanManager);
        initType();
        initProducerMethodInjectableParameters();
        this.proxiable = Proxies.isTypesProxyable(method.getTypeClosure());
    }

    protected static <T, X> String createId(EnhancedAnnotatedMethod<T, ? super X> method, AbstractClassBean<X> declaringBean) {
        if (declaringBean.getEnhancedAnnotated().isDiscovered()) {
            StringBuilder sb = new StringBuilder();
            sb.append(BEAN_ID_PREFIX);
            sb.append(ProducerMethod.class.getSimpleName());
            sb.append(BEAN_ID_SEPARATOR);
            sb.append(declaringBean.getEnhancedAnnotated().getName());
            sb.append(method.getSignature().toString());
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(BEAN_ID_PREFIX);
            sb.append(ProducerMethod.class.getSimpleName());
            sb.append(BEAN_ID_SEPARATOR);
            sb.append(AnnotatedTypes.createTypeId(declaringBean.getEnhancedAnnotated()));
            sb.append(AnnotatedTypes.createCallableId(method));
            return sb.toString();
        }

    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        checkProducerMethod();
        setProducer(new AbstractProducer() {

            public T produce(Object receiver, CreationalContext<T> creationalContext) {
                if (receiver != null) {
                    return method.invokeOnInstance(receiver, beanManager, creationalContext, CreationException.class);
                } else {
                    return method.invoke(null, beanManager, creationalContext, CreationException.class);
                }
            }

            public String toString() {
                return method.toString();
            }
        });
    }

    /**
     * Initializes the injection points
     */
    protected void initProducerMethodInjectableParameters() {
        for (WeldInjectionPoint<?, ?> ip : method.getParameterInjectionPoints()) {
            addInjectionPoint(ip);
        }
    }

    /**
     * Validates the producer method
     */
    protected void checkProducerMethod() {
        if (getEnhancedAnnotated().getEnhancedParameters(Observes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Observes");
        } else if (getEnhancedAnnotated().getEnhancedParameters(Disposes.class).size() > 0) {
            throw new DefinitionException(INCONSISTENT_ANNOTATIONS_ON_METHOD, "@Produces", "@Disposes");
        } else if (getDeclaringBean() instanceof SessionBean<?>) {
            boolean methodDeclaredOnTypes = false;
            // TODO use annotated item?
            for (Type type : getDeclaringBean().getTypes()) {
                if (type instanceof Class<?>) {
                    if (SecureReflections.isMethodExists((Class<?>) type, getEnhancedAnnotated().getName(), getEnhancedAnnotated().getParameterTypesAsArray())) {
                        methodDeclaredOnTypes = true;
                        continue;
                    }
                }
            }
            if (!methodDeclaredOnTypes) {
                throw new DefinitionException(METHOD_NOT_BUSINESS_METHOD, this, getDeclaringBean());
            }
        }
    }

    @Override
    public AnnotatedMethod<? super X> getAnnotated() {
        return method.getAnnotated();
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
        super.cleanupAfterBoot();
        this.enhancedAnnotatedMethod = null;
    }

    @Override
    public AbstractBean<?, ?> getSpecializedBean() {
        return specializedBean;
    }

    @Override
    protected void preSpecialize() {
        if (getDeclaringBean().getEnhancedAnnotated().getEnhancedSuperclass().getDeclaredEnhancedMethod(getEnhancedAnnotated().getJavaMember()) == null) {
            throw new DefinitionException(PRODUCER_METHOD_NOT_SPECIALIZING, this);
        }
    }

    @Override
    protected void specialize() {
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        EnhancedAnnotatedMethod<?, ?> superClassMethod = getDeclaringBean().getEnhancedAnnotated().getEnhancedSuperclass().getEnhancedMethod(getEnhancedAnnotated().getJavaMember());
        ProducerMethod<?, ?> check = closure.getProducerMethod(superClassMethod);
        if (check == null) {
            throw new IllegalStateException(PRODUCER_METHOD_NOT_SPECIALIZING, this);
        }
        this.specializedBean = check;
    }

    @Override
    public String toString() {
        return "Producer Method [" + Formats.formatType(getAnnotated().getBaseType()) + "] with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "] declared as [" + getAnnotated() + "]";
    }

    @Override
    public boolean isProxyable() {
        return proxiable;
    }
}
