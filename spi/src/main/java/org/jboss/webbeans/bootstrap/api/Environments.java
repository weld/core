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

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.jpa.spi.JpaServices;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.transaction.spi.TransactionServices;

/**
 * Various well known environments.
 * 
 * @author Pete Muir
 *
 */
public enum Environments implements Environment
{
 
   /**
    * Java EE5 or Java EE6
    */
   EE(WebBeanDiscovery.class, EjbServices.class, JpaServices.class, TransactionServices.class, NamingContext.class, ResourceLoader.class),
   
   /**
    * Java EE6 Web Profile
    */
   EE_WEB_PROFILE(WebBeanDiscovery.class, EjbServices.class, JpaServices.class,TransactionServices.class, NamingContext.class, ResourceLoader.class),
   
   /**
    * Servlet container such as Tomcat
    */
   SERVLET(WebBeanDiscovery.class, NamingContext.class, ResourceLoader.class),
   
   /**
    * Java SE
    */
   SE(WebBeanDiscovery.class, NamingContext.class, ResourceLoader.class);
   
   private Set<Class<? extends Service>> requiredServices;
   
   private Environments(Class<? extends Service>... requiredServices)
   {
      this.requiredServices = new HashSet<Class<? extends Service>>(Arrays.asList(requiredServices));
   }

   public Set<Class<? extends Service>> getRequiredServices()
   {
      return requiredServices;
   }
   
}