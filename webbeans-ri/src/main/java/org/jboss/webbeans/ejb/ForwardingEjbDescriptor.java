package org.jboss.webbeans.ejb;

import java.lang.reflect.Method;

import org.jboss.webbeans.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;

public abstract class ForwardingEjbDescriptor<T> implements EjbDescriptor<T>
{
   
   protected abstract EjbDescriptor<T> delegate();
   
   public String getEjbName()
   {
      return delegate().getEjbName();
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces()
   {
      return delegate().getLocalBusinessInterfaces();
   }
   
   public Iterable<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces()
   {
      return delegate().getRemoteBusinessInterfaces();
   }
   
   public Iterable<Method> getRemoveMethods()
   {
      return delegate().getRemoveMethods();
   }
   
   public Class<T> getType()
   {
      return delegate().getType();
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
