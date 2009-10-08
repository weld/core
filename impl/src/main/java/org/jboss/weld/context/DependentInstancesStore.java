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
package org.jboss.weld.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.log.Log;
import org.jboss.weld.log.Logging;

/**
 * A store for dependent instances created under a given key
 * 
 * @author Nicklas Karlsson
 */
public class DependentInstancesStore implements Serializable
{
   private static final long serialVersionUID = -2349574791148336833L;

   private static Log log = Logging.getLog(DependentInstancesStore.class);
   
   // A object -> List of contextual instances mapping
   private List<ContextualInstance<?>> dependentInstances;

   /**
    * Creates a new DependentInstancesStore
    */
   public DependentInstancesStore()
   {
      dependentInstances = Collections.synchronizedList(new ArrayList<ContextualInstance<?>>());
   }

   /**
    * Adds a dependent instance under a given key
    * 
    * @param key The key to store the instance under
    * @param contextualInstance The instance to store
    */
   public <T> void addDependentInstance(ContextualInstance<T> contextualInstance)
   {
      log.trace("Registered dependent instance #0", contextualInstance);
      dependentInstances.add(contextualInstance);
   }

   /**
    * Destroys all dependent objects
    * 
    * @param key The key to remove
    */
   public void destroyDependentInstances()
   {
      log.trace("Destroying dependent instances");
      for (ContextualInstance<?> injectedInstance : dependentInstances)
      {
         destroy(injectedInstance);
      }
   }
   
   private static <T> void destroy(ContextualInstance<T> beanInstance)
   {
      beanInstance.getContextual().destroy(beanInstance.getInstance(), beanInstance.getCreationalContext());
   }
   
   @Override
   public String toString()
   {
      return "Dependent Instances: " + dependentInstances;
   }

}
