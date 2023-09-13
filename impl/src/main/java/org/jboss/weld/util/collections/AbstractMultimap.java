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

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An abstract {@link Multimap} backed by a {@link Map}.
 *
 * @author Martin Kouba
 *
 * @param <K> The key type
 * @param <V> The value type
 * @param <C> The collection of values type
 */
abstract class AbstractMultimap<K, V, C extends Collection<V>> implements Multimap<K, V>, Serializable {

    private static final long serialVersionUID = -8363450390652782067L;

    protected final Supplier<C> supplier;

    private final Map<K, C> map;

    /**
     *
     * @param mapSupplier
     * @param collectionSupplier
     * @param multimap
     */
    protected AbstractMultimap(Supplier<Map<K, C>> mapSupplier, Supplier<C> collectionSupplier, Multimap<K, V> multimap) {
        checkArgumentNotNull(mapSupplier, "mapSupplier");
        checkArgumentNotNull(collectionSupplier, "collectionSupplier");
        this.supplier = collectionSupplier;
        this.map = mapSupplier.get();
        if (multimap != null) {
            for (Entry<K, Collection<V>> entry : multimap.entrySet()) {
                C values = supplier.get();
                values.addAll(entry.getValue());
                putAll(entry.getKey(), values);
            }
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public C get(K key) {
        return map.computeIfAbsent(key, (k) -> supplier.get());
    }

    @Override
    public boolean put(K key, V value) {
        return get(key).add(value);
    }

    @Override
    public boolean putAll(K key, Collection<? extends V> values) {
        return get(key).addAll(values);
    }

    @Override
    public C replaceValues(K key, Iterable<? extends V> values) {
        C replacement = supplier.get();
        Iterables.addAll(replacement, values);
        return map.put(key, replacement);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Set<K> keySet() {
        return ImmutableSet.copyOf(map.keySet());
    }

    @Override
    public List<V> values() {
        return ImmutableList.copyOf(Iterables.concat(map.values()));
    }

    @Override
    public Set<V> uniqueValues() {
        ImmutableSet.Builder<V> builder = ImmutableSet.builder();
        for (C values : map.values()) {
            builder.addAll(values);
        }
        return builder.build();
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet() {
        ImmutableSet.Builder<Entry<K, Collection<V>>> builder = ImmutableSet.builder();
        for (Entry<K, C> entry : map.entrySet()) {
            builder.add(new MultimapEntry<K, Collection<V>>(entry.getKey(),
                    Multimaps.unmodifiableValueCollection(entry.getValue())));
        }
        return builder.build();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <K>
     * @param <V>
     */
    static class MultimapEntry<K, V> implements Map.Entry<K, V> {

        private final K key;

        private final V value;

        public MultimapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

    }

}
