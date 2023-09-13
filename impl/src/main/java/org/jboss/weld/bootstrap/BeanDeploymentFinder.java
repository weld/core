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
package org.jboss.weld.bootstrap;

import java.util.Collection;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.DeploymentStructures;

/**
 *
 * @author Martin Kouba
 */
public final class BeanDeploymentFinder {

    private final BeanDeploymentArchiveMapping bdaMapping;

    private final Deployment deployment;

    private final Collection<ContextHolder<? extends Context>> contexts;

    private final BeanManagerImpl deploymentManager;

    /**
     *
     * @param bdaMapping
     * @param deployment
     * @param contexts
     * @param deploymentManager
     */
    public BeanDeploymentFinder(BeanDeploymentArchiveMapping bdaMapping, Deployment deployment,
            Collection<ContextHolder<? extends Context>> contexts,
            BeanManagerImpl deploymentManager) {
        this.bdaMapping = bdaMapping;
        this.deployment = deployment;
        this.contexts = contexts;
        this.deploymentManager = deploymentManager;
    }

    /**
     *
     * @param clazz
     * @return the bean deployment for the given class
     */
    public BeanDeployment getOrCreateBeanDeployment(Class<?> clazz) {
        return DeploymentStructures.getOrCreateBeanDeployment(deployment, deploymentManager, bdaMapping, contexts, clazz);
    }

}
