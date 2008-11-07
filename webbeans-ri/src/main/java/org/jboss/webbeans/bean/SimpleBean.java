package org.jboss.webbeans.bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.injectable.InjectableField;
import org.jboss.webbeans.injectable.InjectableMethod;
import org.jboss.webbeans.introspector.AnnotatedMethod;
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
      T instance = model.getConstructor().invoke(manager);
      bindDecorators();
      bindInterceptors();
      injectEjbAndCommonFields();
      injectBoundFields(instance);
      callInitializers(instance);
      callPostConstruct(instance);
      return instance;
   }
   
   @Override
   public void destroy(T instance)
   {
      callPreDestroy(instance);
   }
   
   protected void callPreDestroy(T instance)
   {
      AnnotatedMethod<Object> preDestroy = getModel().getPreDestroy();
      if (preDestroy!=null)
      {
         try
         {
            preDestroy.getAnnotatedMethod().invoke(instance);
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Unable to invoke " + preDestroy + " on " + instance, e);
         }
     }
   }

   protected void callPostConstruct(T instance)
   {
      AnnotatedMethod<Object> postConstruct = getModel().getPostConstruct();
      if (postConstruct!=null)
      {
         try
         {
            postConstruct.getAnnotatedMethod().invoke(instance);
         }
         catch (Exception e) 
         {
            throw new RuntimeException("Unable to invoke " + postConstruct + " on " + instance, e);
         }
      }
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
