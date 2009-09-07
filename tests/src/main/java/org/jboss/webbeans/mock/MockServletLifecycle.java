package org.jboss.webbeans.mock;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.Lifecycle;
import org.jboss.webbeans.bootstrap.api.helpers.ForwardingLifecycle;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.api.ServletServices;

public class MockServletLifecycle extends ForwardingLifecycle
{
   private static final ResourceLoader MOCK_RESOURCE_LOADER = new MockResourceLoader();
   
   private final WebBeansBootstrap bootstrap;
   private final MockDeployment deployment;
   private final BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
   private final BeanStore sessionBeanStore = new ConcurrentHashMapBeanStore();
   private final BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
   
   private Lifecycle lifecycle;
   
   public MockServletLifecycle()
   {
      this.deployment = new MockDeployment();
      if (deployment == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery is available");
      }
      bootstrap = new WebBeansBootstrap();
      deployment.getServices().add(ResourceLoader.class, MOCK_RESOURCE_LOADER);
      deployment.getServices().add(ServletServices.class, new MockServletServices(deployment.getArchive()));
   }
   
   public void initialize()
   {
      try
      {
         bootstrap.startContainer(getEnvironment(), getDeployment(), getApplicationBeanStore());
      }
      finally  
      {
         lifecycle = deployment.getServices().get(ContextLifecycle.class);
      }
   }
   
   @Override
   protected Lifecycle delegate()
   {
      return lifecycle;
   }
   
   public MockDeployment getDeployment()
   {
      return deployment;
   }
   
   public WebBeansBootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   public void beginApplication()
   {
      bootstrap.startInitialization().deployBeans().validateBeans().endInitialization();
   }
   
   public void endApplication()
   {
      bootstrap.shutdown();
   }
   
   public BeanStore getApplicationBeanStore()
   {
      return applicationBeanStore;
   }
   
   public void resetContexts()
   {
      
   }
   
   public void beginRequest()
   {
      super.beginRequest("Mock", requestBeanStore);
   }
   
   public void endRequest()
   {
      super.endRequest("Mock", requestBeanStore);
   }
   
   public void beginSession()
   {
      super.restoreSession("Mock", sessionBeanStore);
   }
   
   public void endSession()
   {
      // TODO Conversation handling breaks this :-(
      //super.endSession("Mock", sessionBeanStore);
   }
   
   public Environment getEnvironment()
   {
      return Environments.SERVLET;
   }
}
