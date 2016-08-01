/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Multimap utilities.
 *
 * @author Jozef Hartinger
 *
 */
public class Multimaps {

    private Multimaps() {
    }

    /**
     * Note that {@link Multimap#get(Object)} returns unmodifiable collections.
     *
     * @param multimap
     * @return an unmodifiable view of the given multimap
     */
    public static <K, V> Multimap<K, V> unmodifiableMultimap(Multimap<K, V> multimap) {
        if (multimap instanceof UnmodifiableMultimap) {
            return multimap;
        }
        return new UnmodifiableMultimap<K, V>(multimap);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <K>
     * @param <V>
     */
    static class UnmodifiableMultimap<K, V> implements Multimap<K, V> {

        private final Multimap<K, V> delegate;

        /**
         *
         * @param delegate
         */
        public UnmodifiableMultimap(Multimap<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Collection<V> get(K key) {
            return unmodifiableValueCollection(delegate.get(key));
        }

        @Override
        public boolean put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<V> values() {
            return delegate.values();
        }

        @Override
        public Set<V> uniqueValues() {
            return delegate.uniqueValues();
        }

        @Override
        public boolean putAll(K key, Collection<? extends V> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<V> replaceValues(K key, Iterable<? extends V> values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Entry<K, Collection<V>>> entrySet() {
            return delegate.entrySet();
        }

    }

    static <V> Collection<V> unmodifiableValueCollection(Collection<V> values) {
        if (values instanceof Set) {
            return Collections.unmodifiableSet((Set<V>) values);
        } else if (values instanceof List) {
            return Collections.unmodifiableList((List<V>) values);
        }
        return Collections.unmodifiableCollection(values);
    }

}