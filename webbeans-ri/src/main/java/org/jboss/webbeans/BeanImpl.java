package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.model.bean.BeanModel;

public abstract class BeanImpl<T> extends Bean<T>
{
   
   public static final String LOGGER_NAME = "bean";
   
   protected ManagerImpl manager;

   public BeanImpl(ManagerImpl manager)
   {
      super(manager);
      this.manager = manager;
   }
   
   protected T getInstance()
   {
      return getModel().getConstructor().invoke(manager);
   }
   
   protected void bindInterceptors()
   {
      // TODO
   }
   
   protected void bindDecorators()
   {
      // TODO
   }

   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return getModel().getBindingTypes();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
     return getModel().getDeploymentType();
   }

   @Override
   public String getName()
   {
      return getModel().getName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return getModel().getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return getModel().getApiTypes();
   }

   @Override
   public boolean isNullable()
   {
      return !getModel().isPrimitive();
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
      return getModel().toString();
   }
   
   public abstract BeanModel<T, ?> getModel();

}
