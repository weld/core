/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.bootstrap.api.test;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * @author pmuir
 *
 */
public class MockDeployment implements Deployment
{
   
   static class MockBeanDeploymentArchive implements BeanDeploymentArchive
   {

      private final ServiceRegistry services; 
      
      public MockBeanDeploymentArchive(ServiceRegistry services)
      {
         this.services = services;
      }

      public Collection<Class<?>> getBeanClasses()
      {
         return Collections.emptySet();
      }

      public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
      {
         return Collections.emptySet();
      }

      public Collection<URL> getBeansXml()
      {
         return Collections.emptySet();
      }

      public Collection<EjbDescriptor<?>> getEjbs()
      {
         return Collections.emptySet();
      }

      public ServiceRegistry getServices()
      {
         return services;
      }
      
      public String getId()
      {
         return "test";
      }
      
   }
   
   private final ServiceRegistry services;
   private final BeanDeploymentArchive beanDeploymentArchive;

   public MockDeployment(ServiceRegistry services, MockBeanDeploymentArchive beanDeploymentArchive)
   {
      this.services = services;
      this.beanDeploymentArchive = beanDeploymentArchive;
   }

   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.singletonList(beanDeploymentArchive);
   }

   public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
   {
      return null;
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

}
