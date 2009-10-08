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
package org.jboss.weld.bootstrap.api;

import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.servlet.api.ServletServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;

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
    * 
    * In this environment, Weld requires that {@link JpaInjectionServices},
    * {@link ResourceInjectionServices} and {@link EjbInjectionServices} are
    * present, and so will perform EE-style field injection on managed beans
    * itself
    */
   EE_INJECT(new EnvironmentBuilder()
         .addRequiredDeploymentService(TransactionServices.class)
         .addRequiredDeploymentService(ResourceLoader.class)
         .addRequiredDeploymentService(SecurityServices.class)
         .addRequiredDeploymentService(ValidationServices.class)
         .addRequiredDeploymentService(ServletServices.class)
         .addRequiredDeploymentService(EjbServices.class)
         .addRequiredBeanDeploymentArchiveService(JpaInjectionServices.class)
         .addRequiredBeanDeploymentArchiveService(ResourceInjectionServices.class)
         .addRequiredBeanDeploymentArchiveService(EjbInjectionServices.class)
   ),
   
   /**
    * Java EE5 or Java EE6
    * 
    * In this environment, Weld requires that {@link InjectionServices} are
    * present, and expects the container to use this callback to perform EE-style
    * injection
    * 
    */
   EE(new EnvironmentBuilder()
         .addRequiredDeploymentService(TransactionServices.class)
         .addRequiredDeploymentService(ResourceLoader.class)
         .addRequiredDeploymentService(SecurityServices.class)
         .addRequiredDeploymentService(ValidationServices.class)
         .addRequiredDeploymentService(ServletServices.class)
         .addRequiredDeploymentService(EjbServices.class)
         .addRequiredBeanDeploymentArchiveService(InjectionServices.class)
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
      
      private EnvironmentBuilder()
      {
         this.requiredBeanDeploymentArchiveServices = new HashSet<Class<? extends Service>>();
         this.requiredDeploymentServices = new HashSet<Class<? extends Service>>();
      }
      
      private Set<Class<? extends Service>> getRequiredBeanDeploymentArchiveServices()
      {
         return requiredBeanDeploymentArchiveServices;
      }
      
      private Set<Class<? extends Service>> getRequiredDeploymentServices()
      {
         return requiredDeploymentServices;
      }
      
      private EnvironmentBuilder addRequiredDeploymentService(Class<? extends Service> service)
      {
         this.requiredDeploymentServices.add(service);
         return this;
      }
      
      private EnvironmentBuilder addRequiredBeanDeploymentArchiveService(Class<? extends Service> service)
      {
         this.requiredBeanDeploymentArchiveServices.add(service);
         return this;
      }
      
   }
   
}