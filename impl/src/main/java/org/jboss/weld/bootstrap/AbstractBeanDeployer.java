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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.FOUND_DECORATOR;
import static org.jboss.weld.logging.messages.BootstrapMessage.FOUND_INTERCEPTOR;
import static org.jboss.weld.logging.messages.BootstrapMessage.FOUND_OBSERVER_METHOD;

import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedField;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.attributes.BeanAttributesFactory;
import org.jboss.weld.bean.attributes.ExternalBeanAttributesFactory;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ee.EEResourceProducerField;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.events.ProcessBeanAttributesImpl;
import org.jboss.weld.bootstrap.events.ProcessBeanImpl;
import org.jboss.weld.bootstrap.events.ProcessBeanInjectionTarget;
import org.jboss.weld.bootstrap.events.ProcessManagedBeanImpl;
import org.jboss.weld.bootstrap.events.ProcessObserverMethodImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerFieldImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerMethodImpl;
import org.jboss.weld.bootstrap.events.ProcessSessionBeanImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.event.ObserverFactory;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;
import org.slf4j.cal10n.LocLogger;

/**
 * @author Pete Muir
 * @author Ales Justin
 * @author Jozef Hartinger
 */
public class AbstractBeanDeployer<E extends BeanDeployerEnvironment> {

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    private final BeanManagerImpl manager;
    private final ServiceRegistry services;
    private final E environment;

    public AbstractBeanDeployer(BeanManagerImpl manager, ServiceRegistry services, E environment) {
        this.manager = manager;
        this.services = services;
        this.environment = environment;
    }

    protected BeanManagerImpl getManager() {
        return manager;
    }

    /**
     * In multi-threaded environment we often cannot leverage multiple core fully in bootstrap because the deployer
     * threads are often blocked by the reflection API or waiting to get a classloader lock. While waiting for classes to be loaded or
     * reflection metadata to be obtained, we can make use of the idle CPU cores and start resolving container lifecycle event observers
     * (extensions) upfront for those types of events we know we will be firing. Since these resolutions are cached, firing of the
     * lifecycle events will then be very fast.
     *
     * This method is a noop hook by default. It is expected to be override by a concurrent implementation of bean deployer.
     *
     */
    protected void preloadContainerLifecycleEvent(Class<?> eventRawType, Type... typeParameters) {
        // noop by default
    }

    // interceptors, decorators and observers go first
    public AbstractBeanDeployer<E> deploySpecialized() {
        // ensure that all decorators are initialized before initializing
        // the rest of the beans
        for (DecoratorImpl<?> bean : getEnvironment().getDecorators()) {
            bean.initialize(getEnvironment());
            ProcessBeanImpl.fire(getManager(), bean);
            manager.addDecorator(bean);
            log.debug(FOUND_DECORATOR, bean);
        }
        for (InterceptorImpl<?> bean : getEnvironment().getInterceptors()) {
            bean.initialize(getEnvironment());
            ProcessBeanImpl.fire(getManager(), bean);
            manager.addInterceptor(bean);
            log.debug(FOUND_INTERCEPTOR, bean);
        }
        return this;
    }

    public AbstractBeanDeployer<E> initializeBeans() {
        for (RIBean<?> bean : getEnvironment().getBeans()) {
            bean.initialize(getEnvironment());
        }
        return this;
    }

    public AbstractBeanDeployer<E> fireBeanEvents() {
        for (RIBean<?> bean : getEnvironment().getBeans()) {
            fireBeanEvents(bean);
        }
        return this;
    }

    public void fireBeanEvents(RIBean<?> bean) {
        if (!(bean instanceof NewBean)) {
            if (bean instanceof AbstractProducerBean<?, ?, ?>) {
                ProcessProducerImpl.fire(manager, Reflections.<AbstractProducerBean<?, ?, Member>>cast(bean));
            } else if (bean instanceof AbstractClassBean<?>) {
                ProcessBeanInjectionTarget.fire(manager, (AbstractClassBean<?>) bean);
            }
            if (bean instanceof ManagedBean<?>) {
                ProcessManagedBeanImpl.fire(manager, (ManagedBean<?>) bean);
            } else if (bean instanceof SessionBean<?>) {
                ProcessSessionBeanImpl.fire(manager, Reflections.<SessionBean<Object>>cast(bean));
            } else if (bean instanceof ProducerField<?, ?>) {
                ProcessProducerFieldImpl.fire(manager, (ProducerField<?, ?>) bean);
            } else if (bean instanceof ProducerMethod<?, ?>) {
                ProcessProducerMethodImpl.fire(manager, (ProducerMethod<?, ?>) bean);
            } else {
                ProcessBeanImpl.fire(getManager(), bean);
            }
        }
    }

