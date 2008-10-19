package org.jboss.webbeans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.injectable.Injectable;

public class ResolutionManager
{
   
   private Map<Injectable<?, ?>, Set<?>> resolvedInjectionPoints;
   private ManagerImpl manager;
   
   public ResolutionManager(ManagerImpl manager)
   {
      resolvedInjectionPoints = new HashMap<Injectable<?, ?>, Set<?>>();
      this.manager = manager;
   }
   
   public void registerInjectionPoint(Injectable<?, ?> injectable)
   {
      resolvedInjectionPoints.put(injectable, injectable.getPossibleTargets(manager.getBeans()));
   }
   
   public void registerInjectionPoints(Set<Injectable<?, ?>> injectables)
   {
      for (Injectable<?, ?> injectable : injectables)
      {
         registerInjectionPoint(injectable);
      }
   }
   
   @SuppressWarnings("unchecked")
   public <T> Set<Bean<T>> get(Injectable<T, ?> key)
   {
      return (Set<Bean<T>>) resolvedInjectionPoints.get(key);
   }

}
