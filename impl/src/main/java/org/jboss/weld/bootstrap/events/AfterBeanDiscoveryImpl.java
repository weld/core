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
package org.jboss.weld.bootstrap.events;

import static org.jboss.weld.util.Observers.validateObserverMethod;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator;

import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.WeldBean;
import org.jboss.weld.bean.attributes.ExternalBeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.BeanDeploymentFinder;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.event.InterceptorConfigurator;
import org.jboss.weld.bootstrap.event.WeldAfterBeanDiscovery;
import org.jboss.weld.bootstrap.event.WeldBeanConfigurator;
import org.jboss.weld.bootstrap.events.configurator.BeanConfiguratorImpl;
import org.jboss.weld.bootstrap.events.configurator.ObserverMethodConfiguratorImpl;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.logging.ContextLogger;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.logging.MetadataLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Bindings;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Preconditions;

public class AfterBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements WeldAfterBeanDiscovery {

    private static final String TYPE_ARGUMENT_NAME = "type";

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Collection<ContextHolder<? extends Context>> contexts) {
        final AfterBeanDiscoveryImpl event = new AfterBeanDiscoveryImpl(beanManager, deployment, bdaMapping, contexts);
        event.fire();
        event.finish();
    }

    private AfterBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, WeldAfterBeanDiscovery.class, bdaMapping, deployment, contexts);
        this.slimAnnotatedTypeStore = beanManager.getServices().get(SlimAnnotatedTypeStore.class);
        this.containerLifecycleEvents = beanManager.getServices().get(ContainerLifecycleEvents.class);
        this.additionalBeans = new LinkedList<>();
        this.additionalObservers = new LinkedList<>();
    }

    private final SlimAnnotatedTypeStore slimAnnotatedTypeStore;
    private final ContainerLifecycleEvents containerLifecycleEvents;
    private final List<BeanRegistration> additionalBeans;
    private final List<ObserverRegistration> additionalObservers;

    @Override
    public void addBean(Bean<?> bean) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(bean, "bean");
        ExternalBeanAttributesFactory.validateBeanAttributes(bean, getBeanManager());
        validateBean(bean);
        additionalBeans.add(BeanRegistration.of(bean, getReceiver()));
        BootstrapLogger.LOG.addBeanCalled(getReceiver(), bean);
    }

    @Override
    public <T> WeldBeanConfigurator<T> addBean() {
        // null is only going to occur if the invocation is outside of OM in which case it will fail in the
        // subsequent method inside checkWithinObserverNotification()
        return addBean(getReceiver() != null ? getReceiver().getClass() : null);
    }

    /**
     * Used by {@code LiteExtensionTranslator} to register beans coming from Build Compatible extensions.
     * This ensures that the bean is registered under given BCE class instead of being linked to
     * {@code LiteExtensionTranslator}.
     *
     * This method should not be used anywhere else.
     *
     * @param receiverClass class of the Build Compatible extension performing synth. bean registration
     * @param <T> bean type
     * @return instance of {@link WeldBeanConfigurator}
     */
    public <T> WeldBeanConfigurator<T> addBean(Class<?> receiverClass) {
        checkWithinObserverNotification();
        BeanConfiguratorImpl<T> configurator = new BeanConfiguratorImpl<>(receiverClass, getBeanDeploymentFinder());
        // note that here we deliberately keep getReceiver() since the logging is related to registering portable extension
        additionalBeans.add(BeanRegistration.of(configurator, getReceiver()));
        return configurator;
    }

    @Override
    public void addContext(Context context) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(context, "context");
        Class<? extends Annotation> scope = context.getScope();
        if (scope == null) {
            throw ContextLogger.LOG.contextHasNullScope(context);
        }
        if (!getBeanManager().isScope(scope)) {
            MetadataLogger.LOG.contextGetScopeIsNotAScope(scope, context);
        }
        if (scope == ApplicationScoped.class || scope == Dependent.class) {
            throw ContextLogger.LOG.cannotRegisterContext(scope, context);
        }
        getBeanManager().addContext(context);
        BootstrapLogger.LOG.addContext(getReceiver(), context);
    }

    @Override
    public void addObserverMethod(ObserverMethod<?> observerMethod) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(observerMethod, "observerMethod");
        validateObserverMethod(observerMethod, getBeanManager(), null);
        additionalObservers.add(ObserverRegistration.of(observerMethod, getReceiver()));
        BootstrapLogger.LOG.addObserverMethodCalled(getReceiver(), observerMethod);
    }

    @Override
    public <T> ObserverMethodConfigurator<T> addObserverMethod() {
        checkWithinObserverNotification();
        ObserverMethodConfiguratorImpl<T> configurator = new ObserverMethodConfiguratorImpl<>(getReceiver());
        additionalObservers.add(ObserverRegistration.of(configurator, getReceiver()));
        return configurator;
    }

    @Override
    public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return slimAnnotatedTypeStore.get(type, id);
    }

    @Override
    public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
        checkWithinObserverNotification();
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return cast(slimAnnotatedTypeStore.get(type));
    }

    @Override
    public InterceptorConfigurator addInterceptor() {
        InterceptorConfiguratorImpl configurator = new InterceptorConfiguratorImpl(getBeanManager());
        additionalBeans.add(BeanRegistration.of(configurator));
        return configurator;
    }

    /**
     * Bean and observer registration is delayed until after all {@link AfterBeanDiscovery} observers are notified.
     */
    private void finish() {
        try {
            GlobalEnablementBuilder globalEnablementBuilder = getBeanManager().getServices().get(GlobalEnablementBuilder.class);
            for (BeanRegistration registration : additionalBeans) {
                processBeanRegistration(registration, globalEnablementBuilder);
            }
            for (ObserverRegistration registration : additionalObservers) {
                processObserverRegistration(registration);
            }
        } catch (Exception e) {
            throw new DefinitionException(e);
        }
    }


    private <T> void processBeanRegistration(BeanRegistration registration, GlobalEnablementBuilder globalEnablementBuilder) {
        Bean<?> bean = registration.initBean();
        BeanManagerImpl beanManager = registration.initBeanManager();
        if (beanManager == null) {
            // Get the bean manager for beans added via ABD#addBean()
            beanManager = getOrCreateBeanDeployment(bean.getBeanClass()).getBeanManager();
        } else {
            // Also validate the bean produced by a builder
            ExternalBeanAttributesFactory.validateBeanAttributes(bean, getBeanManager());
            validateBean(bean);
        }

        // Custom beans (alternatives, interceptors, decorators) may also implementjakarta.enterprise.inject.spi.Prioritized
        Integer priority = (bean instanceof Prioritized) ? ((Prioritized) bean).getPriority() : null;
        // if added via WeldBeanConfigurator, there might be a priority specified as well
        if (priority == null && bean instanceof WeldBean) {
            priority = ((WeldBean) bean).getPriority();
        }

        if (bean instanceof Interceptor<?>) {
            beanManager.addInterceptor((Interceptor<?>) bean);
            if (priority != null) {
                globalEnablementBuilder.addInterceptor(bean.getBeanClass(), priority);
            }
        } else if (bean instanceof Decorator<?>) {
            beanManager.addDecorator(CustomDecoratorWrapper.of((Decorator<?>) bean, beanManager));
            if (priority != null) {
                globalEnablementBuilder.addDecorator(bean.getBeanClass(), priority);
            }
        } else {
            beanManager.addBean(bean);
            if (priority != null && bean.isAlternative()) {
                globalEnablementBuilder.addAlternative(bean.getBeanClass(), priority);
            }
        }
        containerLifecycleEvents.fireProcessBean(beanManager, bean, registration.extension);
    }

    private void validateBean(Bean<?> bean) {
        if (bean.getBeanClass() == null) {
            throw BeanLogger.LOG.beanMethodReturnsNull("getBeanClass", bean);
        }
        if (bean.getInjectionPoints() == null) {
            throw BeanLogger.LOG.beanMethodReturnsNull("getInjectionPoints", bean);
        }
        if (bean instanceof PassivationCapable) {
            PassivationCapable passivationCapable = (PassivationCapable) bean;
            if (passivationCapable.getId() == null) {
                throw BeanLogger.LOG.passivationCapableBeanHasNullId(bean);
            }
        }
        if (bean instanceof Interceptor<?>) {
            validateInterceptor((Interceptor<?>) bean);
        } else if (bean instanceof Decorator<?>) {
            validateDecorator((Decorator<?>) bean);
        }
    }

    private void validateInterceptor(Interceptor<?> interceptor) {
        Set<Annotation> bindings = interceptor.getInterceptorBindings();
        if (bindings == null) {
            throw InterceptorLogger.LOG.nullInterceptorBindings(interceptor);
        }
        for (Annotation annotation : bindings) {
            if (!getBeanManager().isInterceptorBinding(annotation.annotationType())) {
                throw MetadataLogger.LOG.notAnInterceptorBinding(annotation, interceptor);
            }
        }
    }

    private void validateDecorator(Decorator<?> decorator) {
        Set<Annotation> qualifiers = decorator.getDelegateQualifiers();
        if (decorator.getDelegateType() == null) {
            throw BeanLogger.LOG.decoratorMethodReturnsNull("getDelegateType", decorator);
        }
        Bindings.validateQualifiers(qualifiers, getBeanManager(), decorator, "Decorator.getDelegateQualifiers");
        if (decorator.getDecoratedTypes() == null) {
            throw BeanLogger.LOG.decoratorMethodReturnsNull("getDecoratedTypes", decorator);
        }

    }

    private void processObserverRegistration(ObserverRegistration registration) {
        ObserverMethod<?> observerMethod = registration.initObserverMethod();
        validateObserverMethod(observerMethod, getBeanManager(), null);
        BeanManagerImpl manager = getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager();
        if (Observers.isObserverMethodEnabled(observerMethod, manager)) {
            ObserverMethod<?> processedObserver = containerLifecycleEvents.fireProcessObserverMethod(manager, observerMethod, registration.extension);
            if (processedObserver != null) {
                manager.addObserver(processedObserver);
            }
        }
    }

    private BeanDeploymentFinder getBeanDeploymentFinder() {
        return new BeanDeploymentFinder(getBeanDeployments(), getDeployment(), getContexts(), getBeanManager());
    }

    private static class BeanRegistration {

        private final Bean<?> bean;

        private final BeanConfiguratorImpl<?> beanConfigurator;

        private final InterceptorConfiguratorImpl interceptorBuilder;

        private final Extension extension;

        static BeanRegistration of(Bean<?> bean, Extension extension) {
            return new BeanRegistration(bean, null, null, extension);
        }

        static BeanRegistration of(BeanConfiguratorImpl<?> configurator, Extension extension) {
            return new BeanRegistration(null, configurator, null, extension);
        }

        static BeanRegistration of(InterceptorConfiguratorImpl interceptorBuilder) {
            return new BeanRegistration(null, null, interceptorBuilder, null);
        }

        BeanRegistration(Bean<?> bean, BeanConfiguratorImpl<?> beanConfigurator, InterceptorConfiguratorImpl interceptorBuilder, Extension extension) {
            this.bean = bean;
            this.beanConfigurator = beanConfigurator;
            this.interceptorBuilder = interceptorBuilder;
            this.extension = extension;
        }

        public Bean<?> initBean() {
            if (bean != null) {
                return bean;
            } else if (beanConfigurator != null) {
                Bean<?> bean;
                try {
                    bean = beanConfigurator.complete();
                } catch (Exception e) {
                    throw BootstrapLogger.LOG.unableToProcessConfigurator(BeanConfigurator.class.getSimpleName(), extension, e);
                }
                BootstrapLogger.LOG.addBeanCalled(extension, bean);
                return bean;
            }
            return interceptorBuilder.build();
        }

        protected BeanManagerImpl initBeanManager() {
            if (bean != null) {
                return null;
            } else if (beanConfigurator != null) {
                return beanConfigurator.getBeanManager();
            }
            return interceptorBuilder.getBeanManager();
        }

    }

    private static class ObserverRegistration {

        private final ObserverMethod<?> observerMethod;

        private final Extension extension;

        private final ObserverMethodConfiguratorImpl<?> observerMethodConfigurator;

        static ObserverRegistration of(ObserverMethod<?> observerMethod, Extension extension) {
            return new ObserverRegistration(observerMethod, null, extension);
        }

        static <T> ObserverRegistration of(ObserverMethodConfiguratorImpl<T> configurator, Extension extension) {
            return new ObserverRegistration(null, configurator, extension);
        }

        private ObserverRegistration(ObserverMethod<?> observerMethod, ObserverMethodConfiguratorImpl<?> observerMethodBuilder, Extension extension) {
            this.observerMethod = observerMethod;
            this.observerMethodConfigurator = observerMethodBuilder;
            this.extension = extension;
        }

        ObserverMethod<?> initObserverMethod() {
            if (observerMethod != null) {
                return observerMethod;
            }
            try {
                return observerMethodConfigurator.complete();
            } catch (Exception e) {
                throw BootstrapLogger.LOG.unableToProcessConfigurator(ObserverMethodConfigurator.class.getSimpleName(), extension, e);
            }
        }

    }

}
