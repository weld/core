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

import java.util.concurrent.ConcurrentMap;

import org.jboss.webbeans.util.collections.ConcurrentList;

/**
 * A concurrent multimap, in which the multi-values are stored in a list
 * 
 * @author Pete Muir
 *
 */
public interface ConcurrentListMultiMap<K, V> extends ConcurrentMap<K, ConcurrentList<V>>
{
  
   /**
    * Add a value, creating the list if it doesn't exist
    * 
    * @param key the key to store the value under
    * @param value the value to add
    */
   public void put(K key, V value);
   
}
