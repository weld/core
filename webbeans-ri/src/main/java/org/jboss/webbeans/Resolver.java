package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.webbeans.NullableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;
import org.jboss.webbeans.model.BindingTypeModel;
import org.jboss.webbeans.util.ListComparator;

import com.google.common.collect.ForwardingMap;

/**
 * Implementation of Web Beans type safe and name based bean resolution
 * @author Pete Muir
 *
 */
public class Resolver
{

   /**
    * Extension of an element which bases equality not only on type, but also
    * on binding type
    *
    */
   private abstract class ResolvableAnnotatedItem<T, S> extends ForwardingAnnotatedItem<T, S>
   {
      
      @Override
      public boolean equals(Object other)
      {
         // TODO Do we need to check the other direction too?
         if (other instanceof AnnotatedItem)
         {
            AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
            return delegate().isAssignableFrom(that) &&
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
         return delegate().hashCode();
      }
      
   }
   
   // TODO Why can't we generify Set?
   
   /**
    * Type safe map for caching annotation metadata
    */
   @SuppressWarnings("unchecked")
   private class AnnotatedItemMap extends ForwardingMap<AnnotatedItem<?, ?>, Set>
   {

      private Map<AnnotatedItem<?, ?>, Set> delegate;
      
      public AnnotatedItemMap()
      {
         delegate = new HashMap<AnnotatedItem<?, ?>, Set>();
      }
      
      @SuppressWarnings("unchecked")
      public <T> Set<Bean<T>> get(AnnotatedItem<T, ?> key)
      {
         return (Set<Bean<T>>) super.get(key);
      }
      
      @Override
      protected Map<AnnotatedItem<?, ?>, Set> delegate()
      {
         return delegate;
      }

   }

   private AnnotatedItemMap resolvedInjectionPoints;
   private Set<AnnotatedItem<?, ?>> injectionPoints;
   
   private Map<String, Set<Bean<?>>> resolvedNames;
   
   private ManagerImpl manager;
   
   public Resolver(ManagerImpl manager)
   {
      this.manager = manager;
      this.injectionPoints = new HashSet<AnnotatedItem<?,?>>();
      this.resolvedInjectionPoints = new AnnotatedItemMap();
   }
   
   /**
    * Add multiple injection points for later resolving using 
    * {@link #registerInjectionPoint(AnnotatedItem)}. Useful during bootstrap.
    */
   public void addInjectionPoints(Collection<AnnotatedItem<?, ?>> elements)
   {
      injectionPoints.addAll(elements);
   }
   
   private <T, S> void registerInjectionPoint(final AnnotatedItem<T, S> element)
   {
      Set<Bean<?>> beans = retainHighestPrecedenceBeans(getMatchingBeans(element, manager.getBeans(), manager.getMetaDataCache()), manager.getEnabledDeploymentTypes());
      if (element.getType().isPrimitive())
      {
         for (Bean<?> bean : beans)
         {
            if (bean.isNullable())
            {
               throw new NullableDependencyException("Primitive injection points resolves to nullable web bean");
            }
         }
      }
	   resolvedInjectionPoints.put(new ResolvableAnnotatedItem<T, S>()
	   {

         @Override
         public AnnotatedItem<T, S> delegate()
         {
            return element;
         }
         
      }, beans);
   }
   
   /**
    * Reset all cached injection points. You must reset all cached injection
    * points when you add a bean to the manager
    */
   public void clear()
   {
      resolvedInjectionPoints = new AnnotatedItemMap();
      resolvedNames = new HashMap<String, Set<Bean<?>>>();
   }
   
   /**
    * Resolve all injection points added using {@link #addInjectionPoints(Collection)}
    */
   public void resolveInjectionPoints()
   {
      for (AnnotatedItem<?, ?> injectable : injectionPoints)
      {
         registerInjectionPoint(injectable);
      }
   }
   
   /**
    * Get the possible beans for the given element
    */
   public <T, S> Set<Bean<T>> get(final AnnotatedItem<T, S> key)
   {
      Set<Bean<T>> beans = new HashSet<Bean<T>>();

      AnnotatedItem<T, S> element = new ResolvableAnnotatedItem<T, S>()
      {

         @Override
         public AnnotatedItem<T, S> delegate()
         {
            return key;
         }
         
      };
      
      // TODO We don't need this I think
      if (element.getType().equals(Object.class))
      {
         // TODO Fix this cast
         beans = new HashSet<Bean<T>>((List) manager.getBeans());
      }
      else
      {
         if (!resolvedInjectionPoints.containsKey(element))
         {
            registerInjectionPoint(element);
         }
         beans = resolvedInjectionPoints.get(element);
      }
      return Collections.unmodifiableSet(beans);
   }
   
   /**
    * Get the possible beans for the given name
    */
   public Set<Bean<?>> get(String name)
   {
      Set<Bean<?>> beans;
      if (resolvedNames.containsKey(name))
      {
         beans = resolvedNames.get(name);
      }
      else
      {
         beans = new HashSet<Bean<?>>();
         for (Bean<?> bean : manager.getBeans())
         {
            if ( (bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
            {
               beans.add(bean);
            }
         }
         beans = retainHighestPrecedenceBeans(beans, manager.getEnabledDeploymentTypes());
         resolvedNames.put(name, beans);
         
      }
      return Collections.unmodifiableSet(beans);
   }
   
   private static Set<Bean<?>> retainHighestPrecedenceBeans(Set<Bean<?>> beans, List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      if (beans.size() > 0)
      {
         SortedSet<Class<? extends Annotation>> possibleDeploymentTypes = new TreeSet<Class<? extends Annotation>>(new ListComparator<Class<? extends Annotation>>(enabledDeploymentTypes));
         for (Bean<?> bean : beans)
         {
            possibleDeploymentTypes.add(bean.getDeploymentType());
         }
         possibleDeploymentTypes.retainAll(enabledDeploymentTypes);
         Set<Bean<?>> trimmed = new HashSet<Bean<?>>();
         if (possibleDeploymentTypes.size() > 0)
         {
            Class<? extends Annotation> highestPrecedencePossibleDeploymentType = possibleDeploymentTypes.last();
            
            for (Bean<?> bean : beans)
            {
               if (bean.getDeploymentType().equals(highestPrecedencePossibleDeploymentType))
               {
                  trimmed.add(bean);
               }
            }
         }
         return trimmed;
      }
      else
      {
         return beans;
      }
   }
   
   private static Set<Bean<?>> getMatchingBeans(AnnotatedItem<?, ?> element, List<Bean<?>> beans, MetaDataCache metaDataCache)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (element.isAssignableFrom(bean.getTypes()) && containsAllBindingBindingTypes(element, bean.getBindingTypes(), metaDataCache))
         {
            resolvedBeans.add(bean);
         }
      }
      return resolvedBeans;
   }
   
   private static boolean containsAllBindingBindingTypes(AnnotatedItem<?, ?> element, Set<Annotation> bindingTypes, MetaDataCache metaDataCache)
   {
      for (Annotation bindingType : element.getBindingTypes())
      {
         BindingTypeModel<?> bindingTypeModel = metaDataCache.getBindingTypeModel(bindingType.annotationType());
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
