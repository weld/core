package org.jboss.webbeans.context.api.helpers;

import javax.context.Contextual;

import org.jboss.webbeans.context.api.BeanStore;

public abstract class ForwardingBeanStore implements BeanStore
{
   
   protected abstract BeanStore delegate(); 
   
   public void clear()
   {
      delegate().clear();
   }
   
   public <T> T get(Contextual<? extends T> bean)
   {
      return delegate().get(bean);
   }
   
   public Iterable<Contextual<? extends Object>> getBeans()
   {
      return delegate().getBeans();
   }
   
   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      delegate().put(bean, instance);
   }
   
   public <T> T remove(Contextual<? extends T> bean)
   {
      return delegate().remove(bean);
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
