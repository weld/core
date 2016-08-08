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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.util.Preconditions;

/**
 * Weld's immutable map implementation.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ImmutableMap<K, V> extends AbstractImmutableMap<K, V> {

    ImmutableMap() {
    }

    /**
     * Creates an immutable map. A copy of the given map is used. As a result, it is safe to modify the source map afterwards.
     *
     * @param map the given map
     * @return an immutable map
     */
    public static <K, V> Map<K, V> copyOf(Map<K, V> map) {
        Preconditions.checkNotNull(map);
        return ImmutableMap.<K, V> builder().putAll(map).build();
    }

    /**
     * Creates an immutable singleton instance.
     *
     * @param key
     * @param value
     * @return
     */
    public static <K, V> Map<K, V> of(K key, V value) {
        return new ImmutableMapEntry<K, V>(key, value);
    }

    /**
     * Creates a new empty builder for building immutable map.
     *
     * @return a new empty builder
     */
    public static <K, V> Builder<K, V> builder() {
        return new HashMapBuilder<K, V>();
    }

    public interface Builder<K, V> {

        Builder<K, V> put(K key, V value);

        Builder<K, V> putAll(Map<K, V> items);

        Map<K, V> build();
    }

    private static class HashMapBuilder<K, V> implements Builder<K, V> {

        private static final int DEFAULT_INITIAL_CAPACITY = 4;
        private static final float LOAD_FACTOR = 1.2f;

        private Map<K, V> map;

        private HashMapBuilder() {
            this.map = new HashMap<K, V>(DEFAULT_INITIAL_CAPACITY, LOAD_FACTOR);
        }

        @Override
        public Builder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        @Override
        public Builder<K, V> putAll(Map<K, V> items) {
            map.putAll(items);
            return this;
        }

        @Override
        public Map<K, V> build() {
            if (map.isEmpty()) {
                return Collections.emptyMap();
            }
            if (map.size() == 1) {
                return new ImmutableMapEntry<K, V>(map.entrySet().iterator().next());
            }
            return Collections.unmodifiableMap(map);
        }
    }

}