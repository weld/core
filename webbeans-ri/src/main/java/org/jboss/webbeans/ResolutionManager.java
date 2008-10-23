package org.jboss.webbeans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.injectable.Injectable;

public class ResolutionManager
{
   
   private Map<Injectable<?, ?>, Set<?>> resolvedInjectionPoints;
   private Set<Injectable<?, ?>> injectionPoints;
   private ManagerImpl manager;
   
   public ResolutionManager(ManagerImpl manager)
   {
      resolvedInjectionPoints = new HashMap<Injectable<?, ?>, Set<?>>();
      this.manager = manager;
   }
   
   public void addInjectionPoint(Injectable<?, ?> injectable)
   {
      injectionPoints.add(injectable);
   }
   
   public void registerInjectionPoint(Injectable<?, ?> injectable)
   {
	  resolvedInjectionPoints.put(injectable, injectable.getPossibleBeans(manager.getBeans())); 
   }
   
   public void registerInjectionPoints()
   {
      for (Injectable<?, ?> injectable : injectionPoints)
      {
         registerInjectionPoint(injectable);
      }
      injectionPoints.clear();
   }
   
   @SuppressWarnings("unchecked")
   public <T> Set<Bean<T>> get(Injectable<T, ?> key)
   {
      return (Set<Bean<T>>) resolvedInjectionPoints.get(key);
   }

}
