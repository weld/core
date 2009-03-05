package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.manager.api.WebBeansManager;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;

public abstract class ForwardingBootstrap implements Bootstrap
{
   
   protected abstract Bootstrap delegate();
   
   public void boot()
   {
      delegate().boot();
   }
   
   public WebBeansManager getManager()
   {
      return delegate().getManager();
   }
   
   public void initialize()
   {
      delegate().initialize();
   }
   
   public void setApplicationContext(BeanStore beanStore)
   {
      delegate().setApplicationContext(beanStore);
   }
   
   public void setEjbDiscovery(EjbDiscovery ejbDiscovery)
   {
      delegate().setEjbDiscovery(ejbDiscovery);
   }
   
   public void setEjbResolver(EjbResolver ejbResolver)
   {
      delegate().setEjbResolver(ejbResolver);
   }
   
   public void setNamingContext(NamingContext namingContext)
   {
      delegate().setNamingContext(namingContext);
   }
   
   public void setResourceLoader(ResourceLoader resourceLoader)
   {
      delegate().setResourceLoader(resourceLoader);
   }
   
   public void setWebBeanDiscovery(WebBeanDiscovery webBeanDiscovery)
   {
      delegate().setWebBeanDiscovery(webBeanDiscovery);
   }
   
   public void shutdown()
   {
      delegate().shutdown();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
}
