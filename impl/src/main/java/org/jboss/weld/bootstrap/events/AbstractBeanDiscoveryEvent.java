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

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.util.DeploymentStructures;

/**
 * @author pmuir
 */
public abstract class AbstractBeanDiscoveryEvent extends AbstractDefinitionContainerEvent {

    private final BeanDeploymentArchiveMapping bdaMapping;
    private final Deployment deployment;
    private final Collection<ContextHolder<? extends Context>> contexts;

    public AbstractBeanDiscoveryEvent(BeanManagerImpl beanManager, Type rawType, BeanDeploymentArchiveMapping bdaMapping,
            Deployment deployment, Collection<ContextHolder<? extends Context>> contexts) {
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

    /**
     *
     * @return the contexts
     */
    protected Collection<ContextHolder<? extends Context>> getContexts() {
        return contexts;
    }

    protected TypeStore getTypeStore() {
        return getDeployment().getServices().get(TypeStore.class);
    }

    protected BeanDeployment getOrCreateBeanDeployment(Class<?> clazz) {
        return DeploymentStructures.getOrCreateBeanDeployment(deployment, getBeanManager(), bdaMapping, contexts, clazz);
    }
}
