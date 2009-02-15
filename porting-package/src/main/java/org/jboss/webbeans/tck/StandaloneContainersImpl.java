package org.jboss.webbeans.tck;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import javax.inject.manager.Manager;

import org.jboss.jsr299.tck.api.DeploymentException;
import org.jboss.jsr299.tck.spi.StandaloneContainers;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.mock.MockBootstrap;
import org.jboss.webbeans.mock.MockWebBeanDiscovery;

public class StandaloneContainersImpl implements StandaloneContainers
{
   
   public Manager deploy(List<Class<? extends Annotation>> enabledDeploymentTypes, Class<?>... classes) throws DeploymentException
   {
      try
      {
         MockBootstrap bootstrap = new MockBootstrap();
         ManagerImpl manager = bootstrap.getManager();
         if (enabledDeploymentTypes != null)
         {
            manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
         }
         MockWebBeanDiscovery discovery = new MockWebBeanDiscovery();
         discovery.setWebBeanClasses(Arrays.asList(classes));
         bootstrap.setWebBeanDiscovery(discovery);
         bootstrap.boot();
         return manager;
      }
      catch (Exception e) 
      {
         throw new DeploymentException("Error deploying beans", e);
      }
   }
   
   public Manager deploy(java.lang.Class<?>... classes) throws DeploymentException
   {
      return deploy(null, classes);
   }
   
}
