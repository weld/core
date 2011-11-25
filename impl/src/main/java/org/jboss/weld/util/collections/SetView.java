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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.bootstrap.events.ProcessModuleImpl;

/**
 * Provides a view of type Set<VIEW> for a Set<SOURCE> where the conversion between view and source is handled by a provided
 * {@link ViewProvider} implementation. Changes to the view set are reflected within the source set and vice versa.
 *
 * @author Jozef Hartinger
 *
 * @see ProcessModuleImpl
 *
 * @param <SOURCE> the source type
 * @param <VIEW> the view type
 */
public abstract class SetView<SOURCE, VIEW> extends AbstractSet<VIEW> {

    protected abstract Set<SOURCE> getDelegate();

    protected abstract ViewProvider<SOURCE, VIEW> getViewProvider();

    @Override
    public Iterator<VIEW> iterator() {
        return new CollectionViewIterator();
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
    public void clear() {
        getDelegate().clear();
    }

    private class CollectionViewIterator implements Iterator<VIEW> {

        private Iterator<SOURCE> delegate = getDelegate().iterator();

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public VIEW next() {
            return getViewProvider().toView(delegate.next());
        }

        @Override
        public void remove() {
            delegate.remove();
        }
    }

}
