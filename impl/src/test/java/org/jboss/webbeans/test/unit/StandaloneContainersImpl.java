package org.jboss.webbeans.test.unit;

import java.net.URL;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.mock.MockLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;

public class StandaloneContainersImpl implements StandaloneContainers
{
   
   private MockLifecycle lifecycle;
   
   public void deploy(Iterable<Class<?>> classes, Iterable<URL> beansXml) throws DeploymentException
   {
      this.lifecycle = new MockLifecycle();
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
