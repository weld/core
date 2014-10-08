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

import java.util.function.Function;

/**
 * An abstract {@link ComputingCache} which may remove all cache entries if the max size limit exceeded. Subclasses should always perform the max size check
 * during computation and throw {@link MaxSizeExceededException} if appropriate.
 *
 * @author Martin Kouba
 */
abstract class AbstractComputingCache<K, V> implements ComputingCache<K, V> {

    private final Long maxSize;

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

    /**
     *
     * @return the max size limit
     */
    protected Long getMaxSize() {
        return maxSize;
    }

    /**
     *
     * @param key
     * @return the value for the given key
     */
    protected abstract V computeIfNeeded(K key);

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

    /**
     * An abstract computing function wrapper.
     *
     * @author Martin Kouba
     *
     * @param <K>
     * @param <V>
     */
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
     * This exception should be thrown when the max size limit exceeded.
     *
     * @author Martin Kouba
     */
    static class MaxSizeExceededException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

}
