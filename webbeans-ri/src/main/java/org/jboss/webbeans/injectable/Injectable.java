package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
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
   
   private AnnotatedItem<T, S> annotatedItem;
   
   public Injectable(AnnotatedItem<T, S> annotatedItem)
   {
      this.annotatedItem = annotatedItem;
   }

   public Set<Annotation> getBindingTypes()
   {
      return annotatedItem.getAnnotations();
   }
   
   protected Injectable() {}
   
   @Override
   public String toString()
   {
      return getType() + " with binding types " + getBindingTypes();
   }

   public T getValue(ManagerImpl manager)
   {
      return manager.getInstanceByType(getType(), getBindingTypes());
   }
   
   public Class<? extends T> getType()
   {
      return annotatedItem.getType();
   }
   
   public AnnotatedItem<T, S> getAnnotatedItem()
   {
      return annotatedItem;
   }
   
   public Set<Bean<?>> getPossibleBeans(Set<Bean<?>> beans)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean.getTypes().contains(getType()))
         {
            List<Annotation> beanBindingTypes = new ArrayList<Annotation>(bean.getBindingTypes());
            if (beanBindingTypes.containsAll(annotatedItem.getAnnotations()))
            {
               // TODO inspect annotation parameters
               // TODO inspect deployment types
               resolvedBeans.add(bean);
            }
         }
      }
     return resolvedBeans;
   }
   
   @Override
   public boolean equals(Object other)
   {
      // TODO Currently you must have any annotation literals on other for this to work, probably need to iterate over the set and check both directions
      if (other instanceof Injectable)
      {
         Injectable<?, ?> that = (Injectable<?, ?>) other;
         return this.getAnnotatedItem().getType().isAssignableFrom(that.getAnnotatedItem().getType()) &&
            that.getAnnotatedItem().getAnnotations(BindingType.class).equals(this.getAnnotatedItem().getAnnotations(BindingType.class));
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return 0;
   }
   
}