    public AbstractBeanDeployer<E> deployBeans() {
        manager.addBeans(getEnvironment().getBeans());
        return this;
    }

    public AbstractBeanDeployer<E> initializeObserverMethods() {
        for (ObserverInitializationContext<?, ?> observerInitializer : getEnvironment().getObservers()) {
            if (Observers.isObserverMethodEnabled(observerInitializer.getObserver(), manager)) {
                observerInitializer.initialize();
            }
        }
        return this;
    }

    public AbstractBeanDeployer<E> deployObserverMethods() {
        // TODO -- why do observers have to be the last?
        for (ObserverInitializationContext<?, ?> observerInitializer : getEnvironment().getObservers()) {
            if (Observers.isObserverMethodEnabled(observerInitializer.getObserver(), manager)) {
                log.debug(FOUND_OBSERVER_METHOD, observerInitializer.getObserver());
                ProcessObserverMethodImpl.fire(manager, observerInitializer.getObserver());
                manager.addObserver(observerInitializer.getObserver());
            }
        }
        return this;
    }

    /**
     * Creates the sub bean for an class (simple or enterprise) bean
     *
     * @param bean The class bean
     */
    protected <T> void createObserversProducersDisposers(AbstractClassBean<T> bean) {
        if (bean instanceof ManagedBean<?> || bean instanceof SessionBean<?>) {
            createProducerMethods(bean, bean.getEnhancedAnnotated());
            createProducerFields(bean, bean.getEnhancedAnnotated());
            if (manager.isBeanEnabled(bean)) {
                createObserverMethods(bean, bean.getEnhancedAnnotated());
            }
            createDisposalMethods(bean, bean.getEnhancedAnnotated());
        }
    }

    protected <X> void createProducerMethods(AbstractClassBean<X> declaringBean, EnhancedAnnotatedType<X> annotatedClass) {
        for (EnhancedAnnotatedMethod<?, ? super X> method : annotatedClass.getDeclaredEnhancedMethods(Produces.class)) {
            // create method for now
            // specialization and PBA processing is handled later
            createProducerMethod(declaringBean, method);
        }
    }

    protected <X> void createDisposalMethods(AbstractClassBean<X> declaringBean, EnhancedAnnotatedType<X> annotatedClass) {
        for (EnhancedAnnotatedMethod<?, ? super X> method : annotatedClass.getDeclaredEnhancedMethodsWithAnnotatedParameters(Disposes.class)) {
            DisposalMethod<? super X, ?> disposalBean = DisposalMethod.of(manager, method, declaringBean);
            disposalBean.initialize(getEnvironment());
            getEnvironment().addDisposesMethod(disposalBean);
        }
    }

    protected <X, T> void createProducerMethod(AbstractClassBean<X> declaringBean, EnhancedAnnotatedMethod<T, ? super X> annotatedMethod) {
        BeanAttributes<T> attributes = BeanAttributesFactory.forBean(annotatedMethod, getManager());
        ProducerMethod<? super X, T> bean = ProducerMethod.of(attributes, annotatedMethod, declaringBean, manager, services);
        preloadContainerLifecycleEvent(ProcessBeanAttributes.class, bean.getType());
        preloadContainerLifecycleEvent(ProcessProducerMethod.class, annotatedMethod.getBaseType(), bean.getBeanClass());
        preloadContainerLifecycleEvent(ProcessProducer.class, bean.getBeanClass(), annotatedMethod.getBaseType());
        getEnvironment().addProducerMethod(bean);
    }

    protected <X, T> void createProducerField(AbstractClassBean<X> declaringBean, EnhancedAnnotatedField<T, ? super X> field) {
        BeanAttributes<T> attributes = BeanAttributesFactory.forBean(field, getManager());
        ProducerField<X, T> bean;
        if (isEEResourceProducerField(field)) {
            bean = EEResourceProducerField.of(attributes, field, declaringBean, manager, services);
        } else {
            bean = ProducerField.of(attributes, field, declaringBean, manager, services);
        }
        preloadContainerLifecycleEvent(ProcessBeanAttributes.class, bean.getType());
        preloadContainerLifecycleEvent(ProcessProducerField.class, field.getBaseType(), bean.getBeanClass());
        preloadContainerLifecycleEvent(ProcessProducer.class, bean.getBeanClass(), field.getBaseType());
        getEnvironment().addProducerField(bean);
    }

    protected <X> void createProducerFields(AbstractClassBean<X> declaringBean, EnhancedAnnotatedType<X> annotatedClass) {
        for (EnhancedAnnotatedField<?, ? super X> field : annotatedClass.getDeclaredEnhancedFields(Produces.class)) {
            createProducerField(declaringBean, field);
        }
    }

