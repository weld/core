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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.util.Preconditions;

/**
 * Immutable map entry implementation. At the same time this implementation serves as a singleton Map implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <K> the key type
 * @param <V> the value type
 */
class ImmutableMapEntry<K, V> extends ImmutableMap<K, V> implements Entry<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private final K key;
    private final V value;

    private transient volatile Set<Entry<K, V>> entrySet;
    private transient volatile Set<K> keySet;
    private transient volatile Collection<V> values;

    ImmutableMapEntry(Entry<K, V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    ImmutableMapEntry(K key, V value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);
        this.key = key;
        this.value = value;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new ImmutableTinySet.Singleton<>(this);
        }
        return entrySet;
    }

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new ImmutableTinySet.Singleton<>(key);
        }
        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new ImmutableTinySet.Singleton<>(value);
        }
        return values;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Entry<?, ?>) {
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return key.equals(entry.getKey()) && value.equals(entry.getValue());
        }
        if (o instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) o;
            if (map.size() != 1) {
                return false;
            }
            return value.equals(map.get(key));
        }
        return false;
    }

    public int hashCode() {
        return key.hashCode() ^ value.hashCode();
    }

    @Override
    @SuppressWarnings("checkstyle:multiplestringliterals")
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key == this ? "(this Map)" : key);
        sb.append('=');
        sb.append(value == this ? "(this Map)" : value);
        sb.append('}');
        return sb.toString();
    }
}
