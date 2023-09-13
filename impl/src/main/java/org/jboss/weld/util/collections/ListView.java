/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides a view of type List<VIEW> for a List<SOURCE>. Changes to the view list are reflected within the source list and vice
 * versa.
 *
 * @param <SOURCE> the source type
 * @param <VIEW> the view type
 * @author Jozef Hartinger
 */
public abstract class ListView<SOURCE, VIEW> extends AbstractList<VIEW> {

    protected abstract List<SOURCE> getDelegate();

    @Override
    public VIEW get(int index) {
        return toView(getDelegate().get(index));
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public boolean add(VIEW element) {
        return getDelegate().add(createSource(element));
    }

    @Override
    public VIEW set(int index, VIEW element) {
        return toView(getDelegate().set(index, createSource(element)));
    }

    @Override
    public void add(int index, VIEW element) {
        getDelegate().add(index, createSource(element));
    }

    @Override
    public VIEW remove(int index) {
        return toView(getDelegate().remove(index));
    }

    @Override
    public void clear() {
        getDelegate().clear();
    }

    @Override
    public Iterator<VIEW> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<VIEW> listIterator() {
        return new ListViewIterator(getDelegate().listIterator());
    }

    @Override
    public ListIterator<VIEW> listIterator(int index) {
        return new ListViewIterator(getDelegate().listIterator(index));
    }

    /**
     * Provides a view representation of a source object.
     *
     * @param source
     * @return a view representation
     */
    protected abstract VIEW toView(SOURCE source);

    /**
     * Creates a new source object based on a given view.
     *
     * @param view
     * @return a new source object
     */
    protected abstract SOURCE createSource(VIEW view);

    protected class ListViewIterator implements ListIterator<VIEW> {

        protected final ListIterator<SOURCE> delegate;

        public ListViewIterator(ListIterator<SOURCE> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public VIEW next() {
            return ListView.this.toView(delegate.next());
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public VIEW previous() {
            return ListView.this.toView(delegate.previous());
        }

        @Override
        public int nextIndex() {
            return delegate.nextIndex();
        }

        @Override
        public int previousIndex() {
            return delegate.previousIndex();
        }

        @Override
        public void remove() {
            delegate.remove();
        }

        @Override
        public void set(VIEW e) {
            delegate.set(ListView.this.createSource(e));
        }

        @Override
        public void add(VIEW e) {
            delegate.add(ListView.this.createSource(e));
        }
    }
}
