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
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.util.Supplier;

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
    private final ConcurrentMap<K, C> concurrentMap;

    /** Only one map supplier can be provided, otherwise there is an exception
     *
     * @param mapSupplier
     * @param concurrentMapSupplier
     * @param collectionSupplier
     * @param multimap
     */
    protected AbstractMultimap(Supplier<Map<K, C>> mapSupplier, Supplier<ConcurrentMap<K, C>> concurrentMapSupplier, Supplier<C> collectionSupplier, Multimap<K, V> multimap) {
        // exactly one map supplier has to have a non-null value
        if ((mapSupplier == null && concurrentMapSupplier == null) ||(mapSupplier != null && concurrentMapSupplier != null)) {
            throw new IllegalArgumentException("AbstractMultimap has to have only one map supplier provided");
        }
        checkArgumentNotNull(collectionSupplier, "collectionSupplier");
        this.supplier = collectionSupplier;
        this.concurrentMap = (concurrentMapSupplier != null) ? concurrentMapSupplier.get() : null;
        this.map = (mapSupplier != null) ? mapSupplier.get() : null;

        if (multimap != null) {
            for (Entry<K, Collection<V>> entry : multimap.entrySet()) {
                C values = supplier.get();
                values.addAll(entry.getValue());
                putAll(entry.getKey(), values);
            }
        }
    }

    /**
     *
     * @return either ConcurrentMap or Map based on which is not null
     */
    private Map<K, C> getMap() {
        return (concurrentMap != null) ? concurrentMap : map;
    }

    @Override
    public int size() {
        return getMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getMap().isEmpty();
    }

    @Override
    public C get(K key) {
        C value;
        if (concurrentMap != null) {
            value = concurrentMap.get(key);
            if (value == null ) {
                value = supplier.get();
                if (value != null) {
                    C previousValue = concurrentMap.putIfAbsent(key, value);
                    if (previousValue != null) {
                        value = previousValue;
                    }
                }
            }
        } else {
            value = map.get(key);
            if (value == null ) {
                value = supplier.get();
                if (value != null) {
                    C previousValue = map.put(key, value);
                    if (previousValue != null) {
                        value = previousValue;
                    }
                }
            }
        }
        return value;
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
        return getMap().put(key, replacement);
    }

    @Override
    public boolean containsKey(Object key) {
        return getMap().containsKey(key);
    }

    @Override
    public Set<K> keySet() {
        return ImmutableSet.copyOf(getMap().keySet());
    }

    @Override
    public List<V> values() {
        return ImmutableList.copyOf(Iterables.concat(getMap().values()));
    }

    @Override
    public Set<V> uniqueValues() {
        ImmutableSet.Builder<V> builder = ImmutableSet.builder();
        for (C values : getMap().values()) {
            builder.addAll(values);
        }
        return builder.build();
    }

    @Override
    public Set<Entry<K, Collection<V>>> entrySet() {
        ImmutableSet.Builder<Entry<K, Collection<V>>> builder = ImmutableSet.builder();
        for (Entry<K, C> entry : getMap().entrySet()) {
            builder.add(new MultimapEntry<K, Collection<V>>(entry.getKey(), Multimaps.unmodifiableValueCollection(entry.getValue())));
        }
        return builder.build();
    }

    @Override
    public void clear() {
        getMap().clear();
    }

    @Override
    public String toString() {
        return getMap().toString();
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