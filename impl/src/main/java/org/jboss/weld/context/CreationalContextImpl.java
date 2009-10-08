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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

public class CreationalContextImpl<T> implements CreationalContext<T>, WeldCreationalContext<T>, Serializable
{

   private static final long serialVersionUID = 7375854583908262422L;
   
   // Only needed for creation, by the time it's serialized we don't need it
   private final transient Map<Contextual<?>, Object> incompleteInstances;
   private final transient Contextual<T> contextual;
   
   private final DependentInstancesStore dependentInstancesStore;
   
   private final DependentInstancesStore parentDependentInstancesStore;
   
   public CreationalContextImpl(Contextual<T> contextual)
   {
      this(contextual, new HashMap<Contextual<?>, Object>(), new DependentInstancesStore());
   }
   
   private CreationalContextImpl(Contextual<T> contextual, Map<Contextual<?>, Object> incompleteInstances, DependentInstancesStore parentDependentInstancesStore)
   {
      this.incompleteInstances = incompleteInstances;
      this.contextual = contextual;
      this.dependentInstancesStore = new DependentInstancesStore();
      this.parentDependentInstancesStore = parentDependentInstancesStore;
   }
   
   public void push(T incompleteInstance)
   {
      incompleteInstances.put(contextual, incompleteInstance);
   }
   
   public <S> WeldCreationalContext<S> getCreationalContext(Contextual<S> Contextual)
   {
      return new CreationalContextImpl<S>(Contextual, new HashMap<Contextual<?>, Object>(incompleteInstances), dependentInstancesStore);
   }
   
   public <S> S getIncompleteInstance(Contextual<S> bean)
   {
      return (S) incompleteInstances.get(bean);
   }
   
   public boolean containsIncompleteInstance(Contextual<?> bean)
   {
      return incompleteInstances.containsKey(bean);
   }
   
   public DependentInstancesStore getParentDependentInstancesStore()
   {
      return parentDependentInstancesStore;
   }

   public void release()
   {
      dependentInstancesStore.destroyDependentInstances();
      incompleteInstances.clear();
   }
   
}
