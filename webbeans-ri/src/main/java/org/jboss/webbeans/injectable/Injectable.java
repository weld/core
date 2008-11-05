package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.ModelManager;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.model.BindingTypeModel;

/**
 * Abstraction of java reflection for Web Beans, represent's something that can
 * be injected
 * 
 * @author Pete Muir
 *
 */
public abstract class Injectable<T, S>
{
   
   private static final Annotation[] DEFAULT_BINDING_ARRAY = {new CurrentAnnotationLiteral()};
   private static final Set<Annotation> DEFAULT_BINDING = new HashSet<Annotation>(Arrays.asList(DEFAULT_BINDING_ARRAY));
   
   private AnnotatedItem<T, S> annotatedItem;
   
   private boolean useDefaultBinding;
   
   public Injectable(AnnotatedItem<T, S> annotatedItem)
   {
      this.annotatedItem = annotatedItem;
      if (annotatedItem.getAnnotations(BindingType.class).size() == 0)
      {
         useDefaultBinding = true;
      }
   }
   
   public Set<Annotation> getBindingTypes()
   {
      if (useDefaultBinding)
      {
         return DEFAULT_BINDING;
      }
      else
      {
         return annotatedItem.getAnnotations(BindingType.class);
      }
   }
   
   public Annotation[] getBindingTypesAsArray()
   {
      if (useDefaultBinding)
      {
         return DEFAULT_BINDING_ARRAY;
      }
      else
      {
         return annotatedItem.getAnnotationsAsArray(BindingType.class);
      }
   }
   
   protected Injectable() {}
   
   @Override
   public String toString()
   {
      return getClass().getSimpleName() + "[" + getAnnotatedItem().toString() + "]";
   }

   public T getValue(ManagerImpl manager)
   {
      return manager.getInstanceByType(getType(), getBindingTypesAsArray());
   }
   
   public Class<? extends T> getType()
   {
      return annotatedItem.getType();
   }
   
   public AnnotatedItem<T, S> getAnnotatedItem()
   {
      return annotatedItem;
   }
   
   public Set<Bean<?>> getMatchingBeans(List<Bean<?>> beans, ModelManager modelManager)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (getAnnotatedItem().isAssignableFrom(bean.getTypes()) && containsAllBindingBindingTypes(bean.getBindingTypes(), modelManager))
         {
            resolvedBeans.add(bean);
         }
      }
      return resolvedBeans;
   }
   
   @Override
   public boolean equals(Object other)
   {
      // TODO Do we need to check the other direction too?
      if (other instanceof Injectable)
      {
         Injectable<?, ?> that = (Injectable<?, ?>) other;
         return this.getAnnotatedItem().isAssignableFrom(that.getAnnotatedItem()) &&
            that.getBindingTypes().equals(this.getBindingTypes());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      // TODO Implement this!
      return 0;
   }
   
   private boolean containsAllBindingBindingTypes(Set<Annotation> bindingTypes, ModelManager modelManager)
   {
      for (Annotation bindingType : getBindingTypes())
      {
         BindingTypeModel<?> bindingTypeModel = modelManager.getBindingTypeModel(bindingType.annotationType());
         if (bindingTypeModel.getNonBindingTypes().size() > 0)
         {
            boolean matchFound = false;
            for (Annotation otherBindingType : bindingTypes)
            {
               if (bindingTypeModel.isEqual(bindingType, otherBindingType))
               {
                  matchFound = true;
               }
            }
            if (!matchFound)
            {
               return false;
            }
         }
         else if (!bindingTypes.contains(bindingType))
         {
            return false;
         }
      }
      return true;
   }
   
}
