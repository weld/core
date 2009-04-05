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

import java.util.Map;
import java.util.Set;

/**
 * A concurrent multimap, in which the multi-values are stored in a Set
 * 
 * @author Pete Muir
 *
 */
public interface SetMultiMap<K, V> extends Map<K, Set<V>>
{
   
   /**
    * Add a value. The collection is automatically created. If the value already
    * exists, it isn't added
    * 
    * @param key the key to add the value to
    * @param value the value to add
    */
   public void put(K key, V value);
   
   /**
    * Put all the data in the map into this map, copying each multi-value, not
    * just attaching the existing multi-value
    * 
    * @param map the map to copy
    */
   public void deepPutAll(Map<? extends K, ? extends Set<V>> map);
   
}
