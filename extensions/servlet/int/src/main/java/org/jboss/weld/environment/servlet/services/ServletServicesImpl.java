package org.jboss.weld.environment.servlet.services;

import javax.servlet.ServletContext;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.servlet.api.ServletServices;

public class ServletServicesImpl implements ServletServices
{
   
   private final BeanDeploymentArchive beanDeploymentArchive;

   public ServletServicesImpl(BeanDeploymentArchive beanDeploymentArchive)
   {
      this.beanDeploymentArchive = beanDeploymentArchive;
   }

   public BeanDeploymentArchive getBeanDeploymentArchive(ServletContext ctx)
   {
      return beanDeploymentArchive;
   }

   public void cleanup() {}

}
