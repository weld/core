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
import java.util.function.Function;

import org.jboss.weld.util.WeakLazyValueHolder;

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
            return new ReentrantMapBackedComputingCache<>(computingFunction, WeakLazyValueHolder::forSupplier, maxSize);
        }
        return new ReentrantMapBackedComputingCache<>(computingFunction, maxSize);
    }
}
