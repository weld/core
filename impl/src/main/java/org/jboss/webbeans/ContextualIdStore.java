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
package org.jboss.webbeans;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.Contextual;

import org.jboss.webbeans.bootstrap.api.Service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

/**
 * Application wide contextual identifier service which allows a serializable
 * reference to a contextual to be obtained, and the contextual to be returned
 * for a given id. Note that this allows a Bean object to be loaded regardless
 * of the bean's accessiblity from the current module, and should not be abused
 * as a way to ignore accessibility rules enforced during resolution.
 * 
 * @author Pete Muir
 * 
 */
public class ContextualIdStore implements Service
{
   
   private final BiMap<Contextual<?>, Integer> contextuals;
   private final AtomicInteger idGenerator;
   
   public ContextualIdStore()
   {
      this.idGenerator = new AtomicInteger(0);
      BiMap<Contextual<?>, Integer> map = HashBiMap.create();
      // TODO Somehow remove this sync if it shows bad in a profiler
      this.contextuals = Maps.synchronizedBiMap(map);
   }
   
   @SuppressWarnings("unchecked")
   public <T> Contextual<T> getContextual(Integer id)
   {
      return (Contextual<T>) contextuals.inverse().get(id);
   }
   
   public Integer getId(Contextual<?> contextual)
   {
      if (contextuals.containsKey(contextual))
      {
         return contextuals.get(contextual);
      }
      else
      {
         Integer id = idGenerator.incrementAndGet();
         contextuals.put(contextual, id);
         return id;
      }
   }
}
