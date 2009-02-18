package org.jboss.webbeans.tck;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

import org.jboss.jsr299.tck.api.DeploymentException;
import org.jboss.jsr299.tck.spi.StandaloneContainers;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.mock.MockLifecycle;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;

public class StandaloneContainersImpl implements StandaloneContainers
{
   
   private MockLifecycle lifecycle;
   
   public void deploy(List<Class<? extends Annotation>> enabledDeploymentTypes, Iterable<Class<?>> classes) throws DeploymentException
   {
      deploy(enabledDeploymentTypes, classes, null);
   }
   
   public void deploy(List<Class<? extends Annotation>> enabledDeploymentTypes, Iterable<Class<?>> classes, Iterable<URL> beansXml) throws DeploymentException
   {
      this.lifecycle = new MockLifecycle();
      try
      {
         lifecycle.initialize();  
         ManagerImpl manager = lifecycle.getBootstrap().getManager();
         if (enabledDeploymentTypes != null)
         {
            manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
         }
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
      deploy(null, classes, null);
   }
   
   public void deploy(Iterable<Class<?>> classes, Iterable<URL> beansXml) throws DeploymentException
   {
      deploy(null, classes, beansXml);
   }

   public void cleanup()
   {
      // TODO Auto-generated method stub
      
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
