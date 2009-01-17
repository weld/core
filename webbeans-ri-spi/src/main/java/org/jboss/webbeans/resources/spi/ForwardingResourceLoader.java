package org.jboss.webbeans.resources.spi;

import java.net.URL;

/**
 * An implementation of {@link ResourceLoader} which forwards all its method calls
 * to another {@link ResourceLoader}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link ResourceLoader} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingResourceLoader implements ResourceLoader
{
   
   protected abstract ResourceLoader delegate();
   
   public Class<?> classForName(String name)
   {
      return delegate().classForName(name);
   }
   
   public URL getResource(String name)
   {
      return delegate().getResource(name);
   }
   
   public Iterable<URL> getResources(String name)
   {
      return delegate().getResources(name);
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
