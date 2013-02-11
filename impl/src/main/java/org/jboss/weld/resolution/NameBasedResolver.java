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

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.bootstrap.SpecializationAndEnablementRegistry;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;

/**
 * Implementation of name based bean resolution
 *
 * @author Pete Muir
 */
public class NameBasedResolver {

    private static class NameToBeanSet extends CacheLoader<String, Set<Bean<?>>> {

        private final BeanManagerImpl beanManager;
        private final Iterable<? extends Bean<?>> allBeans;
        private final SpecializationAndEnablementRegistry registry;

        private NameToBeanSet(BeanManagerImpl beanManager, Iterable<? extends Bean<?>> allBeans) {
            this.beanManager = beanManager;
            this.allBeans = allBeans;
            this.registry = beanManager.getServices().get(SpecializationAndEnablementRegistry.class);
        }

        public Set<Bean<?>> load(String from) {
            Set<Bean<?>> matchedBeans = new HashSet<Bean<?>>();
            for (Bean<?> bean : allBeans) {
                if ((bean.getName() == null && from == null) || (bean.getName() != null && bean.getName().equals(from))) {
                    matchedBeans.add(bean);
                }
            }
            //noinspection unchecked
            return ImmutableSet.copyOf((Iterable<Bean<?>>) Beans.removeDisabledAndSpecializedBeans(matchedBeans, beanManager, registry));
        }

    }

    // The resolved names
    private LoadingCache<String, Set<Bean<?>>> resolvedNames;

    /**
     * Constructor
     */
    public NameBasedResolver(BeanManagerImpl manager, Iterable<? extends Bean<?>> allBeans) {
        this.resolvedNames = CacheBuilder.newBuilder().build(new NameToBeanSet(manager, allBeans));
    }

    /**
     * Reset all cached injection points. You must reset all cached injection
     * points when you add a bean to the manager
     */
    public void clear() {
        this.resolvedNames.invalidateAll();
    }

    /**
     * Get the possible beans for the given name
     *
     * @param name The name to match
     * @return The set of matching beans
     */
    public Set<Bean<?>> resolve(final String name) {
        return getCacheValue(resolvedNames, name);
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
        buffer.append("Resolved names points: " + resolvedNames.size() + "\n");
        return buffer.toString();
    }

}
