package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.model.bean.ProducerMethodBeanModel;

public class ProducerBean<T> extends AbstractBean<T>
{
   
   private ProducerMethodBeanModel<T> model;

   public ProducerBean(ProducerMethodBeanModel<T> model, ManagerImpl manager)
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
      // TODO Auto-generated method stub
      return null;
   }
   

   
}
