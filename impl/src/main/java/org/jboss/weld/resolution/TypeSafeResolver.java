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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Implementation of type safe bean resolution
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 * @author Ales Justin
 */
public abstract class TypeSafeResolver<R extends Resolvable, T, C extends Collection<T>, F> {

    private static class ResolvableToBeanCollection<R extends Resolvable, T, C extends Collection<T>, F>
            implements Function<R, F> {

        private final TypeSafeResolver<R, T, C, F> resolver;

        private ResolvableToBeanCollection(TypeSafeResolver<R, T, C, F> resolver) {
            this.resolver = resolver;
        }

        public F apply(R from) {
            return resolver.makeResultImmutable(resolver.sortResult(resolver.filterResult(resolver.findMatching(from))));
        }

    }

    // The resolved injection points
    private final ComputingCache<R, F> resolved;
    // The beans to search
    private final Iterable<? extends T> allBeans;
    private final ResolvableToBeanCollection<R, T, C, F> resolverFunction;

    /**
     * Constructor
     */
    public TypeSafeResolver(Iterable<? extends T> allBeans, WeldConfiguration configuration) {
        this.resolverFunction = new ResolvableToBeanCollection<R, T, C, F>(this);
        this.resolved = ComputingCacheBuilder.newBuilder()
                .setMaxSize(configuration.getLongProperty(ConfigurationKey.RESOLUTION_CACHE_SIZE)).build(resolverFunction);
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
    public F resolve(R resolvable, boolean cache) {
        R wrappedResolvable = wrap(resolvable);
        if (cache) {
            return resolved.getValue(wrappedResolvable);
        } else {
            return resolverFunction.apply(wrappedResolvable);
        }
    }

    /**
     * Gets the matching beans for binding criteria from a list of beans
     *
     * @param resolvable the resolvable
     * @return A set of filtered beans
     */
    private Set<T> findMatching(R resolvable) {
        Set<T> result = new HashSet<T>();
        for (T bean : getAllBeans(resolvable)) {
            if (matches(resolvable, bean)) {
                result.add(bean);
            }
        }
        return result;
    }

    protected Iterable<? extends T> getAllBeans(R resolvable) {
        return allBeans;
    }

    protected Iterable<? extends T> getAllBeans() {
        return allBeans;
    }

    protected abstract Set<T> filterResult(Set<T> matched);

    protected abstract C sortResult(Set<T> matched);

    protected abstract boolean matches(R resolvable, T t);

    protected F makeResultImmutable(C result) {
        if (result instanceof List<?>) {
            return cast(ImmutableList.copyOf(((List<?>) result)));
        }
        if (result instanceof Set<?>) {
            return cast(ImmutableSet.copyOf((Set<?>) result));
        }
        throw new IllegalArgumentException("Unable to make result immutable");
    }

    /**
     * allows subclasses to wrap a resolvable before it is resolved
     */
    protected R wrap(R resolvable) {
        return resolvable;
    }

    public boolean isCached(R resolvable) {
        return resolved.getValueIfPresent(wrap(resolvable)) != null;
    }

    /**
     * Gets a string representation
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resolver\n");
        sb.append("Resolved injection points: ").append(resolved.size()).append('\n');
        return sb.toString();
    }
}
