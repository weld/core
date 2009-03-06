package org.jboss.webbeans.context.api.helpers;

import java.util.Map;
import java.util.Set;

import javax.context.Contextual;
import javax.inject.manager.Bean;

import org.jboss.webbeans.context.api.BeanStore;

public abstract class AbstractMapBackedBeanStore implements BeanStore
{
   
   public AbstractMapBackedBeanStore()
   {
      super();
   }

   public abstract Map<Contextual<? extends Object>, Object> delegate();

   /**
    * Gets an instance from the store
    * 
    * @param The bean to look for
    * @return An instance, if found
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#get(Bean)
    */
   public <T extends Object> T get(Contextual<? extends T> bean)
   {
      @SuppressWarnings("unchecked")
      T instance = (T) delegate().get(bean);
      return instance;
   }

   /**
    * Removed a instance from the store
    * 
    * @param bean the bean to remove
    * @return The instance removed
    *
    * @see org.jboss.webbeans.context.api.BeanStore#remove(Bean)
    */
   public <T extends Object> T remove(Contextual<? extends T> bean)
   {
      @SuppressWarnings("unchecked")
      T instance = (T) delegate().remove(bean);
      return instance;
   }

   /**
    * Clears the store
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#clear()
    */
   public void clear()
   {
      delegate().clear();
   }

   /**
    * Returns the beans contained in the store
    * 
    * @return The beans present
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#getBeans()
    */
   public Set<Contextual<? extends Object>> getBeans()
   {
      return delegate().keySet();
   }

   /**
    * Puts a bean instance under the bean key in the store
    * 
    * @param bean The bean
    * @param instance the instance
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#put(Bean, Object)
    */
   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      delegate().put(bean, instance);
   }

   @Override
   public String toString()
   {
      return "holding " + delegate().size() + " instances";
   }
   
}