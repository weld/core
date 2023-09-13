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
 * Immutable {@link Set} implementation. This implementation uses open addressing with linear probing and a table size of the
 * nearest power of two so that load
 * factor is below 0.75. The implementation is inspired by Guava's ImmutableSet implementation.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the element type
 */
public final class ImmutableHashSet<T> extends ImmutableSet<T> implements Serializable {

    private static final int MAX_CAPACITY = 30;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int MAX_SIZE = (int) Math.floor((1 << MAX_CAPACITY) * LOAD_FACTOR);

    private class IteratorImpl implements Iterator<T> {

        private int position = -1;
        private int processedElements = 0;

        @Override
        public boolean hasNext() {
            return processedElements < size;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            position++;
            while (table[position] == null) {
                position++;
            }
            processedElements++;
            return (T) table[position];
        }
    }

    private static final long serialVersionUID = 1L;

    private final Object[] table;
    private final int size;
    private final int hashCode;

    public ImmutableHashSet(Set<T> data) {
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(!data.isEmpty(), data);
        Preconditions.checkArgument(data.size() < MAX_SIZE, "Collection too large: " + data.size());
        this.size = data.size();
        this.table = new Object[tableSize(size)];
        for (T element : data) {
            storeElement(element);
        }
        this.hashCode = data.hashCode();
    }

    private static int tableSize(int dataSize) {
        int candidate = Integer.highestOneBit(dataSize) << 1;
        if (candidate * LOAD_FACTOR < dataSize) {
            return Integer.highestOneBit(dataSize) << 2;
        } else {
            return candidate;
        }
    }

    private int getTableIndex(int hashCode) {
        return hashCode & table.length - 1;
    }

    private void storeElement(T element) {
        for (int i = element.hashCode();; i++) {
            int index = getTableIndex(i);
            if (table[index] == null) {
                table[index] = element;
                return;
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (int i = o.hashCode();; i++) {
            Object item = table[getTableIndex(i)];
            if (item == null) {
                return false;
            }
            if (o.equals(item)) {
                return true;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AbstractImmutableSet<?>) {
            // all our immutable set implementations have fast hashcode
            AbstractImmutableSet<?> that = (AbstractImmutableSet<?>) obj;
            if (hashCode() != that.hashCode()) {
                return false;
            }
            return equalsSet(that);
        }
        if (obj instanceof Set<?>) {
            return equalsSet((Set<?>) obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public Iterator<T> iterator() {
        return new IteratorImpl();
    }
}
