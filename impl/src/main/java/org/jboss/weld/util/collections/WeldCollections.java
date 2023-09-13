/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection utilities.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldCollections {

    private WeldCollections() {
    }

    /**
     * Returns an immutable view of a given set. If the given set is empty, a shared instance is returned. If the given set is
     * an instance of {@link ArraySet}, it is trimmed.
     */
    public static <T> Set<T> immutableSetView(Set<T> set) {
        if (set instanceof ImmutableSet<?>) {
            return set;
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Returns an immutable view of a given list. If the given list is empty, a shared instance is returned. If the given list
     * is an instance of {@link ArrayList}, it is trimmed.
     */
    public static <T> List<T> immutableListView(List<T> list) {
        if (list instanceof ImmutableList<?>) {
            return list;
        }
        if (list instanceof ArrayList<?>) {
            ArrayList.class.cast(list).trimToSize();
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Returns an immutable view of a given map.
     */
    public static <K, V> Map<K, V> immutableMapView(Map<K, V> map) {
        if (map instanceof ImmutableMap<?, ?>) {
            return map;
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Fluent version of {@link Collections#sort(List, Comparator)}
     */
    public static <T> List<T> sort(List<T> list, Comparator<? super T> comparator) {
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * Returns the supplied collection as a multi-row string with every toString() of every element of the collection
     * in its own row.
     * Example: toMultiRowString(Arrays.asList("aaa", "bbb", "ccc")) will return:
     *
     * <pre>
     *   - aaa,
     *   - bbb,
     *   - ccc
     * </pre>
     */
    public static String toMultiRowString(Collection<?> collection) {
        if (collection == null) {
            return null;
        }
        if (collection.isEmpty()) {
            return "(empty collection)";
        }
        StringBuilder builder = new StringBuilder("\n  - ");
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext();) {
            builder.append(iterator.next());
            if (iterator.hasNext()) {
                builder.append(",\n  - ");
            }
        }
        return builder.toString();
    }

    public static <T> boolean addIfNotNull(Collection<T> collection, T element) {
        if (element == null) {
            return false;
        }
        return collection.add(element);
    }

    /**
     * Utility method for working with maps. Unlike {@link Map#putIfAbsent(Object, Object)} this method always returns the value
     * that ends up store in the map
     * which is either the old value (if any was present) or the new value (if it was stored in the map).
     *
     * @param map the map
     * @param key the key
     * @param value the value
     * @return the value that ends up store in the map which is either the old value (if any was present) or the new value (if
     *         it was stored in the map)
     */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V old = map.putIfAbsent(key, value);
        if (old != null) {
            return old;
        }
        return value;
    }
}
