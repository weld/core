package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.model.AbstractComponentModel;
import org.jboss.webbeans.model.StereotypeModel;

public class ModelManager
{
   
   // TODO Store these in the application context (when it exists)
   private static Map<Class<? extends Annotation>, StereotypeModel<?>> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeModel<?>>();
   private static Map<Class<?>, AbstractComponentModel<?, ?>> componentModels = new HashMap<Class<?>, AbstractComponentModel<?,?>>();
   

   public void addStereotype(StereotypeModel<?> stereotype)
   {
      stereotypes.put(stereotype.getStereotypeClass(), stereotype);
   }
   
   public StereotypeModel<?> getStereotype(Class<? extends Annotation> annotationType)
   {
      return stereotypes.get(annotationType);
   }
   
   public void addComponentModel(AbstractComponentModel<?, ?> componentModel)
   {
      componentModels.put(componentModel.getType(), componentModel);
   }
   
   public <T> AbstractComponentModel<T, ?> getComponentModel(Class<T> clazz)
   {
      return (AbstractComponentModel<T, ?>) componentModels.get(clazz);
   }

}
