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

package org.jboss.webbeans.servlet;

import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.PropertiesBasedBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.DeploymentProperties;

/**
 * Bootstrapper for usage within servlet environments
 * 
 * @author Pete Muir
 */
public class ServletBootstrap extends PropertiesBasedBootstrap
{
   // The Web Beans manager
   private ManagerImpl manager;
   // The resource loader
   private ResourceLoader resourceLoader;
   // The discover implementation
   private WebBeanDiscovery webBeanDiscovery;
   // The deployment properties
   private DeploymentProperties deploymentProperties;
   
   public ServletBootstrap(ServletContext servletContext)
   {
      // Create the manager
      this.manager = new ManagerImpl();
      
      // Create a resouce loader based on the servlet context for initial loading 
      ResourceLoader servletContextResourceLoader = new ServletContextResourceLoader(servletContext);
      this.deploymentProperties = new DeploymentProperties(servletContextResourceLoader);
      
      // Attempt to create a plugin resource loader
      Constructor<? extends ResourceLoader> resourceLoaderConstructor = getClassConstructor(deploymentProperties, servletContextResourceLoader, ResourceLoader.PROPERTY_NAME, ResourceLoader.class, ServletContext.class);
      if (resourceLoaderConstructor != null)
      {
         this.resourceLoader = newInstance(resourceLoaderConstructor, servletContext);
      }
      else
      {
         // If no plugin resource loader, use the servlet context resource loader
         this.resourceLoader = servletContextResourceLoader;
      }
      
      // Now safe to initialize other properties and register the manager
      super.initProperties();
      super.registerManager();
      
      // Attempt to create a plugin web beans discovery
      Constructor<? extends WebBeanDiscovery> webBeanDiscoveryConstructor = getClassConstructor(deploymentProperties, resourceLoader, WebBeanDiscovery.PROPERTY_NAME, WebBeanDiscovery.class, ServletContext.class);
      if (webBeanDiscoveryConstructor == null)
      {
         throw new IllegalStateException("Cannot load Web Bean discovery plugin! Check if Web Beans is properly installed into your container");
      }
      else
      {
         this.webBeanDiscovery = newInstance(webBeanDiscoveryConstructor, servletContext);
      }
      
      // Register the contexts for the Servlet environment
      getManager().addContext(DependentContext.INSTANCE);
      getManager().addContext(RequestContext.INSTANCE);
      getManager().addContext(SessionContext.INSTANCE);
      getManager().addContext(ApplicationContext.INSTANCE);
      ApplicationContext.INSTANCE.setBeanMap(new ApplicationBeanMap(servletContext));
   }

   @Override
   protected DeploymentProperties getDeploymentProperties()
   {
      return deploymentProperties;
   }

   @Override
   public ResourceLoader getResourceLoader()
   {
      return resourceLoader;
   }

   @Override
   protected WebBeanDiscovery getWebBeanDiscovery()
   {
      return webBeanDiscovery;
   }
   
   @Override
   public ManagerImpl getManager()
   {
      return manager;
   }
   
}
