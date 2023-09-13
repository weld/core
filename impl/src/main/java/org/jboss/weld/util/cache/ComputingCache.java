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

import java.util.function.Consumer;

/**
 * A simple abstraction for computing cache.
 *
 * Implementations are not required to support recursive computations. A cache client must be aware that such computations may
 * result in livelocks, inifinite
 * loops and other undesired situations.
 *
 * @author Martin Kouba
 */
public interface ComputingCache<K, V> {

    /**
     *
     * @param key
     * @return the cache value
     */
    V getValue(K key);

    /**
     *
     * @param key
     * @return the cache value cast to the required type
     */
    <T> T getCastValue(Object key);

    /**
     *
     * @param key
     * @return the cache value if present (i.e. no computation is performed)
     */
    V getValueIfPresent(K key);

    /**
     *
     * @return the size of the cache
     */
    long size();

    /**
     * Remove all cache entries.
     */
    void clear();

    /**
     * Invalidate the entry with the given key. No-op if no such entry exists.
     *
     * @param key
     */
    void invalidate(Object key);

    /**
     *
     * @return an immutable map of entries
     */
    Iterable<V> getAllPresentValues();

    /**
     * Performs the given action for each cached value.
     *
     * @param consumer the given action
     */
    void forEachValue(Consumer<? super V> consumer);

}
