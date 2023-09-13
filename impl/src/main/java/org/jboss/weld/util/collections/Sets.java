/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.Preconditions.checkArgumentNotNull;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Static utility methods for {@link Set}.
 *
 * @author Martin Kouba
 */
public final class Sets {

    private Sets() {
    }

    /**
     *
     * @param elements
     * @return
     */
    @SafeVarargs
    public static <E> HashSet<E> newHashSet(E... elements) {
        HashSet<E> set = new HashSet<>(elements.length);
        Collections.addAll(set, elements);
        return set;
    }

    /**
     * This is a replacement for Guava's Sets#union(Set, Set).
     *
     * @param set1
     * @param set2
     * @return an unmodifiable view of the union of two sets
     */
    public static <E> Set<E> union(final Set<? extends E> set1, final Set<? extends E> set2) {
        checkArgumentNotNull(set1, "set1");
        checkArgumentNotNull(set2, "set2");

        // Set 2 minus set 1
        final Set<E> difference = new HashSet<>(set2);
        difference.removeAll(set1);
        // Following stream does not compile on some JDKs
        // See also https://bugs.openjdk.java.net/browse/JDK-8051443
        // set2.stream().filter((e) -> !set1.contains(e)).collect(Collectors.toSet());

        return new AbstractSet<E>() {

            @Override
            public Iterator<E> iterator() {
                final Iterator<E> iterator = Iterators
                        .concat(ImmutableList.of(set1.iterator(), difference.iterator()).iterator());
                // Remove operation is not supported by default
                return new Iterator<E>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public E next() {
                        return iterator.next();
                    }
                };
            }

            @Override
            public int size() {
                return set1.size() + difference.size();
            }

            @Override
            public boolean isEmpty() {
                return set1.isEmpty() && difference.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return set1.contains(o) || difference.contains(o);
            }
        };
    }

}
