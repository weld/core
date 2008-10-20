package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.model.AbstractComponentModel;
import org.jboss.webbeans.util.LoggerUtil;

public class BeanImpl<T> extends Bean<T>
{
   
public static final String LOGGER_NAME = "componentMetaModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private AbstractComponentModel<T, ?> componentMetaModel;

   public BeanImpl(AbstractComponentModel<T, ?> componentMetaModel, ManagerImpl manager)
   {
      super(manager);
      this.componentMetaModel = componentMetaModel;
   }

   @Override
   public T create()
   {
      return componentMetaModel.getConstructor().invoke(getManager());
   }

   @Override
   public void destroy(T instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return componentMetaModel.getBindingTypes();
   }

   @Override
   public Class<Annotation> getDeploymentType()
   {
      return null; // componentMetaModel.getDeploymentType();
   }

   @Override
   public String getName()
   {
      return componentMetaModel.getName();
   }

   @Override
   public Class<Annotation> getScopeType()
   {
      return null; //componentMetaModel.getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      // TODO Auto-generated method stub
      return null;
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

}