    protected <X> void createObserverMethods(RIBean<X> declaringBean, EnhancedAnnotatedType<? super X> annotatedClass) {
        for (EnhancedAnnotatedMethod<?, ? super X> method : Beans.getObserverMethods(annotatedClass)) {
            createObserverMethod(declaringBean, method);
        }
    }

    protected <T, X> void createObserverMethod(RIBean<X> declaringBean, EnhancedAnnotatedMethod<T, ? super X> method) {
        ObserverMethodImpl<T, X> observer = ObserverFactory.create(method, declaringBean, manager);
        ObserverInitializationContext<T, ? super X> observerInitializer = ObserverInitializationContext.of(observer, method);
        preloadContainerLifecycleEvent(ProcessObserverMethod.class, observer.getObservedType(), declaringBean.getBeanClass());
        getEnvironment().addObserverMethod(observerInitializer);
    }

    protected <T> ManagedBean<T> createManagedBean(EnhancedAnnotatedType<T> weldClass) {
        BeanAttributes<T> attributes = BeanAttributesFactory.forBean(weldClass, getManager());
        ManagedBean<T> bean = ManagedBean.of(attributes, weldClass, manager, services);
        getEnvironment().addManagedBean(bean);
        return bean;
    }

    protected <T> void createNewManagedBean(EnhancedAnnotatedType<T> annotatedClass) {
        // TODO resolve existing beans first
        getEnvironment().addManagedBean(NewManagedBean.of(BeanAttributesFactory.forNewManagedBean(annotatedClass, manager), annotatedClass, manager, services));
    }

    protected <T> void createDecorator(EnhancedAnnotatedType<T> weldClass) {
        BeanAttributes<T> attributes = BeanAttributesFactory.forBean(weldClass, getManager());
        DecoratorImpl<T> bean = DecoratorImpl.of(attributes, weldClass, manager, services);
        getEnvironment().addDecorator(bean);
    }

    protected <T> void createInterceptor(EnhancedAnnotatedType<T> weldClass) {
        BeanAttributes<T> attributes = BeanAttributesFactory.forBean(weldClass, getManager());
        InterceptorImpl<T> bean = InterceptorImpl.of(attributes, weldClass, manager, services);
        getEnvironment().addInterceptor(bean);
    }

    protected <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<T> descriptor) {
        return createSessionBean(descriptor, services.get(ClassTransformer.class).getEnhancedAnnotatedType(descriptor.getBeanClass()));
    }
    protected <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<T> descriptor, EnhancedAnnotatedType<T> weldClass) {
        // TODO Don't create enterprise bean if it has no local interfaces!
        BeanAttributes<T> attributes = BeanAttributesFactory.forSessionBean(weldClass, descriptor, getManager());
        SessionBean<T> bean = SessionBean.of(attributes, descriptor, manager, weldClass, services);
        getEnvironment().addSessionBean(bean);
        return bean;
    }

    protected <T> void createNewSessionBean(InternalEjbDescriptor<T> ejbDescriptor, BeanAttributes<?> originalAttributes) {
        BeanAttributes<T> attributes = Reflections.cast(BeanAttributesFactory.forNewSessionBean(originalAttributes));
        getEnvironment().addSessionBean(NewSessionBean.of(attributes, ejbDescriptor, manager, services));
    }

    protected boolean isEEResourceProducerField(EnhancedAnnotatedField<?, ?> field) {
        EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
        PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
        WSApiAbstraction wsApiAbstraction = manager.getServices().get(WSApiAbstraction.class);
        return field.isAnnotationPresent(ejbApiAbstraction.EJB_ANNOTATION_CLASS) || field.isAnnotationPresent(ejbApiAbstraction.RESOURCE_ANNOTATION_CLASS) || field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS) || field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS) || field.isAnnotationPresent(wsApiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS);
    }

    public E getEnvironment() {
        return environment;
    }

    public void addBuiltInBean(AbstractBuiltInBean<?> bean) {
        preloadContainerLifecycleEvent(ProcessBean.class, bean.getBeanClass());
        getEnvironment().addBuiltInBean(bean);
    }

    protected <T, S> boolean fireProcessBeanAttributes(AbstractBean<T, S> bean) {
        if (!getManager().isBeanEnabled(bean) || Beans.isSpecialized(bean, getManager()) || Beans.isSuppressedBySpecialization(bean, getManager())) {
            return false;
        }

        ProcessBeanAttributesImpl<T> event = ProcessBeanAttributesImpl.fire(getManager(), bean, bean.getAnnotated(), bean.getType());
        if (event.isVeto()) {
            return true;
        }
        if (event.isDirty()) {
            bean.setAttributes(ExternalBeanAttributesFactory.<T>of(event.getBeanAttributes(), manager));
            bean.checkSpecialization();
        }
        return false;
    }
}
