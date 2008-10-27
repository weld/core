package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.model.bean.BeanModel;

public class BeanImpl<T> extends Bean<T>
{
   
   public static final String LOGGER_NAME = "bean";
   
   private BeanModel<T, ?> beanModel;
   
   private ManagerImpl manager;

   public BeanImpl(BeanModel<T, ?> model, ManagerImpl manager)
   {
      super(manager);
      this.beanModel = model;
   }

   @Override
   public T create()
   {
      return beanModel.getConstructor().invoke(manager);
   }

   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return beanModel.getBindingTypes();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
     return beanModel.getDeploymentType();
   }

   @Override
   public String getName()
   {
      return beanModel.getName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return beanModel.getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return beanModel.getApiTypes();
   }

   @Override
   public boolean isNullable()
   {
      return !beanModel.isPrimitive();
   }

   @Override
   public boolean isSerializable()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   @Override
   public String toString()
   {
      return beanModel.toString();
   }
   
   public BeanModel<T, ?> getModel()
   {
      return beanModel;
   }

}
