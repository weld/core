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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.interceptor.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.EnterpriseBeanInstance;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.BeansClosure;
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

    private SessionBean<?> specializedBean;

    /**
     * Creates a simple, annotation defined Enterprise Web Bean using the annotations specified on type
     *
     * @param <T>         The type
     * @param beanManager the current manager
     * @param type        the AnnotatedType to use
     * @return An Enterprise Web Bean
     */
    public static <T> SessionBean<T> of(BeanAttributes<T> attributes, InternalEjbDescriptor<T> ejbDescriptor, BeanManagerImpl beanManager, EnhancedAnnotatedType<T> type) {
        return new SessionBean<T>(attributes, type, ejbDescriptor, createId(SessionBean.class.getSimpleName(), ejbDescriptor, type), beanManager);
    }

    protected static String createId(String beanType, InternalEjbDescriptor<?> ejbDescriptor) {
        return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).toString();
    }

    protected static String createId(String beanType, InternalEjbDescriptor<?> ejbDescriptor, EnhancedAnnotatedType<?> type) {
        if (type.isDiscovered()) {
            return createId(beanType, ejbDescriptor);
        } else {
            return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(ejbDescriptor.getEjbName()).append(AnnotatedTypes.createTypeId(type)).toString();
        }
    }

    /**
     * Constructor
     *
     * @param type    The type of the bean
     * @param manager The Bean manager
     */
    protected SessionBean(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> ejbDescriptor, String idSuffix, BeanManagerImpl manager) {
        super(attributes, type, idSuffix, manager);
        this.ejbDescriptor = ejbDescriptor;
        setProducer(beanManager.createInjectionTarget(getEnhancedAnnotated(), this));
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

    /**
     * Validates specialization
     */
    @Override
    protected void preSpecialize() {
        super.preSpecialize();
        // We appear to check this twice?
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        if (closure.isEJB(getEnhancedAnnotated().getEnhancedSuperclass()) == false) {
            throw new DefinitionException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
        }
    }

    @Override
    protected void specialize() {
        BeansClosure closure = BeansClosure.getClosure(beanManager);
        Bean<?> specializedBean = closure.getClassBean(getEnhancedAnnotated().getEnhancedSuperclass());
        if (specializedBean == null) {
            throw new IllegalStateException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
        }
        if (!(specializedBean instanceof SessionBean<?>)) {
            throw new IllegalStateException(SPECIALIZING_ENTERPRISE_BEAN_MUST_EXTEND_AN_ENTERPRISE_BEAN, this);
        } else {
            this.specializedBean = (SessionBean<?>) specializedBean;
        }
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    public T create(final CreationalContext<T> creationalContext) {
        return getProducer().produce(creationalContext);
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

    @Override
    public AbstractBean<?, ?> getSpecializedBean() {
        return specializedBean;
    }

    /**
     * If there are any observer methods, they must be static or business
     * methods.
     */
    protected void checkObserverMethods() {
        List<EnhancedAnnotatedMethod<?, ? super T>> observerMethods = Beans.getObserverMethods(this.getEnhancedAnnotated());

        if (!observerMethods.isEmpty()) {
            Set<MethodSignature> businessMethodSignatures = new HashSet<MethodSignature>();
            for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces()) {
                for (Method m : businessInterfaceDescriptor.getInterface().getDeclaredMethods()) {
                    businessMethodSignatures.add(new MethodSignatureImpl(m));
                }
            }
            for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getRemoteBusinessInterfaces()) {
                for (Method m : businessInterfaceDescriptor.getInterface().getDeclaredMethods()) {
                    businessMethodSignatures.add(new MethodSignatureImpl(m));
                }
            }

            for (EnhancedAnnotatedMethod<?, ? super T> observerMethod : observerMethods) {
                if (!observerMethod.isStatic() && !businessMethodSignatures.contains(new MethodSignatureImpl(observerMethod))) {
                    throw new DefinitionException(OBSERVER_METHOD_MUST_BE_STATIC_OR_BUSINESS, observerMethod, getEnhancedAnnotated());
                }
            }
        }
    }

    public SessionObjectReference createReference() {
        return beanManager.getServices().get(EjbServices.class).resolveEjb(getEjbDescriptor().delegate());
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
}

