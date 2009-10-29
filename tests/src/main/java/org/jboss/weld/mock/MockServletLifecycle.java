package org.jboss.weld.mock;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.helpers.ForwardingLifecycle;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.servlet.api.ServletServices;

public class MockServletLifecycle extends ForwardingLifecycle implements MockLifecycle
{
   private static final ResourceLoader MOCK_RESOURCE_LOADER = new MockResourceLoader();
   
   private final WeldBootstrap bootstrap;
   private final MockDeployment deployment;
   private final BeanStore applicationBeanStore;
   private final BeanStore sessionBeanStore;
   private final BeanStore requestBeanStore;
   
   private Lifecycle lifecycle;
   
   public MockServletLifecycle()
   {
      this.deployment = new MockDeployment();
      if (deployment == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery is available");
      }
      this.bootstrap = new WeldBootstrap();
      this.deployment.getServices().add(ResourceLoader.class, MOCK_RESOURCE_LOADER);
      this.deployment.getServices().add(ServletServices.class, new MockServletServices(deployment.getArchive()));
      this.applicationBeanStore = new ConcurrentHashMapBeanStore();
      this.sessionBeanStore = new ConcurrentHashMapBeanStore();
      this.requestBeanStore = new ConcurrentHashMapBeanStore();
   }
   
   protected BeanStore getSessionBeanStore()
   {
      return sessionBeanStore;
   }
   
   protected BeanStore getRequestBeanStore()
   {
      return requestBeanStore;
   }
   
   protected BeanStore getApplicationBeanStore()
   {
      return applicationBeanStore;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#initialize()
    */
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
   
   protected MockDeployment getDeployment()
   {
      return deployment;
   }
   
   public WeldBootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#beginApplication()
    */
   public void beginApplication()
   {
      bootstrap.startInitialization().deployBeans().validateBeans().endInitialization();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#endApplication()
    */
   @Override
   public void endApplication()
   {
      bootstrap.shutdown();
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#resetContexts()
    */
   public void resetContexts()
   {
      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#beginRequest()
    */
   public void beginRequest()
   {
      super.beginRequest("Mock", getRequestBeanStore());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#endRequest()
    */
   public void endRequest()
   {
      super.endRequest("Mock", getRequestBeanStore());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#beginSession()
    */
   public void beginSession()
   {
      super.restoreSession("Mock", getSessionBeanStore());
   }
   
   /* (non-Javadoc)
    * @see org.jboss.weld.mock.MockLifecycle#endSession()
    */
   public void endSession()
   {
      // TODO Conversation handling breaks this :-(
      //super.endSession("Mock", sessionBeanStore);
   }
   
   protected Environment getEnvironment()
   {
      return Environments.SERVLET;
   }
}
