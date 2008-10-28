package org.jboss.webbeans;

import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.model.bean.SimpleBeanModel;

public class SimpleBeanImpl<T> extends BeanImpl<T>
{
   
   private SimpleBeanModel<T> model;
   
   public SimpleBeanImpl(SimpleBeanModel<T> model, ManagerImpl manager)
   {
      super(manager);
      this.model = model;
   }

   @Override
   public T create()
   {
      T instance = getInstance();
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      return instance;
   }
   
   protected void injectEjbAndCommonFields()
   {
      // TODO
   }
   
   protected void injectBoundFields(T instance)
   {
      for (InjectableField<?> injectableField : getModel().getInjectableFields())
      {
         injectableField.inject(instance, manager);
      }
   }

   @Override
   public SimpleBeanModel<T> getModel()
   {
      return model;
   }
   
}
