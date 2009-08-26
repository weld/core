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
package org.jboss.webbeans.bootstrap.api;

import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.jsf.spi.JSFServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.servlet.api.ServletServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.validation.spi.ValidationServices;

/**
 * Requirements for various well known environments.
 * 
 * @author Pete Muir
 *
 */
public enum Environments implements Environment
{
 
   /**
    * Java EE5 or Java EE6
    */
   EE(new EnvironmentBuilder()
         .addRequiredDeploymentService(TransactionServices.class)
         .addRequiredDeploymentService(ResourceLoader.class)
         .addRequiredDeploymentService(SecurityServices.class)
         .addRequiredDeploymentService(ValidationServices.class)
         .addRequiredDeploymentService(ServletServices.class)
         .addRequiredDeploymentService(JSFServices.class)
         .addRequiredBeanDeploymentArchiveService(JpaServices.class)
         .addRequiredBeanDeploymentArchiveService(ResourceServices.class)
         .addRequiredBeanDeploymentArchiveService(EjbServices.class)
   ),
   
   /**
    * Servlet container such as Tomcat
    */
   SERVLET(new EnvironmentBuilder()
         .addRequiredDeploymentService(ResourceLoader.class)
         .addRequiredDeploymentService(ServletServices.class)
   ),
   
   /**
    * Java SE
    */
   SE(new EnvironmentBuilder()
   );
   
   private final Set<Class<? extends Service>> requiredDeploymentServices;
   
   private final Set<Class<? extends Service>> requiredBeanDeploymentArchiveServices;
   
   private Environments(EnvironmentBuilder builder)
   {
      this.requiredDeploymentServices = builder.getRequiredDeploymentServices();
      this.requiredBeanDeploymentArchiveServices = builder.getRequiredBeanDeploymentArchiveServices();
   }

   public Set<Class<? extends Service>> getRequiredDeploymentServices()
   {
      return requiredDeploymentServices;
   }
   
   public Set<Class<? extends Service>> getRequiredBeanDeploymentArchiveServices()
   {
      return requiredBeanDeploymentArchiveServices;
   }
   
   private static class EnvironmentBuilder
   {
      
      private final Set<Class<? extends Service>> requiredDeploymentServices;
      
      private final Set<Class<? extends Service>> requiredBeanDeploymentArchiveServices;
      
      public EnvironmentBuilder()
      {
         this.requiredBeanDeploymentArchiveServices = new HashSet<Class<? extends Service>>();
         this.requiredDeploymentServices = new HashSet<Class<? extends Service>>();
      }
      
      public Set<Class<? extends Service>> getRequiredBeanDeploymentArchiveServices()
      {
         return requiredBeanDeploymentArchiveServices;
      }
      
      public Set<Class<? extends Service>> getRequiredDeploymentServices()
      {
         return requiredDeploymentServices;
      }
      
      public EnvironmentBuilder addRequiredDeploymentService(Class<? extends Service> service)
      {
         this.requiredDeploymentServices.add(service);
         return this;
      }
      
      public EnvironmentBuilder addRequiredBeanDeploymentArchiveService(Class<? extends Service> service)
      {
         this.requiredBeanDeploymentArchiveServices.add(service);
         return this;
      }
      
   }
   
}