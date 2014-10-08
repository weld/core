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

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Martin Kouba
 */
public final class Iterables {

    private Iterables() {
    }

    /**
     *
     * @param target
     * @param iterable
     * @return true if the target was modified, false otherwise
     */
    public static <T> boolean addAll(Collection<T> target, Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return target.addAll((Collection<? extends T>) iterable);
        }
        return Iterators.addAll(target, iterable.iterator());
    }

    /**
     *
     * @param iterables
     * @return a single combined iterable
     */
    public static <T> Iterable<T> concat(final Iterable<? extends Iterable<? extends T>> iterables) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.concat(iterators(iterables));
            }
        };
    }

    /**
     *
     * @param iterables
     * @return an iterator over iterators from the given iterable of iterables
     */
    public static <T> Iterator<Iterator<? extends T>> iterators(Iterable<? extends Iterable<? extends T>> iterables) {
        final Iterator<? extends Iterable<? extends T>> iterator = iterables.iterator();
        return new Iterator<Iterator<? extends T>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }
            @Override
            public Iterator<? extends T> next() {
                return iterator.next().iterator();
            }
        };
    }

}
