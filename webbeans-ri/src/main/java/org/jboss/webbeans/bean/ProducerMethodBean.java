package org.jboss.webbeans.bean;

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
      return model.getConstructor().invoke(manager, manager.getInstance(model.getDeclaringBean()));
   }
   

   
}
