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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.webbeans.util.collections.ConcurrentList;
import org.jboss.webbeans.util.collections.ForwardingConcurrentMap;

/**
 * An concurrent multimap which is internally backed by a a ConcurrentHashMap 
 * and a CopyOnWriteArrayList
 * 
 * @author Pete Muir
 */
public class ConcurrentListHashMultiMap<K, V> extends ForwardingConcurrentMap<K, ConcurrentList<V>> implements ConcurrentListMultiMap<K, V>
{
   
   private final ConcurrentList<V> EMPTY_LIST = ConcurrentList.emptyList();

   // The map delegate
   private ConcurrentHashMap<K, ConcurrentList<V>> delegate;

   /**
    * Constructor.
    */
   public ConcurrentListHashMultiMap()
   {
      delegate = new ConcurrentHashMap<K, ConcurrentList<V>>();
   }

   @Override
   protected ConcurrentMap<K, ConcurrentList<V>> delegate()
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
   public ConcurrentList<V> get(Object key)
   {
      ConcurrentList<V> values = super.get(key);
      return values != null ? values : EMPTY_LIST;
   }

   public void put(K key, V value)
   {
      delegate.putIfAbsent(key, new ConcurrentList<V>());
      get(key).add(value);
   }

}