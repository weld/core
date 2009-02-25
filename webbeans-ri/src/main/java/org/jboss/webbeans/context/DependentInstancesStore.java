/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.webbeans.context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A store for dependent instances created under a given key
 * 
 * @author Nicklas Karlsson
 */
public class DependentInstancesStore
{
   private static LogProvider log = Logging.getLogProvider(DependentInstancesStore.class);
   
   // A object -> List of contextual instances mapping
   private Map<Object, List<ContextualInstance<?>>> dependentInstances;

   /**
    * Creates a new DependentInstancesStore
    */
   public DependentInstancesStore()
   {
      dependentInstances = new ConcurrentHashMap<Object, List<ContextualInstance<?>>>();
   }

   /**
    * Adds a dependent instance under a given key
    * 
    * @param key The key to store the instance under
    * @param contextualInstance The instance to store
    */
   public <T> void addDependentInstance(Object key, ContextualInstance<T> contextualInstance)
   {
      List<ContextualInstance<?>> instances = dependentInstances.get(key);
      if (instances == null)
      {
         instances = new CopyOnWriteArrayList<ContextualInstance<?>>();
         dependentInstances.put(key, instances);
      }
      log.trace("Registered dependent instance " + contextualInstance + " under key " + key);
      instances.add(contextualInstance);
   }

   /**
    * Destroys all dependent objects associated with a particular key and remove
    * that key from the store
    * 
    * @param key The key to remove
    */
   public void destroyDependentInstances(Object key)
   {
      log.trace("Destroying dependent instances under key " + key);
      if (!dependentInstances.containsKey(key))
      {
         return;
      }
      for (ContextualInstance<?> injectedInstance : dependentInstances.get(key))
      {
         injectedInstance.destroy();
      }
      dependentInstances.remove(key);
   }

}
