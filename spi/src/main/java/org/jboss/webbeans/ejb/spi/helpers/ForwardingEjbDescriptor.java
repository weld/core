package org.jboss.webbeans.ejb.spi.helpers;

import java.lang.reflect.Method;
import java.util.Collection;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;


/**
 * An implementation of {@link EjbDescriptor} which forwards all its method calls
 * to another {@link EjbDescriptor}}. Subclasses should override one or more 
 * methods to modify the behavior of the backing {@link EjbDescriptor} as desired
 * per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 * 
 * @author Pete Muir
 *
 */
public abstract class ForwardingEjbDescriptor<T> implements EjbDescriptor<T>
{
   
   protected abstract EjbDescriptor<T> delegate();
      
   public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return delegate().getLocalBusinessInterfaces();
   }
   
   public Collection<Method> getRemoveMethods()
   {
      return delegate().getRemoveMethods();
   }
   
   public Class<T> getBeanClass()
   {
      return delegate().getBeanClass();
   }
   
   public String getEjbName()
   {
      return delegate().getEjbName();
   }
   
   public boolean isMessageDriven()
   {
      return delegate().isMessageDriven();
   }
   
   public boolean isSingleton()
   {
      return delegate().isSingleton();
   }
   
   public boolean isStateful()
   {
      return delegate().isStateful();
   }
   
   public boolean isStateless()
   {
      return delegate().isStateless();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return this == obj || delegate().equals(obj);
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
