/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE;

import java.util.Map;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.manager.BeanManagerImpl;

public class DeploymentStructures
{
  
   private DeploymentStructures() {}
   
   public static BeanDeployment getOrCreateBeanDeployment(Deployment deployment, BeanManagerImpl deploymentManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Class<?> clazz)
   {
      BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
      if (beanDeploymentArchive == null)
      {
         throw new ForbiddenStateException(UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE, clazz);
      }
      else
      {
         if (beanDeployments.containsKey(beanDeploymentArchive))
         {
            return beanDeployments.get(beanDeploymentArchive);
         }
         else
         {
            BeanDeployment beanDeployment = new BeanDeployment(beanDeploymentArchive, deploymentManager, deployment.getServices());
            beanDeployments.put(beanDeploymentArchive, beanDeployment);
            return beanDeployment;
         }
      }
   }

}
