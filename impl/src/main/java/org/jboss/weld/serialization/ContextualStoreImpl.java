/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.serialization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

/**
 * Implementation of {@link org.jboss.weld.serialization.spi.ContextualStore}
 * 
 * @author Pete Muir
 * 
 */
public class ContextualStoreImpl implements ContextualStore
{

   private static final String GENERATED_ID_PREFIX = ContextualStoreImpl.class.getName();

   // The map containing container-local contextuals
   private final BiMap<Contextual<?>, String> contextuals;

   // The map containing passivation capable contextuals
   private final ConcurrentMap<String, Contextual<?>> passivationCapableContextuals;

   private final AtomicInteger idGenerator;

   public ContextualStoreImpl()
   {
      this.idGenerator = new AtomicInteger(0);
      BiMap<Contextual<?>, String> map = HashBiMap.create();
      // TODO Somehow remove this sync if it shows bad in a profiler
      this.contextuals = Maps.synchronizedBiMap(map);
      this.passivationCapableContextuals = new ConcurrentHashMap<String, Contextual<?>>();
   }

   /**
    * Given a particular id, return the correct contextual. For contextuals
    * which aren't passivation capable, the contextual can't be found in another
    * container, and null will be returned.
    * 
    * @param id An identifier for the contextual
    * @return the contextual
    */
   @SuppressWarnings("unchecked")
   public <C extends Contextual<I>, I> C getContextual(String id)
   {
      if (id.startsWith(GENERATED_ID_PREFIX))
      {
         return (C) contextuals.inverse().get(id);
      }
      else
      {
         return (C) passivationCapableContextuals.get(id);
      }
   }

   /**
    * Add a contextual (if not already present) to the store, and return it's
    * id. If the contextual is passivation capable, it's id will be used,
    * otherwise an id will be generated
    * 
    * @param contextual the contexutal to add
    * @return the current id for the contextual
    */
   @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED", justification="Using non-standard semantics of putIfAbsent")
   public String putIfAbsent(Contextual<?> contextual)
   {
      if (contextual instanceof PassivationCapable)
      {
         PassivationCapable passivationCapable = (PassivationCapable) contextual;
         passivationCapableContextuals.putIfAbsent(passivationCapable.getId(), contextual);
         return passivationCapable.getId();
      }
      else if (contextuals.containsKey(contextual))
      {
         return contextuals.get(contextual);
      }
      else
      {
         synchronized (contextual)
         {
            if (contextuals.containsKey(contextual))
            {
               return contextuals.get(contextual);
            }
            String id = new StringBuilder().append(GENERATED_ID_PREFIX).append(idGenerator.incrementAndGet()).toString();
            contextuals.put(contextual, id);
            return id;
         }
      }
   }

   public <C extends Contextual<I>, I> SerializableContextual<C, I> getSerializableContextual(Contextual<I> contextual)
   {
      return new SerializableContextualImpl(contextual);
   }

   public <C extends Contextual<I>, I> SerializableContextualInstance<C, I> getSerializableContextualInstance(Contextual<I> contextual, I instance, CreationalContext<I> creationalContext)
   {
      return new SerializableContextualInstanceImpl(contextual, instance, creationalContext);
   }

   public void cleanup()
   {
      contextuals.clear();
      passivationCapableContextuals.clear();
   }
}
