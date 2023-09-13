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
import java.util.function.Function;

import org.jboss.weld.annotated.enhanced.TypeClosureLazyValueHolder;
import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.collections.ImmutableSet;

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

    private final ComputingCache<Set<?>, Set<?>> sharedSets = ComputingCacheBuilder.newBuilder()
            .build(new Function<Set<?>, Set<?>>() {
                @Override
                public Set<?> apply(Set<?> from) {
                    return ImmutableSet.copyOf(from);
                }
            });

    private final ComputingCache<Map<?, ?>, Map<?, ?>> sharedMaps = ComputingCacheBuilder.newBuilder().build(
            new Function<Map<?, ?>, Map<?, ?>>() {
                @Override
                public Map<?, ?> apply(Map<?, ?> from) {
                    return ImmutableMap.copyOf(from);
                }
            });

    private final ComputingCache<Type, LazyValueHolder<Set<Type>>> typeClosureHolders = ComputingCacheBuilder.newBuilder()
            .build(
                    new Function<Type, LazyValueHolder<Set<Type>>>() {
                        @Override
                        public LazyValueHolder<Set<Type>> apply(Type input) {
                            return new TypeClosureLazyValueHolder(input);
                        }
                    });

    public <T> Set<T> getSharedSet(Set<T> set) {
        return sharedSets.getCastValue(set);
    }

    public <K, V> Map<K, V> getSharedMap(Map<K, V> map) {
        return sharedMaps.getCastValue(map);
    }

    public LazyValueHolder<Set<Type>> getTypeClosureHolder(Type type) {
        return typeClosureHolders.getCastValue(type);
    }

    @Override
    public void cleanupAfterBoot() {
        sharedSets.clear();
        sharedMaps.clear();
        typeClosureHolders.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
    }
}
