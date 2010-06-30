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
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

public abstract class AbstractMockDeployment implements Deployment
{

   private final List<BeanDeploymentArchive> beanDeploymentArchives;
   private final ServiceRegistry services;

   public AbstractMockDeployment(BeanDeploymentArchive... beanDeploymentArchives)
   {
      this.services = new SimpleServiceRegistry();
      this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>(Arrays.asList(beanDeploymentArchives));
   }

   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives;
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

}