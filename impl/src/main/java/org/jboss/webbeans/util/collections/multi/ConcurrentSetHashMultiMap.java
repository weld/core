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
package org.jboss.webbeans.util.collections.multi;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jboss.webbeans.util.collections.ForwardingConcurrentMap;

/**
 * An concurrent multimap which is internally backed by a a ConcurrentHashMap 
 * and a CopyOnWriteArraySet
 * 
 * @author Pete Muir
 */
public class ConcurrentSetHashMultiMap<K, V> extends ForwardingConcurrentMap<K, Set<V>> implements ConcurrentSetMultiMap<K, V>
{
   
   private final Set<V> EMPTY_COLLECTION = Collections.emptySet();

   // The map delegate
   private ConcurrentMap<K, Set<V>> delegate;

   /**
    * Constructor.
    */
   public ConcurrentSetHashMultiMap()
   {
      delegate = new ConcurrentHashMap<K, Set<V>>();
   }
   
   public void deepPutAll(Map<? extends K, ? extends Set<V>> map)
   {
      for (Entry<? extends K, ? extends Set<V>> entry : map.entrySet())
      {
         put(entry.getKey(), new CopyOnWriteArraySet<V>(entry.getValue()));
      }
   }

   @Override
   protected ConcurrentMap<K, Set<V>> delegate()
   {
      return delegate;
   }

   /**
    * Gets the list of values for a given key
    * 
    * @param key
    *           the key
    * @return The list of values. An empty list is returned if there are no
    *         matches.
    */
   @Override
   public Set<V> get(Object key)
   {
      Set<V> values = super.get(key);
      return values != null ? values : EMPTY_COLLECTION;
   }

   public void put(K key, V value)
   {
      delegate().putIfAbsent(key, new CopyOnWriteArraySet<V>());
      get(key).add(value);
   }

}