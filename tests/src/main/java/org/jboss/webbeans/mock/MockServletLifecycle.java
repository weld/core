package org.jboss.webbeans.mock;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class MockServletLifecycle extends ContextLifecycle
{
   private static final ResourceLoader MOCK_RESOURCE_LOADER = new MockResourceLoader();
   
   private final WebBeansBootstrap bootstrap;
   private final MockWebBeanDiscovery webBeanDiscovery;
   private final BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
   private final BeanStore sessionBeanStore = new ConcurrentHashMapBeanStore();
   private final BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
   
   public MockServletLifecycle()
   {
      this.webBeanDiscovery = new MockWebBeanDiscovery();
      if (webBeanDiscovery == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery is available");
      }
      bootstrap = new WebBeansBootstrap();
      bootstrap.setEnvironment(Environments.SERVLET);
      bootstrap.getServices().add(ResourceLoader.class, MOCK_RESOURCE_LOADER);
      bootstrap.getServices().add(WebBeanDiscovery.class, webBeanDiscovery);
      bootstrap.setApplicationContext(applicationBeanStore);
   }
   
   public void initialize()
   {
      bootstrap.initialize();
   }
   
   public MockWebBeanDiscovery getWebBeanDiscovery()
   {
      return webBeanDiscovery;
   }
   
   public WebBeansBootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   public void beginApplication()
   {
      bootstrap.boot();
   }
   
   public void endApplication()
   {
      bootstrap.shutdown();
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
}
