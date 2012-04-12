/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resources;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Allows classes to share Maps/Sets to conserve memory.
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class SharedObjectCache implements Service {
    private final Map<Set<?>, Set<?>> sharedSets = new MapMaker().makeComputingMap(new Function<Set<?>, Set<?>>() {
        public Set<?> apply(Set<?> from) {
            if (from instanceof ImmutableSet<?>) {
                return from;
            } else {
                return Collections.unmodifiableSet(from);
            }
        }
    });

    private final Map<Map<?, ?>, Map<?, ?>> sharedMaps = new MapMaker().makeComputingMap(new Function<Map<?, ?>, Map<?, ?>>() {
        public Map<?, ?> apply(Map<?, ?> from) {
            return Collections.unmodifiableMap(from);
        }
    });

    private final Map<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>> sharedMultiMaps = new MapMaker().makeComputingMap(new Function<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>>() {
        public ArraySetMultimap<?, ?> apply(ArraySetMultimap<?, ?> from) {
            return from;
        }
    });

    private final Map<Type, Set<Type>> typeClosures = new MapMaker().makeComputingMap(new Function<Type, Set<Type>>() {

        public Set<Type> apply(Type from) {
            return Collections.unmodifiableSet(new HierarchyDiscovery(from).getTypeClosure());
        }
    });

    public <T> Set<T> getSharedSet(Set<T> set) {
        return Reflections.cast(sharedSets.get(set));
    }

    public <K, V> Map<K, V> getSharedMap(Map<K, V> map) {
        return Reflections.cast(sharedMaps.get(map));
    }

    public <K, V> ArraySetMultimap<K, V> getSharedMultimap(ArraySetMultimap<K, V> map) {
        return Reflections.cast(sharedMultiMaps.get(map));
    }

    public Set<Type> getTypeClosure(Type type) {
        return typeClosures.get(type);
    }

    public void cleanup() {
        sharedSets.clear();
        sharedMaps.clear();
        sharedMultiMaps.clear();
        typeClosures.clear();
    }
}
