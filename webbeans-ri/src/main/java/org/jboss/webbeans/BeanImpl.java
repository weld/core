package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.model.AbstractComponentModel;

public class BeanImpl<T> extends Bean<T>
{
   
public static final String LOGGER_NAME = "componentMetaModel";
   
   private AbstractComponentModel<T, ?> model;
   
   private ManagerImpl manager;

   public BeanImpl(AbstractComponentModel<T, ?> model, ManagerImpl manager)
   {
      super(manager);
      this.model = model;
   }

   @Override
   public T create()
   {
      return model.getConstructor().invoke(manager);
   }

   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return model.getBindingTypes();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
     return model.getDeploymentType();
   }

   @Override
   public String getName()
   {
      return model.getName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return model.getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return model.getApiTypes();
   }

   @Override
   public boolean isNullable()
   {
      // TODO Auto-generated method stub
      return false;
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
      return model.toString();
   }
   
   public AbstractComponentModel<T, ?> getModel()
   {
      return model;
   }

}
