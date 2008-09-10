package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.logging.Logger;

import javax.webbeans.ComponentInstance;
import javax.webbeans.Container;

import org.jboss.webbeans.model.AbstractComponentModel;
import org.jboss.webbeans.util.LoggerUtil;

public class ComponentInstanceImpl<T> extends ComponentInstance<T>
{
   
public static final String LOGGER_NAME = "componentMetaModel";
   
   private static Logger log = LoggerUtil.getLogger(LOGGER_NAME);
   
   private AbstractComponentModel<T, ?> componentMetaModel;

   public ComponentInstanceImpl(AbstractComponentModel<T, ?> componentMetaModel)
   {
      this.componentMetaModel = componentMetaModel;
   }

   @Override
   public T create(Container container)
   {
      return componentMetaModel.getConstructor().invoke(container);
   }

   @Override
   public void destroy(Container container, T instance)
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return componentMetaModel.getBindingTypes();
   }

   @Override
   public Annotation getDeploymentType()
   {
      return componentMetaModel.getDeploymentType();
   }

   @Override
   public String getName()
   {
      return componentMetaModel.getName();
   }

   @Override
   public Annotation getScopeType()
   {
      return componentMetaModel.getScopeType();
   }

   @Override
   public Set<Class> getTypes()
   {
      // TODO Auto-generated method stub
      return null;
   }

}
