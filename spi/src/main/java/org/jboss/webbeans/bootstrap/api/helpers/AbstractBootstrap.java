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
package org.jboss.webbeans.bootstrap.api.helpers;

import static org.jboss.webbeans.bootstrap.api.Environments.EE;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.transaction.spi.TransactionServices;

/**
 * A common implementation of {@link Bootstrap}.
 * 
 * Not threadsafe
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractBootstrap implements Bootstrap
{
   private final ServiceRegistry serviceRegistry;
   private Environment environment = EE;
   
   private BeanStore applicationContext;
   
   public AbstractBootstrap()
   {
      this.serviceRegistry = new ServiceRegistry();
   }

   @Deprecated
   public void setEjbDiscovery(EjbDiscovery ejbDiscovery)
   {
      getServices().add(EjbDiscovery.class, ejbDiscovery);
   }

   @Deprecated
   public void setEjbServices(EjbServices ejbServices)
   {
      getServices().add(EjbServices.class, ejbServices);
   }

   @Deprecated
   public void setNamingContext(NamingContext namingContext)
   {
      getServices().add(NamingContext.class, namingContext);
   }

   @Deprecated
   public void setResourceLoader(ResourceLoader resourceLoader)
   {
      getServices().add(ResourceLoader.class, resourceLoader);
   }

   @Deprecated
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      getServices().add(WebBeanDiscovery.class, webBeanDiscovery);
   }
   
   @Deprecated
   public void setTransactionServices(TransactionServices transactionServices)
   {
      getServices().add(TransactionServices.class, transactionServices);
   }

   public WebBeanDiscovery getWebBeanDiscovery()
   {
      return getServices().get(WebBeanDiscovery.class);
   }

   @Deprecated
   public ResourceLoader getResourceLoader()
   {
      return getServices().get(ResourceLoader.class);
   }

   @Deprecated
   public NamingContext getNamingContext()
   {
      return getServices().get(NamingContext.class);
   }

   @Deprecated
   public EjbServices getEjbServices()
   {
      return getServices().get(EjbServices.class);
   }

   @Deprecated
   public EjbDiscovery getEjbDiscovery()
   {
      return getServices().get(EjbDiscovery.class);
   }
   
   @Deprecated
   public TransactionServices getTransactionServices()
   {
      return getServices().get(TransactionServices.class);
   }
   
   public BeanStore getApplicationContext()
   {
      return applicationContext;
   }
   
   public void setApplicationContext(BeanStore applicationContext)
   {
      this.applicationContext = applicationContext;      
   }
   
   public Environment getEnvironment()
   {
      return environment;
   }
   
   public void setEnvironment(Environment environment)
   {
      this.environment = environment;
   }
   
   protected void verify()
   {
      for (Class<? extends Service> serviceType : environment.getRequiredServices())
      {
         if (!getServices().contains(serviceType))
         {
            throw new IllegalStateException("Required service " + serviceType.getName() + " has not been specified");
         }
      }
   }
   
   public ServiceRegistry getServices()
   {
      return serviceRegistry;
   }
   
}