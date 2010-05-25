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

import java.util.List;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

/**
 * Weld Deployment for Java SE environment.
 * 
 * @author Peter Royle
 */
public class WeldSEDeployment implements Deployment
{

   public static final String[] RESOURCES = { "META-INF/beans.xml" };

   private final ServiceRegistry serviceRegistry;
   private final Scanner scanner;

   public WeldSEDeployment(Scanner scanner)
   {
      this.serviceRegistry = new SimpleServiceRegistry();
      this.scanner = scanner;
   }
   
   public Scanner getScanner()
   {
      return scanner;
   }

   /* 
    * Returns collection containing the singular logical BeanDeploymentArchive
    * consisting of all Bean classes and beans.xml descriptors in the current
    * classpath.
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return scanner.getBeanDeploymentArchives();
   }

   public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
   {
      return scanner.getBeanDeploymentArchive(beanClass);
   }

   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }

}
