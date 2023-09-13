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
package org.jboss.weld.util.cache;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.ValueHolder;

/**
 * A {@link ComputingCache} backed by a {@link ConcurrentHashMap} which intentionally does not use
 * {@link Map#computeIfAbsent(Object, Function)}
 * and is reentrant.
 *
 * @author Jozef Hartinger
 *
 * @param <K> the key type
 * @param <V> the value type
 * @see ValueHolder
 * @see LazyValueHolder
 */
class ReentrantMapBackedComputingCache<K, V> implements ComputingCache<K, V>, Iterable<V> {

    private final ConcurrentMap<K, ValueHolder<V>> map;
    private final Long maxSize;
    private final Function<K, ValueHolder<V>> function;

    ReentrantMapBackedComputingCache(Function<K, V> computingFunction, Long maxSize) {
        this(computingFunction, LazyValueHolder::forSupplier, maxSize);
    }

    ReentrantMapBackedComputingCache(Function<K, V> computingFunction,
            Function<Supplier<V>, ValueHolder<V>> valueHolderFunction, Long maxSize) {
        this.map = new ConcurrentHashMap<>();
        this.maxSize = maxSize;
        this.function = (key) -> valueHolderFunction.apply(() -> computingFunction.apply(key));
    }

    @Override
    public V getValue(final K key) {
        ValueHolder<V> value = map.get(key);
        if (value == null) {
            value = function.apply(key);
            ValueHolder<V> previous = map.putIfAbsent(key, value);
            if (previous != null) {
                value = previous;
            }
            // finally, check that we are not over the bound
            if (maxSize != null && size() > maxSize) {
                clear();
            }
        }
        return value.get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCastValue(Object key) {
        return (T) getValue((K) key);
    }

    @Override
    public V getValueIfPresent(K key) {
        ValueHolder<V> value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.getIfPresent();
    }

    @Override
    public long size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public void invalidate(Object key) {
        map.remove(key);
    }

    @Override
    public Iterable<V> getAllPresentValues() {
        return this;
    }

    @Override
    public String toString() {
        return map.toString();
    }

    @Override
    public void forEachValue(Consumer<? super V> consumer) {
        for (ValueHolder<V> valueHolder : map.values()) {
            V value = valueHolder.getIfPresent();
            if (value != null) {
                consumer.accept(value);
            }
        }
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {

            private final Iterator<ValueHolder<V>> delegate = map.values().iterator();
            private V next = findNext();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            private V findNext() {
                while (delegate.hasNext()) {
                    V next = delegate.next().getIfPresent();
                    if (next != null) {
                        return next;
                    }
                }
                return null;
            }

            @Override
            public V next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                V current = next;
                this.next = findNext();
                return current;
            }
        };
    }
}
