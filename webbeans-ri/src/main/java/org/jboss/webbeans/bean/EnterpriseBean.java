package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.model.bean.EnterpriseBeanModel;

public class EnterpriseBean<T> extends AbstractBean<T>
{
   private EnterpriseBeanModel<T> model;
   
   public EnterpriseBean(EnterpriseBeanModel<T> model, ManagerImpl manager) {
      super(manager);
      this.model = model;
   }

   @Override
   public EnterpriseBeanModel<T> getModel()
   {
      return model;
   }

   @Override
   public T create()
   {
      T instance = model.getConstructor().invoke(manager);
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      callInitializers(instance);
      return instance;      
   }
   
   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
      super.destroy(instance);
   }

   protected void callInitializers(T instance)
   {
      for (InjectableMethod<Object> initializer : model.getInitializerMethods())
      {
         initializer.invoke(manager, instance);
      }
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

}
