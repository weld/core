package org.jboss.webbeans.bootstrap.spi;

import java.net.URL;

/**
 * An implementation of {@link WebBeanDiscovery} which forwards all its method 
 * calls to another {@link WebBeanDiscovery}}. Subclasses should override one or 
 * more methods to modify the behavior of the backing {@link WebBeanDiscovery} 
 * as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingWebBeanDiscovery implements WebBeanDiscovery
{
   
   protected abstract WebBeanDiscovery delegate();
   
   public Iterable<Class<?>> discoverWebBeanClasses()
   {
      return delegate().discoverWebBeanClasses();
   }
   
   public Iterable<URL> discoverWebBeansXml()
   {
      return delegate().discoverWebBeansXml();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
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
   
}
