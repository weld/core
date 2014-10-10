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
import java.util.function.Function;

import org.jboss.weld.util.ValueHolder;

/**
 * A builder for {@link ComputingCache} instances.
 *
 * @author Martin Kouba
 * @author Jozef Hartinger
 */
public final class ComputingCacheBuilder {

    private Long maxSize;

    private boolean weakValues;

    private ComputingCacheBuilder() {
    }

    /**
     *
     * @return a new builder instance
     */
    public static ComputingCacheBuilder newBuilder() {
        return new ComputingCacheBuilder();
    }

    /**
     *
     * @param maxSize
     * @return self
     */
    public ComputingCacheBuilder setMaxSize(long maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Values stored in the cache should be wrapped in a {@link WeakReference}.
     *
     * @return self
     */
    public ComputingCacheBuilder setWeakValues() {
        this.weakValues = true;
        return this;
    }

    /**
     *
     * @param computingFunction
     * @return a new ComputingCache instance
     */
    public <K, V> ComputingCache<K, V> build(Function<K, V> computingFunction) {
        if (weakValues) {
            return new WeakValueMapBackedComputingCache<K, V>(maxSize, computingFunction);
        }
        return new MapBackedComputingCache<>(maxSize, computingFunction);
    }

    /**
     * Builds a {@link ComputingCache} that works around the {@link Map#computeIfAbsent(Object, Function)} reentrancy
     * problem.
     * <p>
     * {@link Map#computeIfAbsent(Object, Function)} is not reentrant i.e. it cannot be called from within other
     * computeIfAbsent call. Reentrancy is however often useful when, for example, traversing class hierarchies.
     * </p>
     * <p>
     * We work around this limitation by creating and returning {@link ValueHolder} instance which computes the value lazily.
     * Since the value is computed outside of {@link Map#computeIfAbsent(Object, Function)}, we do not hit the reentrancy
     * problem. At the same time both creating new {@link ValueHolder} and performing the computation are guaranteed to be done
     * only once for a given key.
     * </p>
     *
     * @param computingFunction the given computing function
     * @return a new ComputingCache instance
     */
    public <K, V> ComputingCache<K, V> buildReentrant(Function<K, V> computingFunction) {
        if (weakValues) {
            throw new IllegalStateException("Combining reentrant cache with weak values is not supported");
        }
        return new ReentrantMapBackedComputingCache<>(computingFunction, maxSize);
    }

}
