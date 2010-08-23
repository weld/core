package org.jboss.weld.environment.servlet.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import javax.enterprise.inject.spi.Extension;
import javax.servlet.ServletContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

public class ServletDeployment implements Deployment
{
   
   private final WebAppBeanDeploymentArchive webAppBeanDeploymentArchive;
   private final Collection<BeanDeploymentArchive> beanDeploymentArchives;
   private final ServiceRegistry services;
   private final Iterable<Extension> extensions;

   public ServletDeployment(ServletContext servletContext, Bootstrap bootstrap)
   {
      this.webAppBeanDeploymentArchive = new WebAppBeanDeploymentArchive(servletContext, bootstrap);
      this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
      this.beanDeploymentArchives.add(webAppBeanDeploymentArchive);
      this.services = new SimpleServiceRegistry();
      this.extensions = ServiceLoader.load(Extension.class);
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives;
   }

   public ServiceRegistry getServices()
   {
      return services;
   }

   public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
   {
      return webAppBeanDeploymentArchive;
   }
   
   public WebAppBeanDeploymentArchive getWebAppBeanDeploymentArchive()
   {
      return webAppBeanDeploymentArchive;
   }
   
   public Iterable<Extension> getExtensions()
   {
      return extensions;
   }

}
