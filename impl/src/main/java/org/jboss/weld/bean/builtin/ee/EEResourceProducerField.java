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

import static org.jboss.weld.logging.messages.BeanMessage.BEAN_NOT_EE_RESOURCE_PRODUCER;
import static org.jboss.weld.logging.messages.BeanMessage.INVALID_RESOURCE_PRODUCER_FIELD;
import static org.jboss.weld.logging.messages.BeanMessage.NAMED_RESOURCE_PRODUCER_FIELD;
import static org.jboss.weld.logging.messages.BeanMessage.NON_DEPENDENT_RESOURCE_PRODUCER_FIELD;

import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.bean.proxy.BeanInstance;
import org.jboss.weld.bean.proxy.EnterpriseTargetBeanInstance;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;

/**
 * @author pmuir
 */
public class EEResourceProducerField<X, T> extends ProducerField<X, T> {

    private static class EEResourceCallable<T> extends AbstractEECallable<T> {

        private static final long serialVersionUID = 6287931036073200963L;

        private final String beanId;
        private transient T instance;
        private final CreationalContext<T> creationalContext;

        private EEResourceCallable(BeanManagerImpl beanManager, ProducerField<?, T> producerField, CreationalContext<T> creationalContext) {
            super(beanManager);
            this.beanId = producerField.getId();
            this.creationalContext = creationalContext;
        }

        public T call() throws Exception {
            if (instance == null) {
                Contextual<T> contextual = Container.instance(getBeanManager().getContextId()).services().get(ContextualStore.class).<Contextual<T>, T>getContextual(beanId);
                if (contextual instanceof EEResourceProducerField<?, ?>) {
                    this.instance = Reflections.<EEResourceProducerField<?, T>>cast(contextual).createUnderlying(creationalContext);
                } else {
                    throw new IllegalStateException(BEAN_NOT_EE_RESOURCE_PRODUCER, contextual);
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
     * @param field         The underlying method abstraction
     * @param declaringBean The declaring bean abstraction
     * @param manager       the current manager
     * @return A producer field
     */
    public static <X, T> EEResourceProducerField<X, T> of(WeldField<T, ? super X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager, ServiceRegistry services) {
        return new EEResourceProducerField<X, T>(field, declaringBean, manager, services);
    }

    private final WeldInjectionPoint<?, ?> injectionPoint;

    private ProxyFactory<T> proxyFactory;

    protected EEResourceProducerField(WeldField<T, ? super X> field, AbstractClassBean<X> declaringBean, BeanManagerImpl manager, ServiceRegistry services) {
        super(field, declaringBean, manager, services);
        this.injectionPoint = FieldInjectionPoint.of(manager.getContextId(), declaringBean, null, field);
    }

    @Override
    public void initialize(BeanDeployerEnvironment environment) {
        if (!isInitialized()) {
            super.initialize(environment);
            checkEEResource();
            proxyFactory = new ProxyFactory<T>(beanManager.getContextId(), getType(), getTypes(), this);
        }
    }

    protected void checkEEResource() {
        if (!getScope().equals(Dependent.class)) {
            throw new DefinitionException(NON_DEPENDENT_RESOURCE_PRODUCER_FIELD, this);
        }
        if (getName() != null) {
            throw new DefinitionException(NAMED_RESOURCE_PRODUCER_FIELD, this);
        }
        EJBApiAbstraction ejbApiAbstraction = beanManager.getServices().get(EJBApiAbstraction.class);
        PersistenceApiAbstraction persistenceApiAbstraction = beanManager.getServices().get(PersistenceApiAbstraction.class);
        WSApiAbstraction wsApiAbstraction = beanManager.getServices().get(WSApiAbstraction.class);
        if (!(getWeldAnnotated().isAnnotationPresent(ejbApiAbstraction.RESOURCE_ANNOTATION_CLASS)
                || getWeldAnnotated().isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS)
                || getWeldAnnotated().isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS)
                || getWeldAnnotated().isAnnotationPresent(ejbApiAbstraction.EJB_ANNOTATION_CLASS)
                || getWeldAnnotated().isAnnotationPresent(wsApiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS))) {
            throw new IllegalStateException(INVALID_RESOURCE_PRODUCER_FIELD, getWeldAnnotated());
        }
    }


    @Override
    public T create(CreationalContext<T> creationalContext) {
        if (Reflections.isFinal(getWeldAnnotated().getJavaClass()) || Serializable.class.isAssignableFrom(getWeldAnnotated().getJavaClass())) {
            return createUnderlying(creationalContext);
        } else {
            BeanInstance proxyBeanInstance = new EnterpriseTargetBeanInstance(getTypes(), new CallableMethodHandler(new EEResourceCallable<T>(getBeanManager(), this, creationalContext)));
            return new ProxyFactory<T>(beanManager.getContextId(), getType(), getTypes(), this).create(proxyBeanInstance);
        }
    }

    /**
     * Access to the underlying producer field
     */
    private T createUnderlying(CreationalContext<T> creationalContext) {
        // Treat static fields as a special case, as they won't be injected, as the no bean is resolved, and normally there is no injection on static fields
        if (getWeldAnnotated().isStatic()) {
            return Reflections.<T>cast(Beans.resolveEEResource(getBeanManager(), injectionPoint));
        } else {
            return super.create(creationalContext);
        }
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
