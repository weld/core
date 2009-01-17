package org.jboss.webbeans.bootstrap.spi;

import org.jboss.webbeans.ejb.spi.EjbDescriptor;

/**
 * An implementation of {@link EjbDiscovery} which forwards all its method calls
 * to another {@link EjbDiscovery}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbDiscovery} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbDiscovery implements EjbDiscovery
{
   
   protected abstract EjbDiscovery delegate();
   
   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return delegate().discoverEjbs();
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
