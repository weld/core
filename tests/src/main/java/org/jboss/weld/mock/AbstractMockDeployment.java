package org.jboss.weld.mock;

import java.util.Arrays;
import java.util.List;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

public abstract class AbstractMockDeployment implements Deployment
{

   private final List<BeanDeploymentArchive> beanDeploymentArchives;
   private final ServiceRegistry services;

   public AbstractMockDeployment(BeanDeploymentArchive... beanDeploymentArchives)
   {
      this.services = new SimpleServiceRegistry();
      this.beanDeploymentArchives = Arrays.asList(beanDeploymentArchives);
   }

   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives;
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

}