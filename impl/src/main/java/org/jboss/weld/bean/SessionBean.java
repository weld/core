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

import static org.jboss.weld.logging.messages.BeanMessage.CANNOT_DESTROY_ENTERPRISE_BEAN_NOT_CREATED;
import static org.jboss.weld.logging.messages.BeanMessage.CANNOT_DESTROY_NULL_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.EJB_CANNOT_BE_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.EJB_CANNOT_BE_INTERCEPTOR;
import static org.jboss.weld.logging.messages.BeanMessage.GENERIC_SESSION_BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.MESSAGE_DRIVEN_BEANS_CANNOT_BE_MANAGED;
import static org.jboss.weld.logging.messages.BeanMessage.OBSERVER_METHOD_MUST_BE_STATIC_OR_BUSINESS;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL;
import static org.jboss.weld.logging.messages.BeanMessage.SCOPE_NOT_ALLOWED_ON_SINGLETON_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.SCOPE_NOT_ALLOWED_ON_STATELESS_SESSION_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.interceptor.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bean.proxy.EnterpriseBeanInstance;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.SessionBeanInjectionPoint;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.ejb.SessionBeanProxyInstantiator;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.reflection.Formats;

/**
 * An enterprise bean representation
 *
 * @param <T> The type (class) of the bean
 * @author Pete Muir
 * @author Ales Justin
 */

public class SessionBean<T> extends AbstractClassBean<T> {
    // The EJB descriptor
    private final InternalEjbDescriptor<T> ejbDescriptor;

    private Instantiator<T> proxyInstantiator;

    /**
     * Creates a simple, annotation defined Enterprise Web Bean using the annotations specified on type
     *
     * @param <T>         The type
     * @param beanManager the current manager
     * @param type        the AnnotatedType to use
     * @return An Enterprise Web Bean
     */
    public static <T> SessionBean<T> of(BeanAttributes<T> attributes, InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl beanManager, EnhancedAnnotatedType<T> type) {
        return new SessionBean<T>(attributes, type, ejbDescriptor, new StringBeanIdentifier(BeanIdentifiers.forSessionBean(type, ejbDescriptor)), beanManager);
    }

