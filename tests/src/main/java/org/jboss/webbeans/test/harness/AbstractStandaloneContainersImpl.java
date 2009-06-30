package org.jboss.webbeans.test.harness;

import java.net.URL;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.webbeans.mock.MockBeanDeploymentArchive;
import org.jboss.webbeans.mock.MockServletLifecycle;

public abstract class AbstractStandaloneContainersImpl implements StandaloneContainers
{

   private DeploymentException deploymentException;

   private MockServletLifecycle lifecycle;

   public boolean deploy(Iterable<Class<?>> classes, Iterable<URL> beansXml)
   {
      this.lifecycle = newLifecycle();
      lifecycle.initialize();
      try
      {
         MockBeanDeploymentArchive archive = lifecycle.getDeployment().getEjbModule();
         archive.setBeanClasses(classes);
         if (beansXml != null)
         {
            archive.setWebBeansXmlFiles(beansXml);
         }
         lifecycle.beginApplication();
      }
      catch (Exception e) 
      {
         this.deploymentException = new DeploymentException("Error deploying beans", e);
         return false;
      }
      lifecycle.beginSession();
      lifecycle.beginRequest();
      return true;
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

   public DeploymentException getDeploymentException()
   {
      return deploymentException;
   }

   public void undeploy()
   {
      lifecycle.endRequest();
      lifecycle.endSession();
      lifecycle.endApplication();
      lifecycle = null;
      deploymentException = null;
   }

}