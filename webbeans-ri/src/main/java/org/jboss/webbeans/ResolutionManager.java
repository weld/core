package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.injectable.Injectable;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.MapWrapper;

public class ResolutionManager
{

   // TODO Why can't we generify Set?
   
   @SuppressWarnings("unchecked")
   private class InjectableMap extends MapWrapper<Injectable<?, ?>, Set>
   {

      public InjectableMap()
      {
         super(new HashMap<Injectable<?, ?>, Set>());
      }
      
      @SuppressWarnings("unchecked")
      public <T> Set<Bean<T>> get(Injectable<T, ?> key)
      {
         return (Set<Bean<T>>) super.get(key);
      }

   }

   
   private InjectableMap resolvedInjectionPoints;
   private Set<Injectable<?, ?>> injectionPoints;
   
   private Map<String, Set<Bean<?>>> resolvedNames;
   
   private ManagerImpl manager;
   
   private boolean rebuildRequired = true;
   
   public ResolutionManager(ManagerImpl manager)
   {
      this.resolvedInjectionPoints = new InjectableMap();
      this.injectionPoints = new HashSet<Injectable<?,?>>();
      
      this.resolvedNames = new HashMap<String, Set<Bean<?>>>();
      
      this.manager = manager;
   }
   
   public void addInjectionPoint(Injectable<?, ?> injectable)
   {
      injectionPoints.add(injectable);
   }
   
   private void registerInjectionPoint(Injectable<?, ?> injectable)
   {
	   resolvedInjectionPoints.put(injectable, retainHighestPrecedenceBeans(injectable.getMatchingBeans(manager.getBeans()), manager.getEnabledDeploymentTypes())); 
   }
   
   public void clear()
   {
      rebuildRequired = true;
      resolvedInjectionPoints.clear();
      resolvedNames.clear();
   }
   
   private void resolveBeans()
   {
      if (rebuildRequired)
      {
         for (Injectable<?, ?> injectable : injectionPoints)
         {
            registerInjectionPoint(injectable);
         }
         rebuildRequired = false;
      }
   }
   
   public <T> Set<Bean<T>> get(Injectable<T, ?> key)
   {
      resolveBeans();
      return resolvedInjectionPoints.get(key);
   }
   
   public Set<Bean<?>> get(String name)
   {
      if (resolvedNames.containsKey(name))
      {
         return resolvedNames.get(name);
      }
      else
      {
         Set<Bean<?>> beans = new HashSet<Bean<?>>();
         for (Bean<?> bean : manager.getBeans())
         {
            if ( (bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
            {
               beans.add(bean);
            }
         }
         resolvedNames.put(name, retainHighestPrecedenceBeans(beans, manager.getEnabledDeploymentTypes()));
         return beans;
      }
   }
   
   private static Set<Bean<?>> retainHighestPrecedenceBeans(Set<Bean<?>> beans, List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      SortedSet<Class<? extends Annotation>> possibleDeploymentTypes = new TreeSet<Class<? extends Annotation>>(new ListComparator<Class<? extends Annotation>>(enabledDeploymentTypes));
      for (Bean<?> bean : beans)
      {
         possibleDeploymentTypes.add(bean.getDeploymentType());
      }
      possibleDeploymentTypes.retainAll(enabledDeploymentTypes);
      Class<? extends Annotation> highestPrecedencePossibleDeploymentType = possibleDeploymentTypes.last();
      Set<Bean<?>> trimmed = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (bean.getDeploymentType().equals(highestPrecedencePossibleDeploymentType))
         {
            trimmed.add(bean);
         }
      }
      return trimmed;
   }

}