    /**
     * Constructor
     *
     * @param type    The type of the bean
     * @param manager The Bean manager
     */
    protected SessionBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> ejbDescriptor, BeanIdentifier identifier, BeanManagerImpl manager) {
        super(attributes, type, identifier, manager);
        this.ejbDescriptor = ejbDescriptor;
        setProducer(beanManager.getLocalInjectionTargetFactory(getEnhancedAnnotated()).createInjectionTarget(getEnhancedAnnotated(), this, false));
    }

    /**
     * Initializes the bean and its metadata
     */
    @Override
    public void internalInitialize(BeanDeployerEnvironment environment) {
        super.internalInitialize(environment);
        checkEJBTypeAllowed();
        checkConflictingRoles();
        checkObserverMethods();
        checkScopeAllowed();
    }

    /**
     * Validates for non-conflicting roles
     */
    protected void checkConflictingRoles() {
        if (getType().isAnnotationPresent(Interceptor.class)) {
            throw new DefinitionException(EJB_CANNOT_BE_INTERCEPTOR, getType());
        }
        if (getType().isAnnotationPresent(Decorator.class)) {
            throw new DefinitionException(EJB_CANNOT_BE_DECORATOR, getType());
        }
    }

    /**
     * Check that the scope type is allowed by the stereotypes on the bean and
     * the bean type
     */
    protected void checkScopeAllowed() {
        if (ejbDescriptor.isStateless() && !isDependent()) {
            throw new DefinitionException(SCOPE_NOT_ALLOWED_ON_STATELESS_SESSION_BEAN, getScope(), getType());
        }
        if (ejbDescriptor.isSingleton() && !(isDependent() || getScope().equals(ApplicationScoped.class))) {
            throw new DefinitionException(SCOPE_NOT_ALLOWED_ON_SINGLETON_BEAN, getScope(), getType());
        }
    }

    @Override
    protected void specialize() {
        Set<? extends AbstractBean<?, ?>> specializedBeans = getSpecializedBeans();
        if (specializedBeans.isEmpty()) {
            throw new DefinitionException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
        }
        for (AbstractBean<?, ?> specializedBean : specializedBeans) {
            if (!(specializedBean instanceof SessionBean<?>)) {
                throw new DefinitionException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
            }
        }
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    public T create(final CreationalContext<T> creationalContext) {
        return proxyInstantiator.newInstance(creationalContext, beanManager);
    }

    public void destroy(T instance, CreationalContext<T> creationalContext) {
        if (instance == null) {
            throw new IllegalArgumentException(CANNOT_DESTROY_NULL_BEAN, this);
        }
        if (!(instance instanceof EnterpriseBeanInstance)) {
            throw new IllegalArgumentException(CANNOT_DESTROY_ENTERPRISE_BEAN_NOT_CREATED, instance);
        }
        EnterpriseBeanInstance enterpriseBeanInstance = (EnterpriseBeanInstance) instance;
        enterpriseBeanInstance.destroy(Marker.INSTANCE, this, creationalContext);
        creationalContext.release();
    }

    /**
     * Validates the bean type
     */
    private void checkEJBTypeAllowed() {
        if (ejbDescriptor.isMessageDriven()) {
            throw new DefinitionException(MESSAGE_DRIVEN_BEANS_CANNOT_BE_MANAGED, this);
        }
    }

    @Override
    protected void checkType() {
        if (!isDependent() && getEnhancedAnnotated().isGeneric()) {
            throw new DefinitionException(GENERIC_SESSION_BEAN_MUST_BE_DEPENDENT, this);
        }
        boolean passivating = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isPassivating();
        if (passivating && !isPassivationCapableBean()) {
            throw new DeploymentException(PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL, this);
        }
    }

    public InternalEjbDescriptor<T> getEjbDescriptor() {
        return ejbDescriptor;
    }

    public boolean isClientCanCallRemoveMethods() {
        return getEjbDescriptor().isStateful() && isDependent();
    }

    /**
     * If there are any observer methods, they must be static or business
     * methods.
     */
    protected void checkObserverMethods() {
        Collection<EnhancedAnnotatedMethod<?, ? super T>> observerMethods = BeanMethods.getObserverMethods(this.getEnhancedAnnotated());

        if (!observerMethods.isEmpty()) {
            Set<MethodSignature> businessMethodSignatures = getBusinessMethodSignatures();
            for (EnhancedAnnotatedMethod<?, ? super T> observerMethod : observerMethods) {
                if (!observerMethod.isStatic() && !businessMethodSignatures.contains(observerMethod.getSignature())) {
                    throw new DefinitionException(OBSERVER_METHOD_MUST_BE_STATIC_OR_BUSINESS, observerMethod, getEnhancedAnnotated());
                }
            }
        }
    }

    protected Set<MethodSignature> getBusinessMethodSignatures() {
        Set<MethodSignature> businessMethodSignatures = new HashSet<MethodSignature>();
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces()) {
            for (Method m : businessInterfaceDescriptor.getInterface().getMethods()) {
                businessMethodSignatures.add(new MethodSignatureImpl(m));
            }
        }
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getRemoteBusinessInterfaces()) {
            for (Method m : businessInterfaceDescriptor.getInterface().getMethods()) {
                businessMethodSignatures.add(new MethodSignatureImpl(m));
            }
        }
        return Collections.unmodifiableSet(businessMethodSignatures);
    }

    public SessionObjectReference createReference() {
        try {
            SessionBeanInjectionPoint.registerContextualInstance(getEjbDescriptor());
            return beanManager.getServices().get(EjbServices.class).resolveEjb(getEjbDescriptor().delegate());
        } finally {
            SessionBeanInjectionPoint.unregisterContextualInstance(getEjbDescriptor());
        }
    }

    @Override
    protected boolean isInterceptionCandidate() {
        return true;
    }

    @Override
    public String toString() {
        return "Session bean [" + getBeanClass() + " with qualifiers [" + Formats.formatAnnotations(getQualifiers()) + "]; local interfaces are [" + Formats.formatBusinessInterfaceDescriptors(getEjbDescriptor().getLocalBusinessInterfaces()) + "]";
    }

    // ejb's are always proxiable
    @Override
    public boolean isProxyable() {
        return true;
    }

    @Override
    public boolean isPassivationCapableBean() {
        return ejbDescriptor.isPassivationCapable();
    }

    @Override
    public boolean isPassivationCapableDependency() {
        return (ejbDescriptor.isStateful() && isPassivationCapableBean()) || ejbDescriptor.isSingleton() || ejbDescriptor.isStateless();
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        super.initializeAfterBeanDiscovery();
        this.proxyInstantiator = new SessionBeanProxyInstantiator<T>(enhancedAnnotatedItem, this);
        registerInterceptors();
    }

    protected void registerInterceptors() {
        InterceptionModel<ClassMetadata<?>> model = beanManager.getInterceptorModelRegistry().get(getEjbDescriptor().getBeanClass());
        if (model != null) {
            getBeanManager().getServices().get(EjbServices.class).registerInterceptors(getEjbDescriptor(), new InterceptorBindingsAdapter(model));
        }
    }
}

