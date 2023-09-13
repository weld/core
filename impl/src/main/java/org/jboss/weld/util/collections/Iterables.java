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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Static utility methods for {@link Iterable}.
 *
 * @author Martin Kouba
 */
public final class Iterables {

    private Iterables() {
    }

    /**
     * Add all elements in the iterable to the collection.
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
     * Combine the iterables into a single one.
     *
     * @param iterables
     * @return a single combined iterable
     */
    public static <T> Iterable<T> concat(final Iterable<? extends Iterable<? extends T>> iterables) {
        return () -> Iterators.concat(iterators(iterables));
    }

    /**
     * Combine the iterables into a single one.
     */
    public static <T> Iterable<T> concat(Iterable<? extends T> a, Iterable<? extends T> b) {
        return concat(Arrays.asList(a, b));
    }

    /**
     *
     * @param iterables
     * @return an iterator over iterators from the given iterable of iterables
     */
    public static <T> Iterator<Iterator<? extends T>> iterators(Iterable<? extends Iterable<? extends T>> iterables) {
        return Iterators.transform(iterables.iterator(), Iterable::iterator);
    }

    public static <T, R> Iterable<R> transform(Iterable<T> iterable, final Function<? super T, ? extends R> function) {
        return () -> new Iterators.TransformingIterator<>(iterable.iterator(), function);
    }

    public static <T, R> Iterable<R> flatMap(Iterable<T> iterable,
            Function<? super T, ? extends Iterable<? extends R>> function) {
        return concat(transform(iterable, function));
    }

    /**
     *
     * @param iterable
     * @return
     * @see WeldCollections#toMultiRowString(Collection)
     */
    public static <T> String toMultiRowString(Iterable<T> iterable) {
        StringBuilder builder = new StringBuilder("\n  - ");
        for (Iterator<T> iterator = iterable.iterator(); iterator.hasNext();) {
            T element = iterator.next();
            builder.append(element);
            if (iterator.hasNext()) {
                builder.append(",\n  - ");
            }
        }
        return builder.toString();
    }

}
