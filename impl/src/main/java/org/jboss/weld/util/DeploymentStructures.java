package org.jboss.weld.util;

import static org.jboss.weld.logging.messages.UtilMessage.UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE;

import java.util.Map;

import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.manager.BeanManagerImpl;

public class DeploymentStructures
{
  
   private DeploymentStructures() {}
   
   public static BeanDeployment getOrCreateBeanDeployment(Deployment deployment, BeanManagerImpl deploymentManager, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Class<?> clazz)
   {
      BeanDeploymentArchive beanDeploymentArchive = deployment.loadBeanDeploymentArchive(clazz);
      if (beanDeploymentArchive == null)
      {
         throw new ForbiddenStateException(UNABLE_TO_FIND_BEAN_DEPLOYMENT_ARCHIVE, clazz);
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
