package org.jboss.webbeans.bean;

import javax.webbeans.Dependent;
import javax.webbeans.IllegalProductException;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.model.bean.ProducerMethodBeanModel;

public class ProducerMethodBean<T> extends AbstractBean<T>
{
   
   private ProducerMethodBeanModel<T> model;

   public ProducerMethodBean(ProducerMethodBeanModel<T> model, ManagerImpl manager)
   {
      super(manager);
      this.model = model;
   }

   @Override
   public ProducerMethodBeanModel<T> getModel()
   {
      return model;
   }

   @Override
   public T create()
   {
      T instance = model.getConstructor().invoke(manager, manager.getInstance(model.getDeclaringBean()));
      if (instance == null && !model.getScopeType().equals(Dependent.class))
      {
         throw new IllegalProductException("Cannot return null from a non-dependent method");
      }
      return instance;
   }
   

   
}
