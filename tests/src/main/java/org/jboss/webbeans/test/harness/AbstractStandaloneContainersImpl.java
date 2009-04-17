package org.jboss.webbeans.test.harness;

import java.net.URL;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.webbeans.mock.MockServletLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;

public abstract class AbstractStandaloneContainersImpl implements StandaloneContainers
{
   
   private MockServletLifecycle lifecycle;
   
   public void deploy(Iterable<Class<?>> classes, Iterable<URL> beansXml) throws DeploymentException
   {
      this.lifecycle = newLifecycle();
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
   
   protected abstract MockServletLifecycle newLifecycle();

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
      lifecycle = null;
   }
   
}