/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.util.collections;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@link java.util.Map} that can contain multiple values, where the keys
 * and values are stored in {@link ArraySet} instances.
 * 
 * @author David Allen
 *
 */
public class ArraySetMultimap<K, V> extends AbstractMap<K, List<V>>
{
   private ArraySet<Map.Entry<K, List<V>>> entrySet = new ArraySet<Map.Entry<K, List<V>>>();

   public ArraySetMultimap()
   {
      
   }
   
   public ArraySetMultimap(Map<K, List<V>> map)
   {
      // Add entries from the given map to this one
   }

   @Override
   public List<V> get(Object key)
   {
      List<V> result = super.get(key);
      if (result == null)
      {
         result = Collections.emptyList();
      }
      return result;
   }

   public List<V> put(K key, V value)
   {
      List<V> result = super.get(key);
      if (result == null)
      {
         result = new ArrayList<V>();
         SimpleEntry<K, List<V>> entry = new SimpleEntry<K, List<V>>(key, result);
         entrySet.add(entry);
      }
      result.add(value);
      return result;
   }

   public void trimToSize()
   {
      for (Map.Entry<K, List<V>> entry : this.entrySet())
      {
         ((ArrayList<V>)entry.getValue()).trimToSize();
      }
   }

   @Override
   public Set<java.util.Map.Entry<K, List<V>>> entrySet()
   {
      return entrySet;
   }
}
