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
package org.jboss.weld.module.ejb;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.decorator.Decorator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.interceptor.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.StringBeanIdentifier;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bean.proxy.Marker;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BeanLogger;
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

class SessionBeanImpl<T> extends AbstractClassBean<T> implements SessionBean<T> {
    // The EJB descriptor
    private final InternalEjbDescriptor<T> ejbDescriptor;

    private Instantiator<T> proxyInstantiator;

    /**
     * Creates a simple, annotation defined Enterprise Web Bean using the annotations specified on type
     *
     * @param <T> The type
     * @param beanManager the current manager
     * @param type the AnnotatedType to use
     * @return An Enterprise Web Bean
     */
    public static <T> SessionBean<T> of(BeanAttributes<T> attributes, InternalEjbDescriptor<T> ejbDescriptor,
            BeanManagerImpl beanManager, EnhancedAnnotatedType<T> type) {
        return new SessionBeanImpl<T>(attributes, type, ejbDescriptor,
                new StringBeanIdentifier(SessionBeans.createIdentifier(type, ejbDescriptor)), beanManager);
    }

    /**
     * Constructor
     *
     * @param type The type of the bean
     * @param manager The Bean manager
     */
    SessionBeanImpl(BeanAttributes<T> attributes, EnhancedAnnotatedType<T> type, InternalEjbDescriptor<T> ejbDescriptor,
            BeanIdentifier identifier, BeanManagerImpl manager) {
        super(attributes, type, identifier, manager);
        this.ejbDescriptor = ejbDescriptor;
        setProducer(beanManager.getLocalInjectionTargetFactory(type).createInjectionTarget(type, this, false));
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
            throw BeanLogger.LOG.ejbCannotBeInterceptor(getType());
        }
        if (getType().isAnnotationPresent(Decorator.class)) {
            throw BeanLogger.LOG.ejbCannotBeDecorator(getType());
        }
    }

    /**
     * Check that the scope type is allowed by the stereotypes on the bean and
     * the bean type
     */
    protected void checkScopeAllowed() {
        if (ejbDescriptor.isStateless() && !isDependent()) {
            throw BeanLogger.LOG.scopeNotAllowedOnStatelessSessionBean(getScope(), getType());
        }
        if (ejbDescriptor.isSingleton() && !(isDependent() || getScope().equals(ApplicationScoped.class))) {
            throw BeanLogger.LOG.scopeNotAllowedOnSingletonBean(getScope(), getType());
        }
    }

    @Override
    protected void specialize() {
        Set<? extends AbstractBean<?, ?>> specializedBeans = getSpecializedBeans();
        if (specializedBeans.isEmpty()) {
            throw BeanLogger.LOG.specializingEnterpriseBeanMustExtendAnEnterpriseBean(this);
        }
        for (AbstractBean<?, ?> specializedBean : specializedBeans) {
            if (!(specializedBean instanceof SessionBean<?>)) {
                throw BeanLogger.LOG.specializingEnterpriseBeanMustExtendAnEnterpriseBean(this);
            }
        }
    }

    /**
     * Creates an instance of the bean
     *
     * @return The instance
     */
    @Override
    public T create(final CreationalContext<T> creationalContext) {
        if (proxyInstantiator == null) {
            // initializeAfterBeanDiscovery wasn't yet invoked
            // this can happen if you attempt to create bean prior to fully booting container
            throw BeanLogger.LOG.initABDnotInvoked(annotatedType);
        }
        return proxyInstantiator.newInstance(creationalContext, beanManager);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
        super.destroy(instance, creationalContext);
        if (instance == null) {
            throw BeanLogger.LOG.cannotDestroyNullBean(this);
        }
        if (!(instance instanceof EnterpriseBeanInstance)) {
            throw BeanLogger.LOG.cannotDestroyEnterpriseBeanNotCreated(instance);
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
            throw BeanLogger.LOG.messageDrivenBeansCannotBeManaged(this);
        }
    }

    @Override
    protected void checkType() {
        if (!isDependent() && getEnhancedAnnotated().isGeneric()) {
            throw BeanLogger.LOG.genericSessionBeanMustBeDependent(this);
        }
        boolean passivating = beanManager.getServices().get(MetaAnnotationStore.class).getScopeModel(getScope())
                .isPassivating();
        if (passivating && !isPassivationCapableBean()) {
            throw BeanLogger.LOG.passivatingBeanNeedsSerializableImpl(this);
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
        Collection<EnhancedAnnotatedMethod<?, ? super T>> observerMethods = BeanMethods
                .getObserverMethods(this.getEnhancedAnnotated());
        Collection<EnhancedAnnotatedMethod<?, ? super T>> asyncObserverMethods = BeanMethods
                .getAsyncObserverMethods(this.getEnhancedAnnotated());
        checkObserverMethods(observerMethods);
        checkObserverMethods(asyncObserverMethods);
    }

    private void checkObserverMethods(Collection<EnhancedAnnotatedMethod<?, ? super T>> observerMethods) {
        if (!observerMethods.isEmpty()) {
            Set<MethodSignature> businessMethodSignatures = getLocalBusinessMethodSignatures();
            Set<MethodSignature> remoteBusinessMethodSignatures = getRemoteBusinessMethodSignatures();
            for (EnhancedAnnotatedMethod<?, ? super T> observerMethod : observerMethods) {
                boolean isLocalBusinessMethod = !remoteBusinessMethodSignatures.contains(observerMethod.getSignature())
                        && businessMethodSignatures
                                .contains(observerMethod.getSignature());
                if (!isLocalBusinessMethod && !observerMethod.isStatic()) {
                    throw BeanLogger.LOG
                            .observerMethodMustBeStaticOrBusiness(observerMethod,
                                    Formats.formatAsStackTraceElement(observerMethod.getJavaMember()));
                }
            }
        }
    }

    public Set<MethodSignature> getLocalBusinessMethodSignatures() {
        Set<MethodSignature> businessMethodSignatures = new HashSet<MethodSignature>();
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getLocalBusinessInterfaces()) {
            for (Method m : businessInterfaceDescriptor.getInterface().getMethods()) {
                businessMethodSignatures.add(new MethodSignatureImpl(m));
            }
        }
        return Collections.unmodifiableSet(businessMethodSignatures);
    }

    public Set<MethodSignature> getRemoteBusinessMethodSignatures() {
        Set<MethodSignature> businessMethodSignatures = new HashSet<MethodSignature>();
        for (BusinessInterfaceDescriptor<?> businessInterfaceDescriptor : ejbDescriptor.getRemoteBusinessInterfaces()) {
            for (Method m : businessInterfaceDescriptor.getInterface().getMethods()) {
                businessMethodSignatures.add(new MethodSignatureImpl(m));
            }
        }
        return Collections.unmodifiableSet(businessMethodSignatures);
    }

    public SessionObjectReference createReference() {
        try {
            SessionBeanAwareInjectionPointBean.registerContextualInstance(getEjbDescriptor());
            return beanManager.getServices().get(EjbServices.class).resolveEjb(getEjbDescriptor().delegate());
        } finally {
            SessionBeanAwareInjectionPointBean.unregisterContextualInstance(getEjbDescriptor());
        }
    }

    @Override
    protected boolean isInterceptionCandidate() {
        return true;
    }

    @Override
    public String toString() {
        return "Session bean [" + getBeanClass() + " with qualifiers [" + Formats.formatAnnotations(getQualifiers())
                + "]; local interfaces are ["
                + Formats.formatBusinessInterfaceDescriptors(getEjbDescriptor().getLocalBusinessInterfaces()) + "]";
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
        return (ejbDescriptor.isStateful() && isPassivationCapableBean()) || ejbDescriptor.isSingleton()
                || ejbDescriptor.isStateless();
    }

    @Override
    public void initializeAfterBeanDiscovery() {
        super.initializeAfterBeanDiscovery();
        this.proxyInstantiator = new SessionBeanProxyInstantiator<T>(enhancedAnnotatedItem, this);
        registerInterceptors();
    }

    protected void registerInterceptors() {
        InterceptionModel model = beanManager.getInterceptorModelRegistry().get(getAnnotated());
        if (model != null) {
            getBeanManager().getServices().get(EjbServices.class).registerInterceptors(getEjbDescriptor().delegate(),
                    new InterceptorBindingsAdapter(model));
        }
    }

    @Override
    public Collection<AnnotatedMethod<? super T>> getInvokableMethods() {
        return invokableMethods;
    }
}
