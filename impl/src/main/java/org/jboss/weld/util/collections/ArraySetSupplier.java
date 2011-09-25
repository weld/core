/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import com.google.common.base.Supplier;
import com.google.common.collect.SetMultimap;

import java.util.Set;

/**
 * Provides new instances of {@link ArraySet} to Google collections.
 *
 * @author David Allen
 */
public class ArraySetSupplier<V> implements Supplier<Set<V>> {
    private static final Supplier<?> INSTANCE = new ArraySetSupplier<Object>();

    private ArraySetSupplier() {
    }

    @SuppressWarnings("unchecked")
    public static <V> Supplier<Set<V>> instance() {
        return (Supplier<Set<V>>) INSTANCE;
    }

    public Set<V> get() {
        return new ArraySet<V>();
    }

    /**
     * Helper method which will trim each set in the multimap to its current size.
     *
     * @param <K>      Key type
     * @param <V>      Value type
     * @param multimap the set multimap using ArraySet<V> as the values
     */
    public static <K, V> void trimSetsToSize(SetMultimap<K, V> multimap) {
        for (K key : multimap.keySet()) {
            if (multimap.get(key) instanceof ArraySet<?>) {
                ((ArraySet<?>) multimap.get(key)).trimToSize();
            }
        }
    }
}
