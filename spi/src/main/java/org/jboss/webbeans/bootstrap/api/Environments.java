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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.persistence.spi.helpers.JSFServices;
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
   EE(Deployment.class, EjbServices.class, JpaServices.class, ResourceServices.class, TransactionServices.class, ResourceLoader.class, SecurityServices.class, ValidationServices.class, ServletServices.class, JSFServices.class),
   
   /**
    * Java EE6 Web Profile
    */
   EE_WEB_PROFILE(Deployment.class, EjbServices.class, JpaServices.class, ResourceServices.class, TransactionServices.class, ResourceLoader.class, SecurityServices.class, ValidationServices.class, ServletServices.class, JSFServices.class),
   
   /**
    * Servlet container such as Tomcat
    */
   SERVLET(Deployment.class, ResourceLoader.class, ServletServices.class),
   
   /**
    * Java SE
    */
   SE(Deployment.class, ResourceLoader.class);
   
   private final Set<Class<? extends Service>> requiredServices;
   
   private Environments(Class<? extends Service>... requiredServices)
   {
      this.requiredServices = new HashSet<Class<? extends Service>>(Arrays.asList(requiredServices));
   }

   public Set<Class<? extends Service>> getRequiredServices()
   {
      return requiredServices;
   }
   
}