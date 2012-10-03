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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.TypeClosureLazyValueHolder;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.collections.WeldCollections;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Allows classes to share Maps/Sets to conserve memory.
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * @author Jozef Hartinger
 */
public class SharedObjectCache implements BootstrapService {

    public static SharedObjectCache instance(BeanManagerImpl manager) {
        return manager.getServices().get(SharedObjectCache.class);
    }

    private final Map<Set<?>, Set<?>> sharedSets = new MapMaker().makeComputingMap(new Function<Set<?>, Set<?>>() {
        public Set<?> apply(Set<?> from) {
            return WeldCollections.immutableSet(from);
        }
    });

    private final Map<Map<?, ?>, Map<?, ?>> sharedMaps = new MapMaker().makeComputingMap(new Function<Map<?, ?>, Map<?, ?>>() {
        public Map<?, ?> apply(Map<?, ?> from) {
            return WeldCollections.immutableMap(from);
        }
    });

    private final Map<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>> sharedMultiMaps = new MapMaker().makeComputingMap(new Function<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>>() {
        public ArraySetMultimap<?, ?> apply(ArraySetMultimap<?, ?> from) {
            return from;
        }
    });

    private final Map<Type, LazyValueHolder<Set<Type>>> typeClosureHolders = new MapMaker().makeComputingMap(new Function<Type, LazyValueHolder<Set<Type>>>() {
        @Override
        public LazyValueHolder<Set<Type>> apply(Type input) {
            return new TypeClosureLazyValueHolder(input);
        }
    });

    private final Map<Type, Type> resolvedTypes = new MapMaker().makeComputingMap(new Function<Type, Type>() {

        public Type apply(Type from) {
            return new HierarchyDiscovery(from).getResolvedType();
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

    public LazyValueHolder<Set<Type>> getTypeClosureHolder(Type type) {
        return typeClosureHolders.get(type);
    }

    public Type getResolvedType(Type type) {
        return resolvedTypes.get(type);
    }

    public void cleanupAfterBoot() {
        sharedSets.clear();
        sharedMaps.clear();
        sharedMultiMaps.clear();
        typeClosureHolders.clear();
    }

    public void cleanup() {
        cleanupAfterBoot();
        resolvedTypes.clear();
    }
}
