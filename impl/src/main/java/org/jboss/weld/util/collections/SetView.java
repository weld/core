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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides a view of type Set<VIEW> for a Set<SOURCE> where the conversion between view and source is handled by a provided
 * {@link ViewProvider} implementation. Changes to the view set are reflected within the source set and vice versa.
 *
 * @author Jozef Hartinger
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
