package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.model.AbstractComponentModel;
import org.jboss.webbeans.model.ScopeModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.util.MapWrapper;

public class ModelManager
{
   
   @SuppressWarnings("unchecked")
   private class ScopeModelMap extends MapWrapper<Class<? extends Annotation>, ScopeModel<? extends Annotation>>
   {

      public ScopeModelMap()
      {
         super(new HashMap<Class<? extends Annotation>, ScopeModel<? extends Annotation>>());
      }
      
      public <T extends Annotation> ScopeModel<T> get(Class<T> key)
      {
         return (ScopeModel<T>) super.get(key);
      }
   }
   
   private Map<Class<? extends Annotation>, StereotypeModel<?>> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeModel<?>>();
   
   private Map<Class<?>, AbstractComponentModel<?, ?>> componentModels = new HashMap<Class<?>, AbstractComponentModel<?,?>>();
   
   private ScopeModelMap scopes = new ScopeModelMap(); 
   

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
   
   public AbstractComponentModel<?, ?> getComponentModel(Class<?> clazz)
   {
      return componentModels.get(clazz);
   }
   
   public <T extends Annotation> ScopeModel<T> getScopeModel(Class<T> scopeType)
   {
      if (scopes.containsKey(scopeType))
      {
         return scopes.get(scopeType);
      }
      else
      {
         ScopeModel<T> scopeModel = new ScopeModel<T>(scopeType);
         scopes.put(scopeType, scopeModel);
         return scopeModel;
      }
   }

}
