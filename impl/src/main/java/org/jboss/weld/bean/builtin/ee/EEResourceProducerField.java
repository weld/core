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
package org.jboss.weld.bean.builtin.ee;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.injection.ResourceInjectionFactory;
import org.jboss.weld.injection.ResourceInjectionProcessor;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 */
public class EEResourceProducerField<X, T> extends ProducerField<X, T> {

    private static class EEResourceCallable<T> extends AbstractEECallable<T> {

        private static final long serialVersionUID = 6287931036073200963L;

        private final BeanIdentifier beanId;
        private transient T instance;
        private final CreationalContext<T> creationalContext;

        private EEResourceCallable(BeanManagerImpl beanManager, ProducerField<?, T> producerField,
                CreationalContext<T> creationalContext, T instance) {
            super(beanManager);
            this.beanId = producerField.getIdentifier();
            this.creationalContext = creationalContext;
            this.instance = instance;
        }

        public T call() throws Exception {
            if (instance == null) {
                Contextual<T> contextual = getBeanManager().getServices().get(ContextualStore.class)
                        .<Contextual<T>, T> getContextual(beanId);
                if (contextual instanceof EEResourceProducerField<?, ?>) {
                    this.instance = Reflections.<EEResourceProducerField<?, T>> cast(contextual)
                            .createUnderlying(creationalContext);
                } else {
                    throw BeanLogger.LOG.beanNotEeResourceProducer(contextual);
                }
            }
            return instance;
        }

        @Override
        public String toString() {
            return instance == null ? "null" : instance.toString();
        }

    }

    /**
     * Creates an EE resource producer field
     *
     * @param field The underlying method abstraction
     * @param declaringBean The declaring bean abstraction
     * @param manager the current manager
     * @return A producer field
     */
    public static <X, T> EEResourceProducerField<X, T> of(BeanAttributes<T> attributes,
            EnhancedAnnotatedField<T, ? super X> field, AbstractClassBean<X> declaringBean, DisposalMethod<X, ?> disposalMethod,
            BeanManagerImpl manager, ServiceRegistry services) {
        return new EEResourceProducerField<X, T>(attributes, field, declaringBean, disposalMethod, manager, services);
    }

    /**
     *
     * @param beanManager
     * @param field
     * @return <code>true</code> if the given field is annotated with an EE resource annotation, <code>false</code> otherwise
     */
    public static boolean isEEResourceProducerField(BeanManagerImpl beanManager, AnnotatedField<?> field) {
        final ResourceInjectionFactory factory = beanManager.getServices().get(ResourceInjectionFactory.class);
        for (ResourceInjectionProcessor<?, ?> processor : factory) {
            if (field.isAnnotationPresent(processor.getMarkerAnnotation(beanManager))) {
                return true;
            }
        }
        return false;
    }

    private ProxyFactory<T> proxyFactory;

    private final Class<T> rawType;

    protected EEResourceProducerField(BeanAttributes<T> attributes, EnhancedAnnotatedField<T, ? super X> field,
            AbstractClassBean<X> declaringBean, DisposalMethod<X, ?> disposalMethod, BeanManagerImpl manager,
            ServiceRegistry services) {
        super(attributes, field, declaringBean, disposalMethod, manager, services);
        this.rawType = field.getJavaClass();
    }

    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        checkEEResource();
        proxyFactory = new ProxyFactory<T>(getBeanManager().getContextId(), getType(), getTypes(), this);
    }

    protected void checkEEResource() {
        if (!getScope().equals(Dependent.class)) {
            throw BeanLogger.LOG.nonDependentResourceProducerField(this);
        }
        if (getName() != null) {
            throw BeanLogger.LOG.namedResourceProducerField(this);
        }
        if (!isEEResourceProducerField(beanManager, getAnnotated())) {
            throw BeanLogger.LOG.invalidResourceProducerField(getAnnotated());
        }
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        final T beanInstance = getProducer().produce(creationalContext);
        if (Reflections.isFinal(rawType) || Serializable.class.isAssignableFrom(beanInstance.getClass())) {
            return checkReturnValue(beanInstance);
        } else {
            BeanInstance proxyBeanInstance = new EnterpriseTargetBeanInstance(getTypes(),
                    new CallableMethodHandler(new EEResourceCallable<T>(getBeanManager(),
                            this, creationalContext, beanInstance)));
            return checkReturnValue(proxyFactory.create(proxyBeanInstance));
        }
    }

    /**
     * Access to the underlying producer field, performs return value check.
     */
    private T createUnderlying(CreationalContext<T> creationalContext) {
        return super.create(creationalContext);
    }

    @Override
    public boolean isPassivationCapableBean() {
        return true;
    }

    @Override
    public String toString() {
        return "Resource " + super.toString();
    }

}
