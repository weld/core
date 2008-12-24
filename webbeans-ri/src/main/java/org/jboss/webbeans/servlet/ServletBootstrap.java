package org.jboss.webbeans.servlet;


import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.PropertiesBasedBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.DeploymentProperties;

public class ServletBootstrap extends PropertiesBasedBootstrap
{
   
   private ManagerImpl manager;
   private ResourceLoader resourceLoader;
   private WebBeanDiscovery webBeanDiscovery;
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
