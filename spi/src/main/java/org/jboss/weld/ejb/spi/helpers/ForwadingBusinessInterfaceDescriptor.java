package org.jboss.weld.ejb.spi.helpers;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;


/**
 * An implementation of {@link BusinessInterfaceDescriptor} which forwards all 
 * its method calls to another {@link BusinessInterfaceDescriptor}}. Subclasses 
 * should override one or more methods to modify the behavior of the backing 
 * {@link BusinessInterfaceDescriptor} as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwadingBusinessInterfaceDescriptor<T> implements BusinessInterfaceDescriptor<T>
{
   
   protected abstract BusinessInterfaceDescriptor<T> delegate();
   
   public Class<T> getInterface()
   {
      return delegate().getInterface();
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
