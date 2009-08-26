package org.jboss.webbeans.bootstrap.api.test;

import java.util.Set;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.manager.api.WebBeansManager;

public class MockBootstrap implements Bootstrap
{
   
   public WebBeansManager getManager(BeanDeploymentArchive beanDeploymentArchive)
   {
      return null;
   }
   
   public void shutdown()
   {
   }

   public Bootstrap deployBeans()
   {
      return this;
   }

   public Bootstrap endInitialization()
   {
      return this;
   }

   public Bootstrap startInitialization()
   {
      return this;
   }

   public Bootstrap validateBeans()
   {
      return this;
   }
   
   protected static void verifyServices(ServiceRegistry services, Set<Class<? extends Service>> requiredServices) 
   {
      for (Class<? extends Service> serviceType : requiredServices)
      {
         if (!services.contains(serviceType))
         {
            throw new IllegalStateException("Required service " + serviceType.getName() + " has not been specified");
         }
      }
   }

   public Bootstrap startContainer(Environment environment, Deployment deployment, BeanStore beanStore)
   {
      verifyServices(deployment.getServices(), environment.getRequiredDeploymentServices());
      verifyServices(deployment.getBeanDeploymentArchives().iterator().next().getServices(), environment.getRequiredBeanDeploymentArchiveServices());
      return this;
   }

   
}
