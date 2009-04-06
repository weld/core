package org.jboss.webbeans.test;

import java.net.URL;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.mock.MockEELifecycle;
import org.jboss.webbeans.mock.MockServletLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;

public class StandaloneContainersImpl implements StandaloneContainers
{
   
   // TODO this is a hack ;-)
   public static Class<? extends MockServletLifecycle> lifecycleClass = MockEELifecycle.class;
   
   private MockServletLifecycle lifecycle;
   
   public void deploy(Iterable<Class<?>> classes, Iterable<URL> beansXml) throws DeploymentException
   {
      try
      {
         this.lifecycle = lifecycleClass.newInstance();
      }
      catch (InstantiationException e1)
      {
         throw new DeploymentException("Error instantiating lifeycle", e1);
      }
      catch (IllegalAccessException e1)
      {
         throw new DeploymentException("Error instantiating lifeycle", e1);
      }
      lifecycle.initialize();
      try
      {
         MockWebBeanDiscovery discovery = lifecycle.getWebBeanDiscovery();
         discovery.setWebBeanClasses(classes);
         if (beansXml != null)
         {
            discovery.setWebBeansXmlFiles(beansXml);
         }
         lifecycle.beginApplication();
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Error deploying beans", e);
      }
      lifecycle.beginSession();
      lifecycle.beginRequest();
   }
   
   public void deploy(Iterable<Class<?>> classes) throws DeploymentException
   {
      deploy(classes, null);
   }

   public void cleanup()
   {
      // Np-op
      
   }
   
   public void setup()
   {
      // No-op
   }

   public void undeploy()
   {
      lifecycle.endRequest();
      lifecycle.endSession();
      lifecycle.endApplication();
      CurrentManager.setRootManager(null);
      lifecycle = null;
   }
   
}
