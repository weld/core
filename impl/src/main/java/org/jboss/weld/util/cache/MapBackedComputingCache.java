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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.jboss.weld.util.collections.WeldCollections;

/**
 * A {@link ComputingCache} backed by a {@link ConcurrentHashMap}.
 *
 * @author Martin Kouba
 *
 * @param <K>
 * @param <V>
 * @see ConcurrentHashMap#computeIfAbsent(Object, Function)
 */
class MapBackedComputingCache<K, V> extends AbstractMapBackedComputingCache<K, V> {

    private final ConcurrentHashMap<K, V> map;

    private final Function<K, V> function;

    /**
     *
     * @param maxSize
     * @param computingFunction
     */
    MapBackedComputingCache(Long maxSize, Function<K, V> computingFunction) {
        super(maxSize);
        this.map = new ConcurrentHashMap<K, V>();
        this.function = (maxSize != null ? new DefaultFunctionWrapper<>(computingFunction, this) : computingFunction);
    }

    @Override
    public V getValueIfPresent(K key) {
        return map.get(key);
    }

    @Override
    public Map<K, V> getAllPresent() {
        return WeldCollections.immutableMapView(map);
    }

    protected V computeIfNeeded(K key) {
        return map.computeIfAbsent(key, function);
    }

    @Override
    protected ConcurrentMap<K, ?> getMap() {
        return map;
    }

}
