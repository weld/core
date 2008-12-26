package org.jboss.webbeans.test.mock;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.SimpleBeanMap;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public class MockBootstrap extends WebBeansBootstrap
{ 
   
   private WebBeanDiscovery webBeanDiscovery;
   private ResourceLoader resourceLoader;
   private MockManagerImpl manager;
   
   public MockBootstrap()
   {
      this.manager = new MockManagerImpl();
      this.resourceLoader = new MockResourceLoader();
      registerManager();
      registerStandardBeans();
      
      // Set up the mock contexts
      manager.addContext(RequestContext.INSTANCE);
      SessionContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      manager.addContext(SessionContext.INSTANCE);
      ApplicationContext.INSTANCE.setBeanMap(new SimpleBeanMap());
      manager.addContext(ApplicationContext.INSTANCE);
      manager.addContext(DependentContext.INSTANCE);
   }
   
   protected void registerStandardBeans()
   {
      getManager().setBeans(createStandardBeans());
   }
   
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
   }
   
   @Override
   protected WebBeanDiscovery getWebBeanDiscovery()
   {
      return this.webBeanDiscovery;
   }

   @Override
   public ResourceLoader getResourceLoader()
   {
      return resourceLoader;
   }

   @Override
   public MockManagerImpl getManager()
   {
      return manager;
   }
   
}
