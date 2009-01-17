package org.jboss.webbeans.ejb.spi;


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
   
   public String getJndiName()
   {
      return delegate().getJndiName();
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
