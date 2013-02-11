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

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.TypeClosureLazyValueHolder;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.collections.WeldCollections;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

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

    private final LoadingCache<Set<?>, Set<?>> sharedSets = CacheBuilder.newBuilder().build(new CacheLoader<Set<?>, Set<?>>() {
        public Set<?> load(Set<?> from) {
            return WeldCollections.immutableSet(from);
        }
    });

    private final LoadingCache<Map<?, ?>, Map<?, ?>> sharedMaps = CacheBuilder.newBuilder().build(
            new CacheLoader<Map<?, ?>, Map<?, ?>>() {
                public Map<?, ?> load(Map<?, ?> from) {
            return WeldCollections.immutableMap(from);
        }
    });

    private final LoadingCache<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>> sharedMultiMaps = CacheBuilder.newBuilder()
            .build(new CacheLoader<ArraySetMultimap<?, ?>, ArraySetMultimap<?, ?>>() {
                public ArraySetMultimap<?, ?> load(ArraySetMultimap<?, ?> from) {
            return from;
        }
    });

    private final LoadingCache<Type, LazyValueHolder<Set<Type>>> typeClosureHolders = CacheBuilder.newBuilder().build(
            new CacheLoader<Type, LazyValueHolder<Set<Type>>>() {
        @Override
                public LazyValueHolder<Set<Type>> load(Type input) {
            return new TypeClosureLazyValueHolder(input);
        }
    });

    private final LoadingCache<Type, Type> resolvedTypes = CacheBuilder.newBuilder().build(new CacheLoader<Type, Type>() {

        public Type load(Type from) {
            return Types.getCanonicalType(from);
        }
    });

    public <T> Set<T> getSharedSet(Set<T> set) {
        return getCastCacheValue(sharedSets, set);
    }

    public <K, V> Map<K, V> getSharedMap(Map<K, V> map) {
        return getCastCacheValue(sharedMaps, map);
    }

    public <K, V> ArraySetMultimap<K, V> getSharedMultimap(ArraySetMultimap<K, V> map) {
        return getCastCacheValue(sharedMultiMaps, map);
    }

    public LazyValueHolder<Set<Type>> getTypeClosureHolder(Type type) {
        return getCacheValue(typeClosureHolders, type);
    }

    public Type getResolvedType(Type type) {
        return resolvedTypes.getUnchecked(type);
    }

    public void cleanupAfterBoot() {
        sharedSets.invalidateAll();
        sharedMaps.invalidateAll();
        sharedMultiMaps.invalidateAll();
        typeClosureHolders.invalidateAll();
    }

    public void cleanup() {
        cleanupAfterBoot();
        resolvedTypes.invalidateAll();
    }
}
