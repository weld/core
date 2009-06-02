package org.jboss.webbeans.context;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.webbeans.context.api.BeanInstance;

public class BeanInstanceImpl<T> implements BeanInstance<T>
{

   private final Contextual<T> contextual;
   private final T instance; 
   private final CreationalContext<T> creationalContext;
   
   public BeanInstanceImpl(Contextual<T> contextual, T instance, CreationalContext<T> creationalContext)
   {
      this.contextual = contextual;
      this.instance = instance;
      this.creationalContext = creationalContext;
   }

   public Contextual<T> getContextual()
   {
      return contextual;
   }

   public T getInstance()
   {
      return instance;
   }

   public CreationalContext<T> getCreationalContext()
   {
      return creationalContext;
   } 

}
