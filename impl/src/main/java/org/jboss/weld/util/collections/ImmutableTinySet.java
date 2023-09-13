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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.weld.util.Preconditions;

/**
 * Contains {@link Set} implementations optimized for tiny number of elements. These implementations do not use hashing.
 * {@link Set#contains(Object)} is o(n)
 * which is fine as the sets are tiny.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 * @param <T>
 */
abstract class ImmutableTinySet<T> extends ImmutableSet<T> {

    /**
     * {@link Set} implementation that represents a set containing a single element.
     *
     * @author Jozef Hartinger
     *
     * @param <T> the element type
     */
    static class Singleton<T> extends ImmutableTinySet<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        private final T element;

        Singleton(Set<T> set) {
            Preconditions.checkNotNull(set);
            Preconditions.checkArgument(set.size() == 1, set);
            this.element = set.iterator().next();
        }

        Singleton(T element) {
            Preconditions.checkNotNull(element);
            this.element = element;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            return o.equals(element);
        }

        @Override
        public int hashCode() {
            return element.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterators.IndexIterator<T>(this.size()) {
                @Override
                T getElement(int position) {
                    return element;
                }
            };
        }
    }

    /**
     * {@link Set} implementation that represents a set containing two elements.
     *
     * @author Jozef Hartinger
     *
     * @param <T> the element type
     */
    static class Doubleton<T> extends ImmutableTinySet<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        private final T element1;
        private final T element2;

        Doubleton(Set<T> set) {
            Preconditions.checkNotNull(set);
            Preconditions.checkArgument(set.size() == 2, set);
            Iterator<T> iterator = set.iterator();
            element1 = iterator.next();
            element2 = iterator.next();
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            return o.equals(element1) || o.equals(element2);
        }

        @Override
        public int hashCode() {
            return element1.hashCode() + element2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterators.IndexIterator<T>(this.size()) {
                @Override
                T getElement(int position) {
                    switch (position) {
                        case 0:
                            return element1;
                        case 1:
                            return element2;
                        default:
                            throw new NoSuchElementException();
                    }
                }
            };
        }
    }

    /**
     * {@link Set} implementation that represents a set containing three elements.
     *
     * @author Jozef Hartinger
     *
     * @param <T> the element type
     */
    static class Tripleton<T> extends ImmutableTinySet<T> implements Serializable {

        private static final long serialVersionUID = 1L;
        private final T element1;
        private final T element2;
        private final T element3;

        Tripleton(Set<T> set) {
            Preconditions.checkNotNull(set);
            Preconditions.checkArgument(set.size() == 3, set);
            Iterator<T> iterator = set.iterator();
            element1 = iterator.next();
            element2 = iterator.next();
            element3 = iterator.next();
        }

        @Override
        public int size() {
            return 3;
        }

        @Override
        public boolean contains(Object o) {
            if (o == null) {
                return false;
            }
            return o.equals(element1) || o.equals(element2) || o.equals(element3);
        }

        @Override
        public int hashCode() {
            return element1.hashCode() + element2.hashCode() + element3.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterators.IndexIterator<T>(this.size()) {
                @Override
                T getElement(int position) {
                    switch (position) {
                        case 0:
                            return element1;
                        case 1:
                            return element2;
                        case 2:
                            return element3;
                        default:
                            throw new NoSuchElementException();
                    }
                }
            };
        }
    }
}
