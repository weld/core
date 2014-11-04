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

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jboss.weld.util.collections.ImmutableMap;

/**
 * A {@link ComputingCache} backed by a {@link ConcurrentHashMap}. Values stored in the cache are wrapped in a {@link WeakReference}.
 *
 * @author Martin Kouba
 *
 * @param <K>
 * @param <V>
 * @see ConcurrentHashMap#compute(Object, BiFunction)
 */
class WeakValueMapBackedComputingCache<K, V> extends AbstractMapBackedComputingCache<K, V> {

    private final BiFunction<K, WeakReference<V>, WeakReference<V>> remappingFunction;

    private final ConcurrentHashMap<K, WeakReference<V>> map;

    /**
     *
     * @param maxSize
     * @param computingFunction
     */
    WeakValueMapBackedComputingCache(Long maxSize, Function<K, V> computingFunction) {
        super(maxSize);
        this.map = new ConcurrentHashMap<K, WeakReference<V>>();
        this.remappingFunction = new WeakValueFunctionWrapper<K, V>(computingFunction, this);
    }

    @Override
    public V getValueIfPresent(K key) {
        WeakReference<V> reference = map.get(key);
        return reference != null ? reference.get() : null;
    }

    @Override
    public Map<K, V> getAllPresent() {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (Entry<K, WeakReference<V>> entry : map.entrySet()) {
            V value = entry.getValue().get();
            if (value != null) {
                builder.put(entry.getKey(), value);
            }
        }
        return builder.build();
    }

    protected V computeIfNeeded(K key) {
        return map.compute(key, remappingFunction).get();
    }

    @Override
    protected ConcurrentMap<K, ?> getMap() {
        return map;
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <K>
     * @param <V>
     */
    static class WeakValueFunctionWrapper<K, V> extends FunctionWrapper<K, V> implements BiFunction<K, WeakReference<V>, WeakReference<V>> {

        /**
         *
         * @param computingFunction
         * @param cache
         */
        public WeakValueFunctionWrapper(Function<K, V> computingFunction, AbstractComputingCache<K, V> cache) {
            super(computingFunction, cache);
        }

        @Override
        public WeakReference<V> apply(K key, WeakReference<V> currentValue) {
            checkMaxSize();
            if (currentValue == null || currentValue.get() == null) {
                return new WeakReference<V>(computingFunction.apply(key));
            }
            return currentValue;
        }

    }

}
