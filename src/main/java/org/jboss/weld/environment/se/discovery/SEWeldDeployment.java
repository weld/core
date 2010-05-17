/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.discovery;

import java.util.ArrayList;
import java.util.List;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

/**
 * Weld Deployment for Java SE environment.
 * 
 * @author Peter Royle
 */
public class SEWeldDeployment implements Deployment
{
   private final SEBeanDeploymentArchive beanDeploymentArchive;
   private final List<BeanDeploymentArchive> archInCollection;

   public SEWeldDeployment(SEBeanDeploymentArchive beanDeploymentArchive)
   {
      this.beanDeploymentArchive = beanDeploymentArchive;
      this.archInCollection = new ArrayList<BeanDeploymentArchive>(1);
      this.archInCollection.add(this.beanDeploymentArchive);
   }

   /**
    * {@inheritDoc}
    * 
    * @return A collection containing the singular logical BeanDeploymentArchive
    *         consisting of all Bean classes and beans.xml descriptors in the
    *         current classpath.
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return this.archInCollection;
   }

   /**
    * {@inheritDoc}
    * 
    * @return The singular logical BeanDeploymentArchive consisting of all which
    *         contains all Beans classes.
    */
   public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
   {
      return this.beanDeploymentArchive;
   }

   public ServiceRegistry getServices()
   {
      return this.beanDeploymentArchive.getServices();
   }
}
