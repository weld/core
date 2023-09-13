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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMember;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.CurrentInjectionPoint;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Defaults;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

/**
 * The implicit producer bean
 *
 * @param <X>
 * @param <T>
 * @param <S>
 * @author Gavin King
 * @author David Allen
 * @author Jozef Hartinger
 */
public abstract class AbstractProducerBean<X, T, S extends Member> extends AbstractBean<T, S> {

    private final AbstractClassBean<X> declaringBean;

    // Passivation flags
    private boolean passivationCapableBean;
    private boolean passivationCapableDependency;

    /**
     * Constructor
     *
     * @param declaringBean The declaring bean
     * @param beanManager The Bean manager
     */
    public AbstractProducerBean(BeanAttributes<T> attributes, BeanIdentifier identifier, AbstractClassBean<X> declaringBean,
            BeanManagerImpl beanManager, ServiceRegistry services) {
        super(attributes, identifier, beanManager);
        this.declaringBean = declaringBean;
    }

    @Override
    // Overridden to provide the class of the bean that declares the producer
    // method/field
    public Class<?> getBeanClass() {
        return getDeclaringBean().getBeanClass();
    }

    /**
     * Initializes the type
     */
    protected void initType() {
        try {
            this.type = getEnhancedAnnotated().getJavaClass();
        } catch (ClassCastException e) {
            Type type = Beans.getDeclaredBeanType(getClass());
            throw BeanLogger.LOG.producerCastError(getEnhancedAnnotated().getJavaClass(), (type == null ? " unknown " : type),
                    e);
        }
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        getDeclaringBean().initialize(environment);
        super.internalInitialize(environment);
        initPassivationCapable();
    }

    private void initPassivationCapable() {
        this.passivationCapableBean = !Reflections.isFinal(getEnhancedAnnotated().getJavaClass())
                || Reflections.isSerializable(getEnhancedAnnotated().getJavaClass());
        if (isNormalScoped()) {
            this.passivationCapableDependency = true;
        } else if (getScope().equals(Dependent.class) && passivationCapableBean) {
            this.passivationCapableDependency = true;
        } else {
            this.passivationCapableDependency = false;
        }
    }

    @Override
    public boolean isPassivationCapableBean() {
        return passivationCapableBean;
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return passivationCapableDependency;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return getProducer().getInjectionPoints();
    }

    /**
     * Validates the return value
     *
     * @param instance The instance to validate
     */
    protected T checkReturnValue(T instance) {
        if (instance == null && !isDependent()) {
            throw BeanLogger.LOG.nullNotAllowedFromProducer(getProducer(),
                    Formats.formatAsStackTraceElement(getAnnotated().getJavaMember()));
        }
        if (instance == null) {
            InjectionPoint injectionPoint = beanManager.getServices().get(CurrentInjectionPoint.class).peek();
            if (injectionPoint != null) {
                Class<?> injectionPointRawType = Reflections.getRawType(injectionPoint.getType());
                if (injectionPointRawType.isPrimitive()) {
                    return cast(Defaults.getJlsDefaultValue(injectionPointRawType));
                }
            }
        }
        if (instance != null && !(instance instanceof Serializable)) {
            if (beanManager.isPassivatingScope(getScope())) {
                throw BeanLogger.LOG.nonSerializableProductError(getProducer(),
                        Formats.formatAsStackTraceElement(getAnnotated().getJavaMember()));
            }
            InjectionPoint injectionPoint = beanManager.getServices().get(CurrentInjectionPoint.class).peek();
            if (injectionPoint != null && injectionPoint.getBean() != null
                    && Beans.isPassivatingScope(injectionPoint.getBean(), beanManager)) {
                // Transient field is passivation capable injection point
                if (!(injectionPoint.getMember() instanceof Field) || !injectionPoint.isTransient()) {
                    throw BeanLogger.LOG.unserializableProductInjectionError(this,
                            Formats.formatAsStackTraceElement(getAnnotated().getJavaMember()),
                            injectionPoint, Formats.formatAsStackTraceElement(injectionPoint.getMember()));
                }
            }
        }
        return instance;
    }

    @Override
    protected void checkType() {
        if (beanManager.isPassivatingScope(getScope()) && !isPassivationCapableBean()) {
            throw BeanLogger.LOG.passivatingBeanNeedsSerializableImpl(this);
        }
    }

    protected boolean isTypeSerializable(final Object instance) {
        return instance instanceof Serializable;
    }

    /**
     * Creates an instance of the bean
     *
     * @returns The instance
     */
    public T create(final CreationalContext<T> creationalContext) {
        T instance = getProducer().produce(creationalContext);
        instance = checkReturnValue(instance);
        return instance;
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        super.destroy(instance, creationalContext);
        try {
            getProducer().dispose(instance);
        } catch (Exception e) {
            BeanLogger.LOG.errorDestroying(instance, this);
            BeanLogger.LOG.catchingDebug(e);
        } finally {
            if (getDeclaringBean().isDependent()) {
                creationalContext.release();
            }
        }
    }

    /**
     * Returns the declaring bean
     *
     * @return The bean representation
     */
    public AbstractClassBean<X> getDeclaringBean() {
        return declaringBean;
    }

    @Override
    public abstract AnnotatedMember<? super X> getAnnotated();

    @Override
    public abstract EnhancedAnnotatedMember<T, ?, S> getEnhancedAnnotated();
}
