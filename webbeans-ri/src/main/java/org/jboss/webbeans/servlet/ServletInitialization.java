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

import org.jboss.webbeans.bootstrap.PropertiesBasedInitialization;
import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resources.DefaultNamingContext;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.DeploymentProperties;

/**
 * Bootstrapper for usage within servlet environments
 * 
 * @author Pete Muir
 */
public class ServletInitialization extends PropertiesBasedInitialization
{
   
   // The deployment properties
   private final DeploymentProperties deploymentProperties;
   private final ResourceLoader resourceLoader;
   
   private final WebBeansBootstrap bootstrap;
   
   public ServletInitialization(ServletContext servletContext)
   {
       bootstrap = new WebBeansBootstrap();
      
      // Create a simple resource loader based for initial loading 
      ResourceLoader temporaryResourceLoader = new DefaultResourceLoader();
      this.deploymentProperties = new DeploymentProperties(temporaryResourceLoader);
      this.resourceLoader = createResourceLoader(servletContext, temporaryResourceLoader);
      
      bootstrap.setResourceLoader(resourceLoader);
      bootstrap.setNamingContext(createNaming(servletContext));
      bootstrap.setEjbResolver(createEjbResolver(servletContext));
      bootstrap.setEjbDiscovery(createEjbDiscovery(servletContext));
      bootstrap.setWebBeanDiscovery(createWebBeanDiscovery(servletContext));
   }
   
   protected NamingContext createNaming(ServletContext servletContext)
   {
      Constructor<? extends NamingContext> namingConstructor = getClassConstructor(deploymentProperties, resourceLoader, NamingContext.PROPERTY_NAME, NamingContext.class, ServletContext.class);
      if (namingConstructor != null)
      {
         return newInstance(namingConstructor, servletContext);
      }
      else
      {
         return new DefaultNamingContext();
      }
   }
   
   protected EjbResolver createEjbResolver(ServletContext servletContext)
   {
      Constructor<? extends EjbResolver> constructor = getClassConstructor(deploymentProperties, resourceLoader, EjbResolver.PROPERTY_NAME, EjbResolver.class, ServletContext.class);
      if (constructor != null)
      {
         return newInstance(constructor, servletContext);
      }
      else
      {
         throw new IllegalStateException("Unable to find a EjbResolver, check Web Beans is correctly installed in your container");
      }
   }
   
   protected EjbDiscovery createEjbDiscovery(ServletContext servletContext)
   {
      Constructor<? extends EjbDiscovery> constructor = getClassConstructor(deploymentProperties, resourceLoader, EjbDiscovery.PROPERTY_NAME, EjbDiscovery.class, ServletContext.class);
      if (constructor != null)
      {
         return newInstance(constructor, servletContext);
      }
      else
      {
         throw new IllegalStateException("Unable to find a EjbDiscovery, check Web Beans is correctly installed in your container");
      }
   }
   
   protected WebBeanDiscovery createWebBeanDiscovery(ServletContext servletContext)
   {
      // Attempt to create a plugin web beans discovery
      Constructor<? extends WebBeanDiscovery> webBeanDiscoveryConstructor = getClassConstructor(deploymentProperties, resourceLoader, WebBeanDiscovery.PROPERTY_NAME, WebBeanDiscovery.class, ServletContext.class);
      if (webBeanDiscoveryConstructor == null)
      {
         throw new IllegalStateException("Cannot load Web Bean discovery plugin! Check if Web Beans is properly installed into your container");
      }
      else
      {
         return newInstance(webBeanDiscoveryConstructor, servletContext);
      }
   }
   
   protected ResourceLoader createResourceLoader(ServletContext servletContext, ResourceLoader defaultResourceLoader)
   {
      // Attempt to create a plugin resource loader
      Constructor<? extends ResourceLoader> resourceLoaderConstructor = getClassConstructor(deploymentProperties, defaultResourceLoader, ResourceLoader.PROPERTY_NAME, ResourceLoader.class, ServletContext.class);
      if (resourceLoaderConstructor != null)
      {
         return newInstance(resourceLoaderConstructor, servletContext);
      }
      else
      {
         return defaultResourceLoader;
      }
   }
   
   public ServletInitialization initialize()
   {
      bootstrap.initialize();      
      return this;
   }
   
   public ServletInitialization start()
   {
      bootstrap.boot();
      return this;
   }

}
