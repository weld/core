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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.webbeans.util.collections.ForwardingMap;

/**
 * An multimap which is internally backed by a HashMap and a ArrayList
 * 
 * @author Pete Muir
 */
public class ListHashMultiMap<K, V> extends ForwardingMap<K, List<V>> implements ListMultiMap<K, V>
{
   
   private final List<V> EMPTY_COLLECTION = Collections.emptyList();

   // The map delegate
   private Map<K, List<V>> delegate;

   /**
    * Constructor.
    */
   public ListHashMultiMap()
   {
      delegate = new HashMap<K, List<V>>();
   }

   @Override
   protected Map<K, List<V>> delegate()
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
   public List<V> get(Object key)
   {
      List<V> values = super.get(key);
      return values != null ? values : EMPTY_COLLECTION;
   }

   /**
    * Adds an value for a given key
    * 
    * Implicitly creates a new list if there is none for the key. Only adds
    * the value if it is not already present
    * 
    * @param key the key
    * @param value the value
    */
   public void put(K key, V value)
   {
      if (!containsKey(key))
      {
         put(key, new ArrayList<V>());
      }
      get(key).add(value);
   }

}