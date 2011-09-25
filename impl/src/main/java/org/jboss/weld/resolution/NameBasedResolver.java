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
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

import javax.enterprise.inject.spi.Bean;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementation of name based bean resolution
 *
 * @author Pete Muir
 */
public class NameBasedResolver {

    private static class NameToBeanSet implements Function<String, Set<Bean<?>>> {

        private final BeanManagerImpl beanManager;
        private final Iterable<? extends Bean<?>> allBeans;

        private NameToBeanSet(BeanManagerImpl beanManager, Iterable<? extends Bean<?>> allBeans) {
            this.beanManager = beanManager;
            this.allBeans = allBeans;
        }

        public Set<Bean<?>> apply(String from) {
            Set<Bean<?>> matchedBeans = new HashSet<Bean<?>>();
            for (Bean<?> bean : allBeans) {
                if ((bean.getName() == null && from == null) || (bean.getName() != null && bean.getName().equals(from))) {
                    matchedBeans.add(bean);
                }
            }
            return ImmutableSet.copyOf(Beans.removeDisabledAndSpecializedBeans(matchedBeans, beanManager));
        }

    }

    // The resolved names
    private ConcurrentMap<String, Set<Bean<?>>> resolvedNames;

    /**
     * Constructor
     */
    public NameBasedResolver(BeanManagerImpl manager, Iterable<? extends Bean<?>> allBeans) {
        this.resolvedNames = new MapMaker().makeComputingMap(new NameToBeanSet(manager, allBeans));
    }

    /**
     * Reset all cached injection points. You must reset all cached injection
     * points when you add a bean to the manager
     */
    public void clear() {
        this.resolvedNames.clear();
    }

    /**
     * Get the possible beans for the given name
     *
     * @param name The name to match
     * @return The set of matching beans
     */
    public Set<Bean<?>> resolve(final String name) {
        return resolvedNames.get(name);
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
