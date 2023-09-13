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
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.util.Preconditions;

/**
 * Static utility methods for {@link Iterator}.
 *
 * @author Martin Kouba
 */
public final class Iterators {

    private Iterators() {
    }

    /**
     * Add all elements in the iterator to the collection.
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
     * Combine the iterators into a single one.
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
        return new TransformingIterator<T, R>(iterator, function);
    }

    static class CombinedIterator<E> implements Iterator<E> {

        private final Iterator<? extends Iterator<? extends E>> iterators;

        private Iterator<? extends E> current;

        private Iterator<? extends E> removeFrom;

        CombinedIterator(Iterator<? extends Iterator<? extends E>> iterators) {
            this.iterators = iterators;
            this.current = Collections.emptyIterator();
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
    static class TransformingIterator<T, R> implements Iterator<R> {

        final Iterator<? extends T> iterator;
        final Function<? super T, ? extends R> function;

        TransformingIterator(Iterator<? extends T> iterator, Function<? super T, ? extends R> function) {
            this.iterator = iterator;
            this.function = function;
        }

        @Override
        public final boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public final R next() {
            return function.apply(iterator.next());
        }

        @Override
        public final void remove() {
            iterator.remove();
        }
    }

    /**
     * Abstract iterator implementation that tracks current index. This implementation is useful for collection implementations
     * with well-known size and random access to elements where its {@link IndexIterator#getElement(int)} may use a switch/case
     * construct to return the corresponding element.
     *
     * @author Jozef Hartinger
     *
     * @param <E> the element type
     */
    abstract static class IndexIterator<E> implements ListIterator<E> {

        private int position;
        private final int size;

        IndexIterator(int size, int position) {
            this.size = size;
            this.position = position;
        }

        IndexIterator(int size) {
            this(size, 0);
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return getElement(position++);
        }

        @Override
        public boolean hasPrevious() {
            return position > 0;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return getElement(--position);
        }

        @Override
        public int nextIndex() {
            return position;
        }

        @Override
        public int previousIndex() {
            return position - 1;
        }

        /**
         * Returns the element identified by the given index.
         *
         * @param index the given index
         * @return element identified by the given index
         */
        abstract E getElement(int index);

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException();
        }
    }
}
