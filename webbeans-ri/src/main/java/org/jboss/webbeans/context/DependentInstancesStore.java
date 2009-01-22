package org.jboss.webbeans.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DependentInstancesStore
{
   private Map<Object, List<ContextualInstance<?>>> dependentInstances;

   public DependentInstancesStore()
   {
      dependentInstances = new ConcurrentHashMap<Object, List<ContextualInstance<?>>>();
   }

   public <T> void addDependentInstance(Object parent, ContextualInstance<T> contextualInstance)
   {
      List<ContextualInstance<?>> instances = dependentInstances.get(parent);
      if (instances == null)
      {
         instances = new CopyOnWriteArrayList<ContextualInstance<?>>();
         dependentInstances.put(parent, instances);
      }
      instances.add(contextualInstance);
   }

   public void destroyDependentInstances(Object parent)
   {
      if (!dependentInstances.containsKey(parent))
      {
         return;
      }
      for (ContextualInstance<?> injectedInstance : dependentInstances.get(parent))
      {
         injectedInstance.destroy();
      }
      dependentInstances.remove(parent);
   }
}
