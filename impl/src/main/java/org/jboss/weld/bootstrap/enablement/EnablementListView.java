/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.enablement;

import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.util.collections.ListView;

abstract class EnablementListView extends ListView<Item, Class<?>> {

    protected abstract ViewType getViewType();

    protected abstract Extension getExtension();

    private static final String ADD_OPERATION = "adds";
    private static final String REMOVE_OPERATION = "removes";
    private static final String SET_OPERATION = "sets";
    private static final String RETAIN_OPERATION = "retains";

    @Override
    public boolean add(Class<?> clazz) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), clazz, ADD_OPERATION, getViewType());
        }
        return getDelegate().add(createSource(clazz));
    }

    @Override
    public Class<?> set(int index, Class<?> clazz) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), clazz, SET_OPERATION, getViewType());
        }
        return toView(getDelegate().set(index, createSource(clazz)));
    }

    @Override
    public void add(int index, Class<?> clazz) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), clazz, ADD_OPERATION, getViewType());
        }
        getDelegate().add(index, createSource(clazz));
    }

    @Override
    public Class<?> remove(int index) {
        Item removedItem = getDelegate().remove(index);
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), removedItem.getClass(), REMOVE_OPERATION, getViewType());
        }
        return toView(removedItem);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), c, REMOVE_OPERATION, getViewType());
        }
        // use impl from AbstractCollection, that one will still invoke our contains() method
        return super.removeAll(c);
    }

    @Override
    public boolean remove(Object o) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), o, REMOVE_OPERATION, getViewType());
        }
        return getDelegate().remove(objectToItemIfNeeded(o));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), c, RETAIN_OPERATION, getViewType());
        }
        // use impl from AbstractCollection, that one will still invoke our contains() method
        return super.retainAll(c);
    }

    @Override
    public void clear() {
        if (getExtension() != null) {
            BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), "", REMOVE_OPERATION + " all classes", getViewType());
        }
        getDelegate().clear();
    }

    @Override
    public Iterator<Class<?>> iterator() {
        return listIterator();
    }

    @Override
    public ListIterator<Class<?>> listIterator() {
        return new EnablementListViewIterator(getDelegate().listIterator());
    }

    @Override
    public ListIterator<Class<?>> listIterator(int index) {
        return new EnablementListViewIterator(getDelegate().listIterator(index));
    }

    @Override
    protected Class<?> toView(Item source) {
        return source.getJavaClass();
    }

    @Override
    protected Item createSource(Class<?> view) {
        return new Item(view);
    }

    @Override
    /**
     * Override contains to support Object -> Item conversion
     */
    public boolean contains(Object o) {
        return getDelegate().contains(objectToItemIfNeeded(o));
    }

    @Override
    public int indexOf(Object o) {
        return getDelegate().indexOf(objectToItemIfNeeded(o));
    }

    private Object objectToItemIfNeeded(Object o) {
        if (o instanceof Item) {
            return o;
        } else {
            return createSource((Class<?>) o);
        }
    }

    class EnablementListViewIterator extends ListViewIterator {

        private ListIterator<Item> delegate;

        public EnablementListViewIterator(ListIterator<Item> delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        @Override
        public void remove() {
            if (getExtension() != null) {
                BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), getDelegate().get(delegate.nextIndex()).getJavaClass(), REMOVE_OPERATION, getViewType());
            }
            delegate.remove();
        }

        @Override
        public void set(Class<?> clazz) {
            if (getExtension() != null) {
                BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), clazz, SET_OPERATION, getViewType());
            }
            delegate.set(createSource(clazz));
        }

        @Override
        public void add(Class<?> clazz) {
            if (getExtension() != null) {
                BootstrapLogger.LOG.typeModifiedInAfterTypeDiscovery(getExtension(), clazz, ADD_OPERATION, getViewType());
            }
            delegate.add(createSource(clazz));
        }
    }

    enum ViewType {

        ALTERNATIVES("getAlternatives()"),
        INTERCEPTORS("getInterceptors()"),
        DECORATORS("getDecorators()");

        private final String name;

        ViewType(String s) {
            name = s;
        }

        public String toString() {
            return this.name;
        }
    }

}
