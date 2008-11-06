package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.model.bean.BeanModel;

public class ProducerBean<T> extends AbstractBean<T>
{

   public ProducerBean(ManagerImpl manager)
   {
      super(manager);
      // TODO Auto-generated constructor stub
   }

   @Override
   public BeanModel<T, ?> getModel()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public T create()
   {
      // TODO Auto-generated method stub
      return null;
   }
   

   
}
