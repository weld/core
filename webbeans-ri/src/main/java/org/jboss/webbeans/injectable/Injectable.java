package org.jboss.webbeans.injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

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

   public Annotation[] getBindingTypes()
   {
      return annotatedItem.getAnnotations().toArray(new Annotation[0]);
   }
   
   protected Injectable() {}
   
   @Override
   public String toString()
   {
      return getType() + " with binding types " + getBindingTypes();
   }

   public T getValue(Manager manager)
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
   
   public Set<Bean<?>> getPossibleTargets(Set<Bean<?>> possibleBeans)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : possibleBeans)
      {
         if (bean.getTypes().contains(getType()))
         {
            List<Annotation> beanBindingTypes = new ArrayList<Annotation>(bean.getBindingTypes());
            for (Annotation annotation : annotatedItem.getAnnotations())
            {
               if (beanBindingTypes.contains(annotation))
               {
                  // TODO inspect annotation parameters
                  // TODO inspect deployment types
                  resolvedBeans.add(bean);
               }
            }
         }
      }
     return resolvedBeans;
   }
   
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof Injectable)
      {
         Injectable<?, ?> that = (Injectable<?, ?>) other;
         return this.getAnnotatedItem().equals(that.getAnnotatedItem());
      }
      else
      {
         return false;
      }
   }
   
}
