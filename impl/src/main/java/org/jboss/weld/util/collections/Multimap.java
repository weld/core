/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A collection-like structure that maps keys to collections of values.
 *
 * @author Martin Kouba
 * @param <K> The key
 * @param <V> The value
 */
public interface Multimap<K, V> {

    /**
     * Unlike Guava's<code>Multimap#size()</code> this method returns the number of key-value mappings.
     *
     * @return the number of key-value mappings
     */
    int size();

    /**
     *
     * @return <code>true</code> if there are no key-value mappings
     */
    boolean isEmpty();

    /**
     * This method never returns null. If no collection of values for a given key exists a new value collection is initialized.
     *
     * @param key
     * @return the collection of values for the given key
     */
    Collection<V> get(K key);

    /**
     *
     * @param key
     * @param value
     * @return <code>true</code> if the the size of the collection associated with the given key increased, <code>false</code>
     *         otherwise (e.g. if the collection
     *         of values doesn't allow duplicates)
     */
    boolean put(K key, V value);

    /**
     *
     * @param key
     * @param values
     * @return <code>true</code> if the the size of the collection associated with the given key increased, <code>false</code>
     *         otherwise (e.g. if the collection
     *         of values doesn't allow duplicates)
     */
    boolean putAll(K key, Collection<? extends V> values);

    /**
     * Note that the original collection of values is completely replaced by a new collection which contains all elements from
     * the given iterable. If the
     * collection of values doesn't allow duplicates, these elements are removed.
     *
     * @param key
     * @param values
     * @return the collection of replaced values
     */
    Collection<V> replaceValues(K key, Iterable<? extends V> values);

    /**
     *
     * @param key
     * @return <code>true</code> if the multimap contains a mapping for the given key
     */
    boolean containsKey(Object key);

    /**
     *
     * @return an immutable set of keys
     */
    Set<K> keySet();

    /**
     * The list may include the same value multiple times if it occurs in multiple mappings or if the collection of values for
     * the mapping allows duplicate
     * elements.
     *
     * @return an immutable list of all the values in the multimap
     */
    List<V> values();

    /**
     *
     * @return an immutable set of all the values in the multimap
     */
    Set<V> uniqueValues();

    /**
     * {@link Entry#getValue()} always returns an unmodifiable collection. {@link Entry#setValue(Object)} operation is not
     * supported.
     *
     * @return an immutable set of all key-value pairs
     */
    Set<Entry<K, Collection<V>>> entrySet();

    /**
     * Removes all of the mappings.
     */
    void clear();

}
