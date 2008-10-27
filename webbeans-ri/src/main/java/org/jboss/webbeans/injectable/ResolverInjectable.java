package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.TypeLiteral;

import org.jboss.webbeans.ModelManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.SimpleAnnotatedItem;

/**
 * TODO Rename this to something which implies it only ever used for resolution
 * @author Pete Muir
 *
 */
public class ResolverInjectable<T> extends Injectable<T, Object>
{
   
   private static final Annotation[] DEFAULT_BINDING_ARRAY = {new CurrentAnnotationLiteral()};
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   private boolean useDefaultBinding;
   
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
      
      if (annotatedItem.getAnnotations(BindingType.class).size() == 0)
      {
         useDefaultBinding = true;
      }
   }
   
   @Override
   public Set<Annotation> getBindingTypes()
   {
      if (useDefaultBinding)
      {
         return DEFAULT_BINDING;
      }
      else
      {
         return super.getBindingTypes();
      }
   }
   
   @Override
   public Annotation[] getBindingTypesAsArray()
   {
      if (useDefaultBinding)
      {
         return DEFAULT_BINDING_ARRAY;
      }
      else
      {
         return super.getBindingTypesAsArray();
      }
   }
   
   public ResolverInjectable(Class<T> type, Annotation[] bindingTypes, ModelManager modelManager)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, type), modelManager);
   }
   
   public ResolverInjectable(Class<T> type, Annotation[] bindingTypes, ModelManager modelManager, Type ... actualTypeArguements)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, type, actualTypeArguements), modelManager);
   }
   
   public ResolverInjectable(TypeLiteral<T> apiType, Annotation[] bindingTypes, ModelManager modelManager)
   {
      this(new SimpleAnnotatedItem<T, Object>(bindingTypes, apiType), modelManager);
   }

}
