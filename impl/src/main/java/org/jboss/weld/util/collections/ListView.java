/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.util.collections;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jboss.weld.bootstrap.events.ProcessModuleImpl;

/**
 * Provides a view of type List<VIEW> for a List<SOURCE> where the conversion between view and source is handled by a provided
 * {@link ViewProvider} implementation. Changes to the view list are reflected within the source list and vice versa.
 *
 * @author Jozef Hartinger
 *
 * @see ProcessModuleImpl
 *
 * @param <SOURCE> the source type
 * @param <VIEW> the view type
 */
public abstract class ListView<SOURCE, VIEW> extends AbstractList<VIEW> {

    protected abstract List<SOURCE> getDelegate();

    protected abstract ViewProvider<SOURCE, VIEW> getViewProvider();

    @Override
    public VIEW get(int index) {
        return getViewProvider().toView(getDelegate().get(index));
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public boolean add(VIEW e) {
        return getDelegate().add(getViewProvider().fromView(e));
    }

    @Override
    public VIEW set(int index, VIEW element) {
        return getViewProvider().toView(getDelegate().set(index, getViewProvider().fromView(element)));
    }

    @Override
    public void add(int index, VIEW element) {
        getDelegate().add(index, getViewProvider().fromView(element));
    }

    @Override
    public VIEW remove(int index) {
        return getViewProvider().toView(getDelegate().remove(index));
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

    private class ListViewIterator implements ListIterator<VIEW> {
        private ListIterator<SOURCE> delegate;

        public ListViewIterator(ListIterator<SOURCE> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public VIEW next() {
            return getViewProvider().toView(delegate.next());
        }

        @Override
        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        @Override
        public VIEW previous() {
            return getViewProvider().toView(delegate.previous());
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
            delegate.set(getViewProvider().fromView(e));
        }

        @Override
        public void add(VIEW e) {
            delegate.add(getViewProvider().fromView(e));
        }
    }
}
