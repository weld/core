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
import java.util.Map;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.util.DeploymentStructures;

/**
 * @author  pmuir
 */
public abstract class AbstractBeanDiscoveryEvent extends AbstractDefinitionContainerEvent
{
   
   private final Map<BeanDeploymentArchive, BeanDeployment> beanDeployments;
   private final Deployment deployment;
   
   public AbstractBeanDiscoveryEvent(BeanManagerImpl beanManager, Type rawType, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Deployment deployment)
   {
      super(beanManager, rawType, EMPTY_TYPES);
      this.beanDeployments = beanDeployments;
      this.deployment = deployment;
   }
   
   /**
    * @return the beanDeployments
    */
   protected Map<BeanDeploymentArchive, BeanDeployment> getBeanDeployments()
   {
      return beanDeployments;
   }
   
   /**
    * @return the deployment
    */
   protected Deployment getDeployment()
   {
      return deployment;
   }
   
   protected TypeStore getTypeStore()
   {
      return getDeployment().getServices().get(TypeStore.class);
   }
   

   protected BeanDeployment getOrCreateBeanDeployment(Class<?> clazz)
   {
      return DeploymentStructures.getOrCreateBeanDeployment(deployment, getBeanManager(), beanDeployments, clazz);
   }
   
}