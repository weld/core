package org.jboss.weld.util;

import java.util.Map;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

public class DeploymentStructures
{
  
   private DeploymentStructures() {}
   
   public static BeanDeployment getOrCreateBeanDeployment(Deployment deployment, BeanManagerImpl deploymentManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Class<?> clazz)
   {
      BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
      if (beanDeploymentArchive == null)
      {
         throw new IllegalStateException("Unable to find Bean Deployment Archive for " + clazz);
      }
      else
      {
         if (beanDeployments.containsKey(beanDeploymentArchive))
         {
            return beanDeployments.get(beanDeploymentArchive);
         }
         else
         {
            BeanDeployment beanDeployment = new BeanDeployment(beanDeploymentArchive, deploymentManager, deployment.getServices());
            beanDeployments.put(beanDeploymentArchive, beanDeployment);
            return beanDeployment;
         }
      }
   }

}
