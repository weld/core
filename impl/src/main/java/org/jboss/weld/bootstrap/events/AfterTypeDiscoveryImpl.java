/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeContext;
import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.GlobalEnablementBuilder;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

public class AfterTypeDiscoveryImpl extends AbstractAnnotatedTypeRegisteringEvent implements AfterTypeDiscovery {

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Collection<ContextHolder<? extends Context>> contexts) {
        AfterTypeDiscoveryImpl event = new AfterTypeDiscoveryImpl(beanManager, bdaMapping, deployment, contexts);
        event.fire();
        event.finish();
    }

    private final GlobalEnablementBuilder builder;
    private final ContainerLifecycleEvents events;
    private final ClassTransformer transformer;
    private final SlimAnnotatedTypeStore store;

    protected AfterTypeDiscoveryImpl(BeanManagerImpl beanManager, BeanDeploymentArchiveMapping bdaMapping,
            Deployment deployment,
            Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, AfterTypeDiscovery.class, bdaMapping, deployment, contexts);
        this.builder = beanManager.getServices().get(GlobalEnablementBuilder.class);
        this.events = beanManager.getServices().get(ContainerLifecycleEvents.class);
        this.transformer = beanManager.getServices().get(ClassTransformer.class);
        this.store = beanManager.getServices().get(SlimAnnotatedTypeStore.class);
    }

    @Override
    public List<Class<?>> getAlternatives() {
        checkWithinObserverNotification();
        return builder.getAlternativeList(getReceiver());
    }

    @Override
    public List<Class<?>> getInterceptors() {
        checkWithinObserverNotification();
        return builder.getInterceptorList(getReceiver());
    }

    @Override
    public List<Class<?>> getDecorators() {
        checkWithinObserverNotification();
        return builder.getDecoratorList(getReceiver());
    }

    @Override
    public void addAnnotatedType(AnnotatedType<?> type, String id) {
        checkWithinObserverNotification();
        addSyntheticAnnotatedType(type, id);
        BootstrapLogger.LOG.addAnnotatedTypeCalled(getReceiver(), type);
    }

    @Override
    public <T> AnnotatedTypeConfigurator<T> addAnnotatedType(Class<T> type, String id) {
        checkWithinObserverNotification();
        AnnotatedTypeConfiguratorImpl<T> configurator = new AnnotatedTypeConfiguratorImpl<>(
                getBeanManager().createAnnotatedType(type));
        additionalAnnotatedTypes.add(new AnnotatedTypeRegistration<T>(configurator, id));
        return configurator;
    }

    @Override
    protected void storeSyntheticAnnotatedType(BeanDeployment deployment, AnnotatedType<?> type, String id) {
        SlimAnnotatedType<?> annotatedType = transformer.getUnbackedAnnotatedType(type, getBeanManager().getId(), id);
        Extension extension = getReceiver();
        SlimAnnotatedTypeContext<?> annotatedTypeContext = SlimAnnotatedTypeContext.of(annotatedType, extension);

        ProcessAnnotatedTypeImpl<?> event = events.fireProcessAnnotatedType(getBeanManager(), annotatedTypeContext);
        if (event == null) {
            deployment.getBeanDeployer().getEnvironment().addAnnotatedType(annotatedTypeContext);
            store.put(annotatedType);
        } else if (event.isVeto()) {
            return;
        } else {
            annotatedType = event.getResultingAnnotatedType();
            deployment.getBeanDeployer().getEnvironment().addSyntheticAnnotatedType(annotatedType, extension);
            store.put(annotatedType);
        }
    }
}
