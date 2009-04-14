package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.manager.api.WebBeansManager;
import org.jboss.webbeans.messaging.spi.JmsServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.ws.spi.WebServices;

/**
 * A bean version of bootstrap that delegates to the underlying bootstrap impl
 * 
 * @author Pete Muir
 *
 */
public class BootstrapBean implements Bootstrap
{
   
   private final Bootstrap bootstrap;

   public BootstrapBean(Bootstrap bootstrap)
   {
      this.bootstrap = bootstrap;
   }
   
   public void setEjbServices(EjbServices ejbServices)
   {
      bootstrap.getServices().add(EjbServices.class, ejbServices);
   }
   
   public EjbServices getEjbServices()
   {
      return bootstrap.getServices().get(EjbServices.class);
   }
   
   public void setJpaServices(JpaServices jpaServices)
   {
      bootstrap.getServices().add(JpaServices.class, jpaServices);
   }
   
   public JpaServices getJpaServices()
   {
      return bootstrap.getServices().get(JpaServices.class);
   }
   
   public ResourceServices getResourceServices()
   {
      return bootstrap.getServices().get(ResourceServices.class);
   }
   
   public void setResourceServices(ResourceServices resourceServices)
   {
      bootstrap.getServices().add(ResourceServices.class, resourceServices);
   }

   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      bootstrap.getServices().add(WebBeanDiscovery.class, webBeanDiscovery);
   }

   public WebBeanDiscovery getWebBeanDiscovery()
   {
      return bootstrap.getServices().get(WebBeanDiscovery.class);
   }

   public void setTransactionServices(TransactionServices transactionServices)
   {
      bootstrap.getServices().add(TransactionServices.class, transactionServices);
   }

   public TransactionServices getTransactionServices()
   {
      return bootstrap.getServices().get(TransactionServices.class);
   }
   
   public void setApplicationContext(BeanStore applicationContext)
   {
      bootstrap.setApplicationContext(applicationContext);      
   }
   
   public void setResourceLoader(ResourceLoader resourceLoader)
   {
      bootstrap.getServices().add(ResourceLoader.class, resourceLoader);
   }
   
   public ResourceLoader getResourceLoader()
   {
      return bootstrap.getServices().get(ResourceLoader.class);
   }
   
   public WebServices getWebServices()
   {
      return bootstrap.getServices().get(WebServices.class);
   }
   
   public void setWebServices(WebServices webServices)
   {
      bootstrap.getServices().add(WebServices.class, webServices);
   }
   
   public JmsServices getJmsServices()
   {
      return bootstrap.getServices().get(JmsServices.class);
   }
   
   public void setJmsServices(JmsServices jmsServices)
   {
      bootstrap.getServices().add(JmsServices.class, jmsServices);
   }
   
   public void boot()
   {
      bootstrap.boot();
   }
   
   public WebBeansManager getManager()
   {
      return bootstrap.getManager();
   }
   
   public ServiceRegistry getServices()
   {
      return bootstrap.getServices();
   }
   
   public void initialize()
   {
      bootstrap.initialize();
   }
   
   public void setEnvironment(Environment environment)
   {
      bootstrap.setEnvironment(environment);
   }
   
   public void shutdown()
   {
      bootstrap.shutdown();
   }
   
}
