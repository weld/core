package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.model.bean.SimpleBeanModel;

public class SimpleBean<T> extends AbstractBean<T>
{
   
   private SimpleBeanModel<T> model;
   
   public SimpleBean(SimpleBeanModel<T> model, ManagerImpl manager)
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
      callInitializers(instance);
      return instance;
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

   @Override
   public SimpleBeanModel<T> getModel()
   {
      return model;
   }
   
}
