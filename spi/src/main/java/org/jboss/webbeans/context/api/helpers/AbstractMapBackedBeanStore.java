package org.jboss.webbeans.context.api.helpers;

import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.Contextual;

import org.jboss.webbeans.context.api.BeanInstance;
import org.jboss.webbeans.context.api.BeanStore;

public abstract class AbstractMapBackedBeanStore implements BeanStore
{
   
   public AbstractMapBackedBeanStore()
   {
      super();
   }

   public abstract Map<Contextual<? extends Object>, BeanInstance<? extends Object>> delegate();

   /**
    * Gets an instance from the store
    * 
    * @param The bean to look for
    * @return An instance, if found
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#get(BaseBean)
    */
   public <T extends Object> BeanInstance<T> get(Contextual<? extends T> bean)
   {
      @SuppressWarnings("unchecked")
      BeanInstance<T> instance = (BeanInstance<T>) delegate().get(bean);
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
    * @see org.jboss.webbeans.context.api.BeanStore#put(Contextual, Object)
    */
   public <T> void put(BeanInstance<T> beanInstance)
   {
      delegate().put(beanInstance.getContextual(), beanInstance);
   }

   @Override
   public String toString()
   {
      return "holding " + delegate().size() + " instances";
   }
   
}