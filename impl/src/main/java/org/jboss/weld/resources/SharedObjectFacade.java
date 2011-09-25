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

import org.jboss.weld.Container;
import org.jboss.weld.util.collections.ArraySetMultimap;
import org.jboss.weld.util.reflection.HierarchyDiscovery;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * Convenience methods to access the shared object cache
 *
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 */
public class SharedObjectFacade {

    private SharedObjectFacade() {

    }

    public static <T> Set<T> wrap(String contextId, Set<T> set) {
        SharedObjectCache cache = getSharedObjectCache(contextId);
        if (cache != null) {
            return cache.getSharedSet(set);
        }
        return set;
    }

    public static <K, V> Map<K, V> wrap(String contextId, Map<K, V> map) {
        SharedObjectCache cache = getSharedObjectCache(contextId);
        if (cache != null) {
            return Container.instance(contextId).services().get(SharedObjectCache.class).getSharedMap(map);
        }
        return map;
    }

    public static <K, V> ArraySetMultimap<K, V> wrap(String contextId, ArraySetMultimap<K, V> map) {
        SharedObjectCache cache = getSharedObjectCache(contextId);
        if (cache != null) {
            return Container.instance(contextId).services().get(SharedObjectCache.class).getSharedMultimap(map);
        }
        return map;
    }

    public static Set<Type> getTypeClosure(String contextId, Type type) {
        SharedObjectCache cache = getSharedObjectCache(contextId);
        if (cache != null) {
            return Container.instance(contextId).services().get(SharedObjectCache.class).getTypeClosure(type);
        }
        return new HierarchyDiscovery(type).getTypeClosure();
    }

    // this may return null in a test environment
    private static SharedObjectCache getSharedObjectCache(String contextId) {
        try {
            return Container.instance(contextId).services().get(SharedObjectCache.class);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
