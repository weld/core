package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bindings.CurrentAnnotationLiteral;
import org.jboss.webbeans.introspector.AnnotatedItem;

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
   
   public Injectable(AnnotatedItem<T, S> annotatedItem)
   {
      this.annotatedItem = annotatedItem;
   }

   public Annotation[] getBindingTypesAsArray()
   {
      Annotation[] annotations = annotatedItem.getAnnotationsAsArray(BindingType.class);
      // TODO This is in the wrong place, where to put it... Probably best to wrap annotated item...
      if (annotations.length ==0)
      {
         return DEFAULT_BINDING_ARRAY; 
      }
      else
      {
         return annotations;
      }
   }
   
   public Set<Annotation> getBindingTypes()
   {
      Set<Annotation> annotations = annotatedItem.getAnnotations(BindingType.class);
      if (annotations.size() == 0)
      {
         return DEFAULT_BINDING;
      }
      else
      {
         return annotations;
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
   
   public Set<Bean<?>> getMatchingBeans(Set<Bean<?>> beans)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (getAnnotatedItem().isAssignableFrom(bean.getTypes()) && bean.getBindingTypes().containsAll(getBindingTypes()))
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
   
   
   
}
