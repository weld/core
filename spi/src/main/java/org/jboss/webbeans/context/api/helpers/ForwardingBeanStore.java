package org.jboss.webbeans.context.api.helpers;

import java.util.Collection;

import javax.enterprise.context.spi.Contextual;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.ContexutalInstance;

public abstract class ForwardingBeanStore implements BeanStore
{
   
   protected abstract BeanStore delegate(); 
   
   public void clear()
   {
      delegate().clear();
   }
   
   public <T> ContexutalInstance<T> get(Contextual<? extends T> bean)
   {
      return delegate().get(bean);
   }
   
   public Collection<Contextual<? extends Object>> getContextuals()
   {
      return delegate().getContextuals();
   }
   
   public <T> void put(ContexutalInstance<T> beanInstance)
   {
      delegate().put(beanInstance);
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
      return this == obj || delegate().equals(obj);
   }
   
}
