package org.jboss.webbeans.introspector.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.webbeans.BindingType;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.ModelManager;

/**
 * TODO Rename this to something which implies it only ever used for resolution
 * @author Pete Muir
 *
 */
public class ResolverInjectable<T> extends Injectable<T, Object>
{
   
   public ResolverInjectable(SimpleAnnotatedItem<T, Object> annotatedItem, ModelManager modelManager)
   {
      super(annotatedItem);
      for (Annotation annotation : annotatedItem.getAnnotations())
      {
         if (!modelManager.getBindingTypeModel(annotation.annotationType()).isValid())
         {
            throw new IllegalArgumentException("Not a binding type " + annotation);
         }
      }
      if (annotatedItem.getActualAnnotations().length > annotatedItem.getAnnotations(BindingType.class).size())
      {
         throw new DuplicateBindingTypeException(getAnnotatedItem().toString());
      }
      
      
   }
   
   public ResolverInjectable(Class<T> type, Annotation[] bindingTypes, ModelManager modelManager)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, type), modelManager);
   }
   
   public ResolverInjectable(Class<T> type, Annotation[] bindingTypes, ModelManager modelManager, Type ... actualTypeArguments)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, type, actualTypeArguments), modelManager);
   }
   
   public ResolverInjectable(TypeLiteral<T> apiType, Annotation[] bindingTypes, ModelManager modelManager)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, apiType), modelManager);
   }

}
