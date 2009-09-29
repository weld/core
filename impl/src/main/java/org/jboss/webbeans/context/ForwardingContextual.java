package org.jboss.webbeans.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public abstract class ForwardingContextual<T> implements Contextual<T>
{
   
   protected abstract Contextual<T> delegate();
   
   public T create(CreationalContext<T> creationalContext)
   {
      return delegate().create(creationalContext);
   }
   
   public void destroy(T instance, CreationalContext<T> creationalContext) 
   {
      delegate().destroy(instance, creationalContext); 
   }
   
   
   @Override
   public boolean equals(Object obj) 
   {
      return this == obj || delegate().equals(obj);
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }

}
