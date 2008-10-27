package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.jboss.webbeans.model.AnnotationModel;
import org.jboss.webbeans.model.ScopeModel;
import org.jboss.webbeans.model.StereotypeModel;
import org.jboss.webbeans.model.bean.BeanModel;
import org.jboss.webbeans.util.MapWrapper;

public class ModelManager
{
   
   @SuppressWarnings("unchecked")
   private abstract class AnnotationModelMap<T extends AnnotationModel<?>> extends MapWrapper<Class<? extends Annotation>, T>
   {

      public AnnotationModelMap()
      {
         super(new HashMap<Class<? extends Annotation>, T>());
      }
      
      public <S extends Annotation> T get(Class<S> key)
      {
         return (T) super.get(key);
      }
   }
   
   @SuppressWarnings("unchecked")
   private class ScopeModelMap extends AnnotationModelMap<ScopeModel<?>>
   {
      
      @Override
      public <S extends Annotation> ScopeModel<S> get(Class<S> key)
      {
         return (ScopeModel<S>) super.get(key);
      }
      
   }
   
   private Map<Class<? extends Annotation>, StereotypeModel<?>> stereotypes = new HashMap<Class<? extends Annotation>, StereotypeModel<?>>();
   
   private Map<Class<?>, BeanModel<?, ?>> beanModels = new HashMap<Class<?>, BeanModel<?,?>>();
   
   private ScopeModelMap scopes = new ScopeModelMap();
   

   public void addStereotype(StereotypeModel<?> stereotype)
   {
      stereotypes.put(stereotype.getType(), stereotype);
   }
   
   public StereotypeModel<?> getStereotype(Class<? extends Annotation> annotationType)
   {
      return stereotypes.get(annotationType);
   }
   
   public void addBeanModel(BeanModel<?, ?> beanModel)
   {
      beanModels.put(beanModel.getType(), beanModel);
   }
   
   public BeanModel<?, ?> getBeanModel(Class<?> clazz)
   {
      return beanModels.get(clazz);
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
