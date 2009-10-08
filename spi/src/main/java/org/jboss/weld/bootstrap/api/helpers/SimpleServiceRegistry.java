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
package org.jboss.weld.bootstrap.api.helpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;

/**
 * A registry for services
 * 
 * @author Pete Muir
 *
 */
public class SimpleServiceRegistry implements ServiceRegistry
{
   
   private final Map<Class<? extends Service>, Service> services;
   
   public SimpleServiceRegistry()
   {
      this.services = new HashMap<Class<? extends Service>, Service>();
   }
   
   public <S extends Service> void add(java.lang.Class<S> type, S service) 
   {
      services.put(type, service);
   }
   
   public void addAll(Collection<Entry<Class<? extends Service>, Service>> services) 
   {
     for (Entry<Class<? extends Service >, Service> entry : services)
     {
        this.services.put(entry.getKey(), entry.getValue());
     }
   }
   
   public Set<Entry<Class<? extends Service>, Service>> entrySet()
   {
      return services.entrySet();
   }
   
   protected Map<Class<? extends Service>, Service> get()
   {
      return services;
   }
   
   @SuppressWarnings("unchecked")
   public <S extends Service> S get(Class<S> type)
   {
      return (S) services.get(type);
   }
   
   public <S extends Service> boolean contains(Class<S> type)
   {
      return services.containsKey(type);
   }
   
   public void cleanup()
   {
      for (Service service : services.values())
      {
         service.cleanup();
      }
   }
   
   @Override
   public String toString()
   {
      return services.toString();
   }
   
   @Override
   public int hashCode()
   {
      return services.hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof Map<?, ?>)
      {
         return services.equals(obj);
      }
      else
      {
         return false;
      }
   }
   
   public Iterator<Service> iterator()
   {
      return new ValueIterator<Class<? extends Service>, Service>()
      {

         @Override
         protected Iterator<Entry<Class<? extends Service>, Service>> delegate()
         {
            return services.entrySet().iterator();
         }
         
      };
   }
   
   private static abstract class ValueIterator<K, V> implements Iterator<V>
   {
      
      protected abstract Iterator<Entry<K, V>> delegate();

      public boolean hasNext()
      {
         return delegate().hasNext();
      }

      public V next()
      {
         return delegate().next().getValue();
      }

      public void remove()
      {
         delegate().remove();
      }
      
   }
   
}
