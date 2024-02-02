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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.events.ContainerLifecycleEvents;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.event.ObserverFactory;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.logging.EventLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.DeploymentStructures;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author pmuir
 */
public class ExtensionBeanDeployer {

    private final BeanManagerImpl beanManager;
    private final Set<Metadata<? extends Extension>> extensions;
    private final Deployment deployment;
    private final BeanDeploymentArchiveMapping bdaMapping;
    private final Collection<ContextHolder<? extends Context>> contexts;
    private final ContainerLifecycleEvents containerLifecycleEventObservers;
    private final MissingDependenciesRegistry missingDependenciesRegistry;

    public ExtensionBeanDeployer(BeanManagerImpl manager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Collection<ContextHolder<? extends Context>> contexts) {
        this.beanManager = manager;
        this.extensions = new HashSet<Metadata<? extends Extension>>();
        this.deployment = deployment;
        this.bdaMapping = bdaMapping;
        this.contexts = contexts;
        this.containerLifecycleEventObservers = beanManager.getServices().get(ContainerLifecycleEvents.class);
        this.missingDependenciesRegistry = beanManager.getServices().get(MissingDependenciesRegistry.class);
    }

    public ExtensionBeanDeployer deployBeans() {
        final ClassTransformer classTransformer = beanManager.getServices().get(ClassTransformer.class);
        for (Metadata<? extends Extension> extension : extensions) {
            deployBean(extension, classTransformer);
        }
        return this;
    }

    private <E extends Extension> void deployBean(Metadata<E> extension, ClassTransformer classTransformer) {
        // Locate the BeanDeployment for this extension
        BeanDeployment beanDeployment = DeploymentStructures.getOrCreateBeanDeployment(deployment, beanManager, bdaMapping,
                contexts, extension.getValue().getClass());

        // Do not register synthetic extension as a bean, only register container lifecycle observer methods
        if (extension.getValue() instanceof SyntheticExtension) {
            SyntheticExtension synthetic = (SyntheticExtension) extension.getValue();
            synthetic.initialize(beanDeployment.getBeanManager());
            for (ObserverMethod<?> observer : synthetic.getObservers()) {
                beanDeployment.getBeanManager().addObserver(observer);
                containerLifecycleEventObservers.processObserverMethod(observer);
            }
            return;
        }

        EnhancedAnnotatedType<E> enchancedAnnotatedType = getEnhancedAnnotatedType(classTransformer, extension, beanDeployment);

        if (enchancedAnnotatedType != null) {
            ExtensionBean<E> bean = new ExtensionBean<E>(beanDeployment.getBeanManager(), enchancedAnnotatedType, extension);
            Set<ObserverInitializationContext<?, ?>> observerMethodInitializers = new HashSet<ObserverInitializationContext<?, ?>>();
            createObserverMethods(bean, beanDeployment.getBeanManager(), enchancedAnnotatedType, observerMethodInitializers);
            beanDeployment.getBeanManager().addBean(bean);
            beanDeployment.getBeanDeployer().addExtension(bean);
            for (ObserverInitializationContext<?, ?> observerMethodInitializer : observerMethodInitializers) {
                observerMethodInitializer.initialize();
                beanDeployment.getBeanManager().addObserver(observerMethodInitializer.getObserver());
                containerLifecycleEventObservers.processObserverMethod(observerMethodInitializer.getObserver());
            }
            BootstrapLogger.LOG.extensionBeanDeployed(bean);
        }
    }

    private <E extends Extension> EnhancedAnnotatedType<E> getEnhancedAnnotatedType(ClassTransformer classTransformer,
            Metadata<E> extension,
            BeanDeployment beanDeployment) {
        Class<? extends Extension> clazz = extension.getValue().getClass();
        try {
            return cast(classTransformer.getEnhancedAnnotatedType(clazz, beanDeployment.getBeanDeploymentArchive().getId()));
        } catch (ResourceLoadingException e) {
            String missingDependency = Formats.getNameOfMissingClassLoaderDependency(e);
            BootstrapLogger.LOG.ignoringExtensionClassDueToLoadingError(clazz.getName(), missingDependency);
            BootstrapLogger.LOG.catchingDebug(e);
            missingDependenciesRegistry.registerClassWithMissingDependency(clazz.getName(), missingDependency);
            return null;
        }
    }

    public void addExtensions(Iterable<Metadata<? extends Extension>> extensions) {
        for (Metadata<? extends Extension> extension : extensions) {
            addExtension(extension);
        }
    }

    public void addExtension(Metadata<? extends Extension> extension) {
        this.extensions.add(extension);
    }

    protected <X> void createObserverMethods(RIBean<X> declaringBean, BeanManagerImpl beanManager,
            EnhancedAnnotatedType<? super X> annotatedClass,
            Set<ObserverInitializationContext<?, ?>> observerMethodInitializers) {
        for (EnhancedAnnotatedMethod<?, ? super X> method : BeanMethods.getObserverMethods(annotatedClass)) {
            createObserverMethod(declaringBean, beanManager, method, observerMethodInitializers, false);
        }
        for (EnhancedAnnotatedMethod<?, ? super X> method : BeanMethods.getAsyncObserverMethods(annotatedClass)) {
            createObserverMethod(declaringBean, beanManager, method, observerMethodInitializers, true);
        }
    }

    protected <T, X> void createObserverMethod(RIBean<X> declaringBean, BeanManagerImpl beanManager,
            EnhancedAnnotatedMethod<T, ? super X> method,
            Set<ObserverInitializationContext<?, ?>> observerMethodInitializers, boolean isAsync) {
        ObserverMethodImpl<T, X> observer = ObserverFactory.create(method, declaringBean, beanManager, isAsync);
        ObserverInitializationContext<T, X> observerMethodInitializer = ObserverInitializationContext.of(observer, method);
        if (Observers.isContainerLifecycleObserverMethod(observer)) {
            if (isAsync) {
                throw EventLogger.LOG.asyncContainerLifecycleEventObserver(observer,
                        Formats.formatAsStackTraceElement(method.getJavaMember()));
            }
            if (method.isStatic()) {
                throw EventLogger.LOG.staticContainerLifecycleEventObserver(observer,
                        Formats.formatAsStackTraceElement(method.getJavaMember()));
            }
        }
        observerMethodInitializers.add(observerMethodInitializer);
    }

}
