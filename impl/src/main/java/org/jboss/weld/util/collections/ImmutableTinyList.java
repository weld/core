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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * {@link List} implementations optimized for tiny number of elements
 *
 * @author Jozef Hartinger
 *
 * @param <E> the element type
 */
abstract class ImmutableTinyList<E> extends ImmutableList<E> implements RandomAccess {

    static class Singleton<E> extends ImmutableTinyList<E> implements Serializable {

        private static final long serialVersionUID = 1L;

        private class SingletonIterator extends Iterators.IndexIterator<E> {

            SingletonIterator(int index) {
                super(size(), index);
            }

            @Override
            E getElement(int index) {
                return element;
            }
        }

        private final E element;

        Singleton(E element) {
            this.element = element;
        }

        @Override
        public int indexOf(Object o) {
            if (o != null && o.equals(element)) {
                return 0;
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o);
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            if (index == 0 || index == 1) {
                return new SingletonIterator(index);
            }
            throw indexOutOfBoundsException(index);
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            if (fromIndex < 0 || fromIndex > toIndex) {
                throw indexOutOfBoundsException(fromIndex);
            }
            if (toIndex > size()) {
                throw indexOutOfBoundsException(toIndex);
            }
            if (fromIndex == toIndex) {
                return Collections.emptyList();
            } else {
                return this;
            }
        }

        @Override
        public int hashCode() {
            return 31 + element.hashCode();
        }

        @Override
        public E get(int index) {
            if (index == 0) {
                return element;
            }
            throw indexOutOfBoundsException(index);
        }

        @Override
        public int size() {
            return 1;
        }
    }
}
