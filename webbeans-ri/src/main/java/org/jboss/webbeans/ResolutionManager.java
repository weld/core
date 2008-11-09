package org.jboss.webbeans;

import java.lang.annotation.Annotation;
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

import org.jboss.webbeans.introspector.impl.Injectable;
import org.jboss.webbeans.util.ListComparator;

import com.google.common.collect.ForwardingMap;

public class ResolutionManager
{

   // TODO Why can't we generify Set?
   
   @SuppressWarnings("unchecked")
   private class InjectableMap extends ForwardingMap<Injectable<?, ?>, Set>
   {

      private Map<Injectable<?, ?>, Set> delegate;
      
      public InjectableMap()
      {
         delegate = new HashMap<Injectable<?, ?>, Set>();
      }
      
      @SuppressWarnings("unchecked")
      public <T> Set<Bean<T>> get(Injectable<T, ?> key)
      {
         return (Set<Bean<T>>) super.get(key);
      }
      
      @Override
      protected Map<Injectable<?, ?>, Set> delegate()
      {
         return delegate;
      }

   }

   
   private InjectableMap resolvedInjectionPoints;
   private Set<Injectable<?, ?>> injectionPoints;
   
   private Map<String, Set<Bean<?>>> resolvedNames;
   
   private ManagerImpl manager;
   
   public ResolutionManager(ManagerImpl manager)
   {
      this.injectionPoints = new HashSet<Injectable<?,?>>();
      this.resolvedInjectionPoints = new InjectableMap();
      this.manager = manager;
   }
   
   public void addInjectionPoint(Injectable<?, ?> injectable)
   {
      injectionPoints.add(injectable);
   }
   
   private void registerInjectionPoint(Injectable<?, ?> injectable)
   {
      Set<Bean<?>> beans = retainHighestPrecedenceBeans(injectable.getMatchingBeans(manager.getBeans(), manager.getModelManager()), manager.getEnabledDeploymentTypes());
      if (injectable.getType().isPrimitive())
      {
         for (Bean<?> bean : beans)
         {
            if (bean.isNullable())
            {
               throw new NullableDependencyException("Primitive injection points resolves to nullable web bean");
            }
         }
      }
	   resolvedInjectionPoints.put(injectable, beans); 
   }
   
   public void clear()
   {
      resolvedInjectionPoints = new InjectableMap();
      resolvedNames = new HashMap<String, Set<Bean<?>>>();
   }
   
   public void resolveInjectionPoints()
   {
      for (Injectable<?, ?> injectable : injectionPoints)
      {
         registerInjectionPoint(injectable);
      }
   }
   
   public <T> Set<Bean<T>> get(Injectable<T, ?> key)
   {
      Set<Bean<T>> beans;
      if (key.getType().equals(Object.class))
      {
         // TODO Fix this cast
         beans = new HashSet<Bean<T>>((List) manager.getBeans());
      }
      else
      {
         if (!resolvedInjectionPoints.containsKey(key))
         {
            registerInjectionPoint(key);
         }
         beans = resolvedInjectionPoints.get(key);
      }
      return Collections.unmodifiableSet(beans);
   }
   
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

}
