package org.jboss.weld.mock;

import java.net.URL;
import java.util.Collection;

import org.jboss.weld.BeanManagerImpl;

/**
 * Control of the container, used for tests. Wraps up common operations.
 * 
 * If you require more control over the container bootstrap lifecycle you should
 * use the {@link #getLifecycle()} method. For example:
 * 
 * <code>TestContainer container = new TestContainer(...);
 * container.getLifecycle().initialize();
 * container.getLifecycle().getBootstrap().startInitialization();
 * container.getLifecycle().getBootstrap().deployBeans();
 * container.getLifecycle().getBootstrap().validateBeans();
 * container.getLifecycle().getBootstrap().endInitialization();
 * container.getLifecycle().stopContainer();</code>
 * 
 * Note that we can easily mix fine-grained calls to bootstrap, and coarse grained calls to {@link TestContainer}.
 * 
 * @author pmuir
 *
 */
public class TestContainer
{
   
   public static class Status
   {
      
      private final Exception deploymentException;

      public Status(Exception deploymentException)
      {
         this.deploymentException = deploymentException;
      }
      
      public Exception getDeploymentException()
      {
         return deploymentException;
      }
      
      public boolean isSuccess()
      {
         return deploymentException == null;
      }
      
   }
   
   private final MockServletLifecycle lifecycle;
   private final Collection<Class<?>> classes;
   private final Collection<URL> beansXml;
   
   /**
    * Create a container, specifying the classes and beans.xml to deploy
    * 
    * @param lifecycle
    * @param classes
    * @param beansXml
    */
   public TestContainer(MockServletLifecycle lifecycle, Collection<Class<?>> classes, Collection<URL> beansXml)
   {
      this.lifecycle = lifecycle;
      this.classes = classes;
      this.beansXml = beansXml;
      configureArchive();
   }
   
   /**
    * Start the container, returning the container state
    * 
    * @return
    */
   public Status startContainerAndReturnStatus()
   {
      try
      {
         startContainer();
      }
      catch (Exception e) 
      {
         return new Status(e);
      }
      return new Status(null);
   }
   
   /**
    * Starts the container and begins the application
    */
   public void startContainer()
   {
      getLifecycle().initialize();
      getLifecycle().beginApplication();
   }
   
   /**
    * Configure's the archive with the classes and beans.xml
    */
   protected void configureArchive()
   {
      MockBeanDeploymentArchive archive = lifecycle.getDeployment().getArchive();
      archive.setBeanClasses(classes);
      if (beansXml != null)
      {
         archive.setBeansXmlFiles(beansXml);
      }
   }
   
   /**
    * Get the context lifecycle, allowing fine control over the contexts' state
    * 
    * @return
    */
   public MockServletLifecycle getLifecycle()
   {
      return lifecycle;
   }
   
   public BeanManagerImpl getBeanManager()
   {
      return getLifecycle().getBootstrap().getManager(getDeployment().getArchive());
   }
   
   public MockDeployment getDeployment()
   {
      return getLifecycle().getDeployment();
   }
   
   /**
    * Utility method which ensures a request is active and available for use
    * 
    */
   public void ensureRequestActive()
   {
      if (!getLifecycle().isSessionActive())
      {
         getLifecycle().beginSession();
      }
      if (!getLifecycle().isRequestActive())
      {
         getLifecycle().beginRequest();
      }
   }

   /**
    * Clean up the container, ending any active contexts
    * 
    */
   public void stopContainer()
   {
      if (getLifecycle().isRequestActive())
      {
         getLifecycle().endRequest();
      }
      if (getLifecycle().isSessionActive())
      {
         getLifecycle().endSession();
      }
      if (getLifecycle().isApplicationActive())
      {
         getLifecycle().endApplication();
      }
   }

}