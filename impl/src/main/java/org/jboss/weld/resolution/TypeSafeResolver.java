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
package org.jboss.weld.resolution;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of type safe bean resolution
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 */
public abstract class TypeSafeResolver<R extends Resolvable, T> {

    private static class ResolvableToBeanSet<R extends Resolvable, T> implements Function<R, Set<T>> {

        private final TypeSafeResolver<R, T> resolver;

        private ResolvableToBeanSet(TypeSafeResolver<R, T> resolver) {
            this.resolver = resolver;
        }

        public Set<T> apply(R from) {
            return resolver.sortResult(resolver.filterResult(resolver.findMatching(from)));
        }

    }

    // The resolved injection points
    private final ConcurrentMap<R, Set<T>> resolved;
    // The beans to search
    private final Iterable<? extends T> allBeans;
    private final ResolvableToBeanSet<R, T> resolverFunction;


    /**
     * Constructor
     */
    public TypeSafeResolver(Iterable<? extends T> allBeans) {
        this.resolverFunction = new ResolvableToBeanSet<R, T>(this);
        this.resolved = new MapMaker().makeComputingMap(resolverFunction);
        this.allBeans = allBeans;
    }

    /**
     * Reset all cached resolutions
     */
    public void clear() {
        this.resolved.clear();
    }

    /**
     * Get the possible beans for the given element
     *
     * @param resolvable The resolving criteria
     * @return An unmodifiable set of matching beans
     */
    public Set<T> resolve(R resolvable, boolean cache) {
        R wrappedResolvable = wrap(resolvable);
        if (cache) {
            return resolved.get(wrappedResolvable);
        } else {
            return resolverFunction.apply(wrappedResolvable);
        }
    }

    /**
     * Gets the matching beans for binding criteria from a list of beans
     *
     * @param <T>     The type of the beans
     * @param element The binding criteria
     * @param beans   The beans to filter
     * @return A set of filtered beans
     */
    private Set<T> findMatching(R resolvable) {
        Set<T> result = new HashSet<T>();
        for (T bean : getAllBeans(resolvable)) {
            if (matches(resolvable, bean)) {
                result.add(bean);
            }
        }
        return ImmutableSet.copyOf(result);
    }

    protected Iterable<? extends T> getAllBeans(R resolvable) {
        return allBeans;
    }

    protected Iterable<? extends T> getAllBeans() {
        return allBeans;
    }

    protected abstract Set<T> filterResult(Set<T> matched);

    protected abstract Set<T> sortResult(Set<T> matched);

    protected abstract boolean matches(R resolvable, T t);

    /**
     * allows subclasses to wrap a resolvable before it is resolved
     */
    protected R wrap(R resolvable) {
        return resolvable;
    }

    public boolean isCached(R resolvable) {
        return resolved.containsKey(wrap(resolvable));
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Resolver\n");
        buffer.append("Resolved injection points: " + resolved.size() + "\n");
        return buffer.toString();
    }

}
