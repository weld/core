/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * A {@link Set} which is backed by a simple array of elements. This provides
 * all the behaviors of a set backed by an array, thus insert and remove
 * operations are always O(n) time to check for uniqueness. This should only be
 * used for sets which are known to be mostly empty or to contain only a handful
 * of elements.
 * </p>
 * <p>
 * The primary use of this set is for those cases where small sets exists and
 * will not be changed. The savings in memory is significant compared to hash
 * sets which may contain many empty buckets.
 * </p>
 *
 * @author David Allen
 */
public class ArraySet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = -5296795345424575659L;

    // Underlying array of set elements
    private ArrayList<E> elements;

    public ArraySet(ArrayList<E> initialList) {
        elements = initialList;
    }

    public ArraySet(Collection<E> initialElements) {
        this(initialElements.size());
        addAll(initialElements);
    }

    public ArraySet(int size) {
        elements = new ArrayList<E>(size);
    }

    public ArraySet() {
        this(5);
    }

    public ArraySet(E... initialElements) {
        this(initialElements.length);
        addAll(initialElements);
    }

    public ArraySet(Collection<E> initialElements, E... initialElements2) {
        this(initialElements.size() + initialElements2.length);
        addAll(initialElements);
        addAll(initialElements2);
    }

    public boolean add(E e) {
        if (contains(e)) {
            return false;
        } else {
            elements.add(e);
            return true;
        }
    }

    public boolean addAll(Collection<? extends E> otherCollection) {
        boolean modified = false;
        boolean realSet = otherCollection instanceof Set<?>;
        Iterator<? extends E> setIterator = otherCollection.iterator();
        while (setIterator.hasNext()) {
            E element = setIterator.next();
            if (realSet || !contains(element)) {
                elements.add(element);
                modified = true;
            }
        }
        elements.trimToSize();
        return modified;
    }

    public boolean addAll(E... elements) {
        boolean modified = false;
        for (E element : elements) {
            modified = modified | add(element);
        }
        return modified;
    }

    public List<E> asList() {
        return Collections.unmodifiableList(elements);
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object o) {
        return elements.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Iterator<E> iterator() {
        return elements.iterator();
    }

    public boolean remove(Object o) {
        return elements.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return elements.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return elements.retainAll(c);
    }

    public int size() {
        return elements.size();
    }

    public Object[] toArray() {
        return elements.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return elements.toArray(a);
    }

    public ArraySet<E> trimToSize() {
        elements.trimToSize();
        return this;
    }

    // Needed to provide set equals semantics
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Set<?>) {
            int elementQuantity = size();
            Object[] otherArray = ((Set<?>) obj).toArray();
            if (elementQuantity != otherArray.length) {
                return false;
            }
            boolean arraysEqual = true;
            for (int i = 0; i < elementQuantity; i++) {
                boolean objFound = false;
                for (int j = 0; j < otherArray.length; j++) {
                    if (elements.get(i).equals(otherArray[j])) {
                        objFound = true;
                        break;
                    }
                }
                if (!objFound) {
                    arraysEqual = false;
                    break;
                }
            }
            return arraysEqual;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return elements.toString();
    }

}
