package org.jboss.weld.context.api.helpers;

import java.util.Collection;

import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.ContextualInstance;

public abstract class ForwardingBeanStore implements BeanStore
{
   
   protected abstract BeanStore delegate(); 
   
   public void clear()
   {
      delegate().clear();
   }
   
   public <T> ContextualInstance<T> get(String id)
   {
      return delegate().get(id);
   }
   
   public Collection<String> getContextualIds()
   {
      return delegate().getContextualIds();
   }
   
   public <T> void put(String id, ContextualInstance<T> beanInstance)
   {
      delegate().put(id, beanInstance);
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
