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

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingMap;

/**
 * Wrapper for a {@link Map} that provides default values. When the {@link #get(Object)} method is invoked and the underlying
 * map does not contain the given key, the default value is stored in the map under the given key. The new value is then
 * returned from the methods.
 *
 * This class is not thread-safe and needs to be externally synchronized. It is not thread safe even if the delegate map is.
 *
 * The semantics of {@link #keySet()}, {@link #containsKey(Object)}, {@link #containsValue(Object)} and {@link #values()} are
 * not modified by this wrapper.
 *
 * @author Jozef Hartinger
 *
 * @param <K>
 * @param <V>
 */
public class DefaultValueMap<K, V> extends ForwardingMap<K, V> {

    public static <K, V> DefaultValueMap<K, V> of(Map<K, V> delegate, Supplier<V> defaultValueSupplier) {
        return new DefaultValueMap<K, V>(delegate, defaultValueSupplier);
    }

    public static <K, V> DefaultValueMap<K, V> hashMapWithDefaultValue(Supplier<V> defaultValueSupplier) {
        return new DefaultValueMap<K, V>(new HashMap<K, V>(), defaultValueSupplier);
    }

    private final Map<K, V> delegate;
    private final Supplier<V> defaultValueSupplier;

    private DefaultValueMap(Map<K, V> delegate, Supplier<V> defaultValueSupplier) {
        this.delegate = delegate;
        this.defaultValueSupplier = defaultValueSupplier;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        V value = delegate.get(key);
        if (value == null) {
            delegate.put((K) key, defaultValueSupplier.get());
            value = delegate.get(key);
        }
        return value;
    }

    @Override
    protected Map<K, V> delegate() {
        return delegate;
    }
}
