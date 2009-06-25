package org.jboss.webbeans.bean;

import java.io.Serializable;

import org.jboss.webbeans.BeanManagerImpl;

public class SerializableBeanInstance<T extends RIBean<I>, I> implements Serializable
{
   
   private static final long serialVersionUID = 7341389081613003687L;
   
   private final BeanManagerImpl manager;
   private final String beanId;
   private final I instance;
   
   public SerializableBeanInstance(T bean, I instance)
   {
      this.manager = bean.getManager();
      this.beanId = bean.getId();
      this.instance = instance;
   }

   @SuppressWarnings("unchecked")
   public T getBean()
   {
      return (T) manager.getRiBeans().get(beanId);
   }
   
   public I getInstance()
   {
      return instance;
   }
   
}