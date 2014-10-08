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
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.jboss.weld.util.Preconditions;

/**
 *
 * @author Martin Kouba
 */
public final class Iterators {

    private Iterators() {
    }

    /**
     *
     * @param target
     * @param iterator
     * @return true if the target was modified, false otherwise
     */
    public static <T> boolean addAll(Collection<T> target, Iterator<? extends T> iterator) {
        Preconditions.checkArgumentNotNull(target, "target");
        boolean modified = false;
        while (iterator.hasNext()) {
            modified |= target.add(iterator.next());
        }
        return modified;
    }

    /**
     *
     * @param iterators An iterator of iterators
     * @return a single combined iterator
     */
    public static <T> Iterator<T> concat(Iterator<? extends Iterator<? extends T>> iterators) {
        Preconditions.checkArgumentNotNull(iterators, "iterators");
        return new CombinedIterator<T>(iterators);
    }

    /**
     *
     * @param iterator
     * @param function
     * @return an iterator that applies the given function to each element of the original iterator
     */
    public static <T, R> Iterator<R> transform(final Iterator<T> iterator, final Function<? super T, ? extends R> function) {
        Preconditions.checkArgumentNotNull(iterator, "iterator");
        Preconditions.checkArgumentNotNull(function, "function");
        return new TransformingIterator<T, R>(iterator) {
            @Override
            R transform(T from) {
                return function.apply(from);
            }
        };
    }

    /**
     *
     * @return an empty iterator
     */
    public static <T> Iterator<T> emptyIterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new IllegalStateException();
            }
        };
    }

    /**
     *
     *
     * @param <E>
     */
    private static class CombinedIterator<E> implements Iterator<E> {

        private final Iterator<? extends Iterator<? extends E>> iterators;

        private Iterator<? extends E> current;

        private Iterator<? extends E> removeFrom;

        CombinedIterator(Iterator<? extends Iterator<? extends E>> iterators) {
            this.iterators = iterators;
            this.current = emptyIterator();
        }

        @Override
        public boolean hasNext() {
            boolean currentHasNext;
            while (!(currentHasNext = current.hasNext()) && iterators.hasNext()) {
                current = iterators.next();
            }
            return currentHasNext;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            removeFrom = current;
            return current.next();
        }

        @Override
        public void remove() {
            if (removeFrom == null) {
                throw new IllegalStateException();
            }
            removeFrom.remove();
            removeFrom = null;
        }

    }

    /**
     *
     * @param <T> The original type
     * @param <R> The result type
     */
    private abstract static class TransformingIterator<T, R> implements Iterator<R> {

        final Iterator<? extends T> iterator;

        TransformingIterator(Iterator<? extends T> iterator) {
            this.iterator = iterator;
        }

        abstract R transform(T from);

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final R next() {
            return transform(iterator.next());
        }

        @Override
        public final void remove() {
            iterator.remove();
        }
    }

}
