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
package org.jboss.weld.util;

import java.util.Collection;

import jakarta.enterprise.context.spi.Context;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.manager.BeanManagerImpl;

public class DeploymentStructures {

    private DeploymentStructures() {
    }

    public static BeanDeployment getOrCreateBeanDeployment(Deployment deployment, BeanManagerImpl deploymentManager,
            BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts, Class<?> clazz) {
        BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
        if (beanDeploymentArchive == null) {
            throw UtilLogger.LOG.unableToFindBeanDeploymentArchive(clazz);
        } else {
            BeanDeployment beanDeployment = bdaMapping.getBeanDeployment(beanDeploymentArchive);
            if (beanDeployment == null) {
                beanDeployment = new BeanDeployment(beanDeploymentArchive, deploymentManager, deployment.getServices(),
                        contexts, true);
                bdaMapping.put(beanDeploymentArchive, beanDeployment);
            }
            return beanDeployment;
        }
    }

    public static BeanDeployment getBeanDeploymentIfExists(Deployment deployment, BeanDeploymentArchiveMapping bdaMapping,
            Class<?> clazz) {
        BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
        if (beanDeploymentArchive == null) {
            throw UtilLogger.LOG.unableToFindBeanDeploymentArchive(clazz);
        } else {
            // can be null
            return bdaMapping.getBeanDeployment(beanDeploymentArchive);
        }
    }

}
