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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.util.collections.ForwardingMap;

/**
 * An multimap which is internally backed by a HashMap and a HashSet
 * 
 * @author Pete Muir
 */
public class SetHashMultiMap<K, V> extends ForwardingMap<K, Set<V>> implements SetMultiMap<K, V>
{
   
   private final Set<V> EMPTY_COLLECTION = Collections.emptySet();

   // The map delegate
   private Map<K, Set<V>> delegate;

   /**
    * Constructor.
    */
   public SetHashMultiMap()
   {
      delegate = new HashMap<K, Set<V>>();
   }

   @Override
   protected Map<K, Set<V>> delegate()
   {
      return delegate;
   }
   
   public void deepPutAll(Map<? extends K, ? extends Set<V>> map)
   {
      for (Entry<? extends K, ? extends Set<V>> entry : map.entrySet())
      {
         put(entry.getKey(), new HashSet<V>(entry.getValue()));
      }
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
      if (!containsKey(key))
      {
         put(key, new HashSet<V>());
      }
      Set<V> values = get(key);
      if (!values.contains(value))
      {
         values.add(value);
      }
   }

}