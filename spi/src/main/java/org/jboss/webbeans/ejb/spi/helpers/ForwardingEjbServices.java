package org.jboss.webbeans.ejb.spi.helpers;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

/**
 * An implementation of {@link EjbServices} which forwards all its method calls
 * to another {@link EjbServices}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbServices} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbServices implements EjbServices
{
   
   public abstract EjbServices delegate();
   
   public Object resolveEjb(InjectionPoint injectionPoint)
   {
      return delegate().resolveEjb(injectionPoint);
   }
   
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return delegate().resolveEjb(ejbDescriptor);
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      return delegate().resolvePersistenceContext(injectionPoint);
   }
   
   public Object resolveResource(InjectionPoint injectionPoint)
   {
      return delegate().resolveResource(injectionPoint);
   }

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
