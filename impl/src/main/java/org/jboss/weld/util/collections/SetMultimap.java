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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A {@link Multimap} whose collections of values are backed by a {@link Set}.
 *
 * @author Martin Kouba
 *
 * @param <K> The key type
 * @param <V> The value type
 */
public class SetMultimap<K, V> extends AbstractMultimap<K, V, Set<V>> {

    private static final long serialVersionUID = -7310409235342796148L;

    /**
     * Creates a new instance backed by a {@link HashMap} and {@link HashSet}.
     */
    public static <K, V> SetMultimap<K, V> newSetMultimap() {
        return new SetMultimap<K, V>(HashMap::new, HashSet::new, null);
    }

    /**
     * Creates a new instance backed by a {@link HashMap} and {@link HashSet}. All key-value mappings are copied from the input
     * multimap. If any
     * collection of values in the input multimap contains duplicate elements, these are removed in the constructed multimap.
     *
     * @param multimap
     */
    public static <K, V> SetMultimap<K, V> newSetMultimap(Multimap<K, V> multimap) {
        return new SetMultimap<K, V>(HashMap::new, HashSet::new, multimap);
    }

    /**
     * Creates a new instance backed by a {@link ConcurrentHashMap} and synchronized {@link HashSet}.
     */
    public static <K, V> SetMultimap<K, V> newConcurrentSetMultimap() {
        return newConcurrentSetMultimap(() -> Collections.synchronizedSet(new HashSet<>()));
    }

    /**
     * Creates a new instance backed by a {@link ConcurrentHashMap} and synchronized {@link HashSet}.
     */
    public static <K, V> SetMultimap<K, V> newConcurrentSetMultimap(Supplier<Set<V>> valueSupplier) {
        return new SetMultimap<K, V>(ConcurrentHashMap::new, valueSupplier, null);
    }

    /**
     *
     * @param mapSupplier
     * @param collectionSupplier
     * @param multimap
     */
    private SetMultimap(Supplier<Map<K, Set<V>>> mapSupplier, Supplier<Set<V>> collectionSupplier, Multimap<K, V> multimap) {
        super(mapSupplier, collectionSupplier, multimap);
    }

}
