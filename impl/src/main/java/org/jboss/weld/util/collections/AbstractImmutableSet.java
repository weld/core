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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

import org.jboss.weld.util.Preconditions;

/**
 * Common implementation for all immutable set implementations.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 * @param <T> the element type
 */
abstract class AbstractImmutableSet<T> implements Set<T> {

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    private void fill(Object[] array) {
        Preconditions.checkNotNull(array);
        int i = 0;
        for (Iterator<T> iterator = iterator(); iterator.hasNext();) {
            array[i++] = iterator.next();
        }
        /*
         * If this set fits in the specified array with room to spare (i.e., the array has more elements than this set), the element in the array immediately
         * following the end of the set is set to null.
         */
        if (array.length > i) {
            array[i] = null;
        }
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        fill(array);
        return array;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <AT> AT[] toArray(AT[] array) {
        if (array.length < size()) {
            array = (AT[]) Array.newInstance(array.getClass().getComponentType(), size());
        }
        fill(array);
        return array;
    }

    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        final Iterator<T> iterator = iterator();
        final StringBuilder builder = new StringBuilder("[");
        while (true) {
            T next = iterator.next();
            if (next == this) {
                builder.append("(this Collection)");
            } else {
                builder.append(next);
            }
            if (!iterator.hasNext()) {
                return builder.append(']').toString();
            }
            builder.append(", ");
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
            return equals(that);
        }
        if (obj instanceof Set<?>) {
            return equals((Set<?>) obj);
        }
        return false;
    }

    boolean equals(Set<?> that) {
        return this.size() == that.size() && that.containsAll(this);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL);
    }
}
