package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.transaction.spi.TransactionServices;

public abstract class AbstractBootstrap implements Bootstrap
{
   
   private WebBeanDiscovery webBeanDiscovery;
   private ResourceLoader resourceLoader;
   private NamingContext namingContext;
   private EjbResolver ejbResolver;
   private EjbDiscovery ejbDiscovery;
   private BeanStore applicationContext;
   private TransactionServices transactionServices;

   public void setEjbDiscovery(EjbDiscovery ejbDiscovery)
   {
      this.ejbDiscovery = ejbDiscovery;
   }

   public void setEjbResolver(EjbResolver ejbResolver)
   {
      this.ejbResolver = ejbResolver;
   }

   public void setNamingContext(NamingContext namingContext)
   {
      this.namingContext = namingContext;
   }

   public void setResourceLoader(ResourceLoader resourceLoader)
   {
      this.resourceLoader = resourceLoader;
   }

   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
   }
   
   public void setTransactionServices(TransactionServices transactionServices)
   {
      this.transactionServices = transactionServices;
   }

   public WebBeanDiscovery getWebBeanDiscovery()
   {
      return webBeanDiscovery;
   }

   public ResourceLoader getResourceLoader()
   {
      return resourceLoader;
   }

   public NamingContext getNamingContext()
   {
      return namingContext;
   }

   public EjbResolver getEjbResolver()
   {
      return ejbResolver;
   }

   public EjbDiscovery getEjbDiscovery()
   {
      return ejbDiscovery;
   }
   
   public TransactionServices getTransactionServices()
   {
      return transactionServices;
   }
   
   public BeanStore getApplicationContext()
   {
      return applicationContext;
   }
   
   public void setApplicationContext(BeanStore applicationContext)
   {
      this.applicationContext = applicationContext;      
   }
   
}