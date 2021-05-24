/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import jakarta.ejb.Timeout;
import jakarta.enterprise.inject.spi.BeanAttributes;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.producer.AbstractInstantiator;
import org.jboss.weld.injection.producer.BasicInjectionTarget;
import org.jboss.weld.injection.producer.ConstructorInterceptionInstantiator;
import org.jboss.weld.injection.producer.DefaultInstantiator;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.injection.producer.InterceptionModelInitializer;
import org.jboss.weld.injection.producer.InterceptorApplyingInstantiator;
import org.jboss.weld.injection.producer.SubclassedComponentInstantiator;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.module.EjbSupport;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.SetMultimap;
import org.jboss.weld.util.reflection.Reflections;

class EjbSupportImpl implements EjbSupport {

    private final EjbServices ejbServices;
    private final EjbDescriptors ejbDescriptors;

    EjbSupportImpl(EjbServices ejbServices, Collection<EjbDescriptor<?>> descriptors) {
        this.ejbServices = ejbServices;
        this.ejbDescriptors = new EjbDescriptors(descriptors);
    }

    @Override
    public void cleanup() {
    }

    @Override
    public <T> BasicInjectionTarget<T> createSessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl beanManager) {
        return SessionBeanInjectionTarget.of(type, bean, beanManager);
    }

    @Override
    public <T> BasicInjectionTarget<T> createMessageDrivenInjectionTarget(EnhancedAnnotatedType<T> type, EjbDescriptor<T> d, BeanManagerImpl manager) {
        InternalEjbDescriptor<T> descriptor = InternalEjbDescriptor.of(d);
        EnhancedAnnotatedType<T> implementationClass = SessionBeans.getEjbImplementationClass(descriptor, manager, type);

        Instantiator<T> instantiator = null;
        if (type.equals(implementationClass)) {
            instantiator = new DefaultInstantiator<T>(type, null, manager);
        } else {
            // Session bean subclassed by the EJB container
            instantiator = SubclassedComponentInstantiator.forSubclassedEjb(type, implementationClass, null, manager);
        }
        InterceptionModel interceptionModel = manager.getInterceptorModelRegistry().get(type.slim());
        if (interceptionModel != null) {
            if (interceptionModel.hasExternalNonConstructorInterceptors()) {
                instantiator = SubclassedComponentInstantiator
                        .forInterceptedDecoratedBean(implementationClass, null, (AbstractInstantiator<T>) instantiator, manager);
                instantiator = new InterceptorApplyingInstantiator<>(instantiator, interceptionModel, type.slim());

            }
            if (interceptionModel.hasExternalConstructorInterceptors()) {
                instantiator = new ConstructorInterceptionInstantiator<>(instantiator, interceptionModel, type.slim());
            }
        }
        return BasicInjectionTarget.createDefault(type, null, manager, instantiator);
    }

    @Override
    public <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> annotated, BeanManagerImpl manager) {
        final InternalEjbDescriptor<?> descriptor = ejbDescriptors.getUnique(annotated.getJavaClass());
        Preconditions.checkArgument(descriptor != null, annotated.getJavaClass() + " is not an EJB.");
        return createSessionBeanAttributes(annotated, descriptor, manager);
    }

    private <T> BeanAttributes<T> createSessionBeanAttributes(EnhancedAnnotatedType<T> annotated, InternalEjbDescriptor<?> descriptor, BeanManagerImpl manager) {
        return SessionBeans.createBeanAttributes(annotated, descriptor, manager);
    }

    @Override
    public void createSessionBeans(BeanDeployerEnvironment environment, SetMultimap<Class<?>, SlimAnnotatedType<?>> types, BeanManagerImpl manager) {
        final ClassTransformer transformer = manager.getServices().get(ClassTransformer.class);

        for (InternalEjbDescriptor<?> ejbDescriptor : getEjbDescriptors()) {
            if (environment.isVetoed(ejbDescriptor.getBeanClass()) || Beans.isVetoed(ejbDescriptor.getBeanClass())) {
                continue;
            }
            if (ejbDescriptor.isSingleton() || ejbDescriptor.isStateful() || ejbDescriptor.isStateless()) {
                Set<SlimAnnotatedType<?>> classes = types.get(ejbDescriptor.getBeanClass());
                if (!classes.isEmpty()) {
                    for (SlimAnnotatedType<?> annotatedType : classes) {
                        createSessionBean(ejbDescriptor, annotatedType, environment, manager, transformer);
                    }
                } else {
                    createSessionBean(ejbDescriptor, environment, manager, transformer);
                }
            }
        }
    }

    private <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<?> descriptor, SlimAnnotatedType<T> slimType, BeanDeployerEnvironment environment, BeanManagerImpl manager, ClassTransformer transformer) {
        final EnhancedAnnotatedType<T> type = transformer.getEnhancedAnnotatedType(slimType);
        final BeanAttributes<T> attributes = createSessionBeanAttributes(type, descriptor, manager);
        final SessionBean<T> bean = SessionBeanImpl.of(attributes, Reflections.<InternalEjbDescriptor<T>>cast(descriptor), manager, type);
        environment.addSessionBean(bean);
        return bean;
    }

    protected <T> SessionBean<T> createSessionBean(InternalEjbDescriptor<T> descriptor, BeanDeployerEnvironment environment, BeanManagerImpl manager, ClassTransformer transformer) {
        final SlimAnnotatedType<T> type = transformer.getBackedAnnotatedType(descriptor.getBeanClass(), manager.getId());
        manager.getServices().get(SlimAnnotatedTypeStore.class).put(type);
        return createSessionBean(descriptor, type, environment, manager, transformer);
    }

    @Override
    public Class<? extends Annotation> getTimeoutAnnotation() {
        return Timeout.class;
    }

    public void registerCdiInterceptorsForMessageDrivenBeans(BeanDeployerEnvironment environment, BeanManagerImpl manager) {
        for (InternalEjbDescriptor<?> descriptor : getEjbDescriptors()) {
            if (descriptor.isMessageDriven()) {
                EnhancedAnnotatedType<?> type =  manager.getServices().getRequired(ClassTransformer.class).getEnhancedAnnotatedType(descriptor.getBeanClass(), manager.getId());
                if (!manager.getInterceptorModelRegistry().containsKey(type.slim())) {
                    InterceptionModelInitializer.of(manager, type, null).init();
                }
                InterceptionModel model = manager.getInterceptorModelRegistry().get(type.slim());
                if (model != null) {
                    ejbServices.registerInterceptors(descriptor.delegate(), new InterceptorBindingsAdapter(model));
                }
            }
        }
    }

    @Override
    public Collection<InternalEjbDescriptor<?>> getEjbDescriptors() {
        return ejbDescriptors.getAll();
    }

    @Override
    public boolean isEjb(Class<?> beanClass) {
        return ejbDescriptors.contains(beanClass);
    }

    @Override
    public <T> InternalEjbDescriptor<T> getEjbDescriptor(String beanName) {
        return ejbDescriptors.get(beanName);
    }

    @Override
    public boolean isSessionBeanProxy(Object instance) {
        return instance instanceof EnterpriseBeanInstance;
    }


}
