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

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.EnablementBuilder;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;

import javax.enterprise.context.spi.Context;
import java.util.Collection;
import java.util.Map;

import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE;

public class DeploymentStructures {

    private DeploymentStructures() {
    }

    public static BeanDeployment getOrCreateBeanDeployment(Deployment deployment, BeanManagerImpl deploymentManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts, Class<?> clazz, EnablementBuilder enablementBuilder) {
        BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
        if (beanDeploymentArchive == null) {
            throw new IllegalStateException(UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE, clazz);
        } else {
            BeanDeployment beanDeployment = beanDeployments.get(beanDeploymentArchive);
            if (beanDeployment == null) {
                beanDeployment = new BeanDeployment(beanDeploymentArchive, deploymentManager, deployment.getServices(), contexts, enablementBuilder);
                beanDeployments.put(beanDeploymentArchive, beanDeployment);
            }
            return beanDeployment;
        }
    }

}
