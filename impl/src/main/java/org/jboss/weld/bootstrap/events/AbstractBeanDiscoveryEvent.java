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

import static org.jboss.weld.util.reflection.Reflections.EMPTY_TYPES;

import java.lang.reflect.Type;
import java.util.Collection;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.util.DeploymentStructures;

/**
 * @author pmuir
 */
public abstract class AbstractBeanDiscoveryEvent extends AbstractDefinitionContainerEvent implements NotificationListener {

    private final BeanDeploymentArchiveMapping bdaMapping;
    private final Deployment deployment;
    private final Collection<ContextHolder<? extends Context>> contexts;

    /*
     * The receiver object and the observer method being used for event dispatch at a given time. This information is required
     * for implementing ProcessSyntheticAnnotatedType properly. The information must be set by an
     * ObserverMethod implementation before invoking the target observer method and unset once the notification is complete.
     */
    private Extension receiver;

    public AbstractBeanDiscoveryEvent(BeanManagerImpl beanManager, Type rawType, BeanDeploymentArchiveMapping bdaMapping, Deployment deployment, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, rawType, EMPTY_TYPES);
        this.bdaMapping = bdaMapping;
        this.deployment = deployment;
        this.contexts = contexts;
    }

    /**
     * @return the bdaMapping
     */
    protected BeanDeploymentArchiveMapping getBeanDeployments() {
        return bdaMapping;
    }

    /**
     * @return the deployment
     */
    protected Deployment getDeployment() {
        return deployment;
    }

    protected TypeStore getTypeStore() {
        return getDeployment().getServices().get(TypeStore.class);
    }


    protected BeanDeployment getOrCreateBeanDeployment(Class<?> clazz) {
        return DeploymentStructures.getOrCreateBeanDeployment(deployment, getBeanManager(), bdaMapping, contexts, clazz);
    }

    @Override
    public void preNotify(Extension extension) {
        this.receiver = extension;
    }

    @Override
    public void postNotify(Extension extension) {
        this.receiver = null;
    }

    protected Extension getSyntheticAnnotatedTypeSource() {
        return receiver;
    }

    /**
     * Checks that this event is currently being delivered to an extension. Otherwise, {@link IllegalStateException}
     * is thrown. This guarantees that methods of container lifecycle events are not called outside of extension
     * observer method invocations.
     *
     * @throws IllegalStateException if this method is not called within extension observer method invocation
     */
    protected void checkWithinObserverNotification() {
        if (receiver == null) {
            throw BootstrapLogger.LOG.containerLifecycleEventMethodInvokedOutsideObserver();
        }
    }
}
