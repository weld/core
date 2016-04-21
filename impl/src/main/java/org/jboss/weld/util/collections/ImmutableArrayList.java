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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import org.jboss.weld.util.Preconditions;

/**
 * Immutable {@link List} implementation. This implementation uses an array as its backing storage.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 * @param <E> the element type
 */
class ImmutableArrayList<E> extends ImmutableList<E> implements RandomAccess, Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] elements;

    private class ListIteratorImpl extends Iterators.IndexIterator<E> {

        ListIteratorImpl(int index) {
            super(size(), index);
        }

        @Override
        E getElement(int index) {
            return get(index);
        }
    }

    ImmutableArrayList(Object[] elements) {
        // null checks are performed in ImmutableList class
        this.elements = elements;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw indexOutOfBoundsException(index);
        }
        return (E) elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean contains(Object o) {
        Preconditions.checkNotNull(o);
        for (Object element : elements) {
            if (o.equals(element)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int indexOf(Object o) {
        Preconditions.checkNotNull(o);
        for (int i = 0; i < elements.length; i++) {
            if (o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        Preconditions.checkNotNull(o);
        for (int i = elements.length - 1; i >= 0; i--) {
            if (o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > elements.length) {
            throw indexOutOfBoundsException(index);
        }
        return new ListIteratorImpl(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > toIndex) {
            throw indexOutOfBoundsException(fromIndex);
        }
        if (toIndex > elements.length) {
            throw indexOutOfBoundsException(toIndex);
        }
        if (fromIndex == toIndex) {
            return Collections.emptyList();
        }
        return new ImmutableArrayList<E>(Arrays.copyOfRange(this.elements, fromIndex, toIndex));
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Object element : elements) {
            hashCode = 31 * hashCode + element.hashCode();
        }
        return hashCode;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elements, size());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] array) {
        if (array.length < size()) {
            return (T[]) Arrays.copyOf(elements, size(), array.getClass());
        }
        System.arraycopy(elements, 0, array, 0, size());
        if (array.length > size()) {
            array[size()] = null;
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size(); i++) {
            Object element = elements[i];
            sb.append(element == this ? "(this Collection)" : element);
            if (i + 1 < size()) {
                sb.append(',').append(' ');
            }
        }
        return sb.append(']').toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> action) {
        Preconditions.checkNotNull(action);
        for (Object object : elements) {
            action.accept((E) object);
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(elements, Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }
}
