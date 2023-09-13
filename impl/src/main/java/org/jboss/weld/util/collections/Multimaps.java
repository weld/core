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

import java.io.Serializable;
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

    public static final Multimap<Object, Object> EMPTY_MULTIMAP = new EmptyMultimap<Object, Object>();

    private Multimaps() {
    }

    /**
     * Note that {@link Multimap#get(Object)} always returns unmodifiable collections. Moreover, it does not trigger
     * initialization of a new value collection
     * (i.e. when no collection of values for a given key exists).
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
     * @return an immutable empty multimap
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Multimap<K, V> emptyMultimap() {
        return (Multimap<K, V>) EMPTY_MULTIMAP;
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

        /**
         * Returns unmodifiable collections. Moreover, it does not trigger initialization of a new value collection (i.e. when
         * no collection of values for a
         * given key exists).
         */
        @Override
        public Collection<V> get(K key) {
            if (delegate.containsKey(key)) {
                return unmodifiableValueCollection(delegate.get(key));
            }
            return Collections.emptyList();
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

    static class EmptyMultimap<K, V> implements Multimap<K, V>, Serializable {

        private static final long serialVersionUID = -7386055586552408792L;

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Collection<V> get(K key) {
            return Collections.emptyList();
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public Set<K> keySet() {
            return Collections.emptySet();
        }

        @Override
        public List<V> values() {
            return Collections.emptyList();
        }

        @Override
        public Set<V> uniqueValues() {
            return Collections.emptySet();
        }

        @Override
        public Set<Entry<K, Collection<V>>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public boolean put(K key, V value) {
            throw new UnsupportedOperationException();
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
        public void clear() {
            throw new UnsupportedOperationException();
        }

        public boolean equals(Object o) {
            return (o instanceof Multimap) && ((Multimap<?, ?>) o).isEmpty();
        }

        public int hashCode() {
            return 1;
        }

        private Object readResolve() {
            return EMPTY_MULTIMAP;
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
