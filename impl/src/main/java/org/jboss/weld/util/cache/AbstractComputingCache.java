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

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 *
 * @author Martin Kouba
 */
abstract class AbstractComputingCache<K, V> implements ComputingCache<K, V> {

    protected final Long maxSize;

    /**
     *
     * @param maxSize
     */
    AbstractComputingCache(Long maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public V getValue(K key) {
        try {
            return computeIfNeeded(key);
        } catch (MaxSizeExceededException e) {
            synchronized (this) {
                if (size() > maxSize) {
                    clear();
                }
            }
            return computeIfNeeded(key);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCastValue(Object key) {
        return (T) getValue((K) key);
    }

    @Override
    public long size() {
        return getMap().size();
    }

    @Override
    public void clear() {
        getMap().clear();
    }

    @Override
    public void invalidate(Object key) {
        getMap().remove(key);
    }

    /**
     *
     * @param key
     * @return the value for the given key
     */
    protected abstract V computeIfNeeded(K key);

    /**
     * @return the underlying map
     */
    protected abstract ConcurrentMap<K, ?> getMap();

    /**
     * A default computing function wrapper which performs max size limit check during computation.
     *
     * @author Martin Kouba
     *
     * @param <K>
     * @param <V>
     */
    static class DefaultFunctionWrapper<K, V> extends FunctionWrapper<K, V> implements Function<K, V> {

        /**
         *
         * @param computingFunction
         * @param cache
         */
        public DefaultFunctionWrapper(Function<K, V> computingFunction, AbstractComputingCache<K, V> cache) {
            super(computingFunction, cache);
        }

        @Override
        public V apply(K key) {
            checkMaxSize();
            return computingFunction.apply(key);
        }

    }

    abstract static class FunctionWrapper<K, V> {

        protected final Function<K, V> computingFunction;

        protected final AbstractComputingCache<K, V> cache;

        /**
         *
         * @param computingFunction
         * @param cache
         */
        FunctionWrapper(Function<K, V> computingFunction, AbstractComputingCache<K, V> cache) {
            this.computingFunction = computingFunction;
            this.cache = cache;
        }

        /**
         * @throws MaxSizeExceededException If the limit exceeded
         */
        protected void checkMaxSize() {
            if (cache.maxSize != null && cache.size() > cache.maxSize) {
                throw new MaxSizeExceededException();
            }
        }

    }

    /**
     *
     * @author Martin Kouba
     */
    static class MaxSizeExceededException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

}
