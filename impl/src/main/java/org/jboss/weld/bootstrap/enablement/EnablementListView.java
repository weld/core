/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.util.Preconditions.checkNotNull;

import java.util.List;
import java.util.ListIterator;

import javax.interceptor.Interceptor;

import org.jboss.weld.exceptions.UnsupportedOperationException;
import org.jboss.weld.util.collections.ListView;

/**
 *
 * @author Martin Kouba
 *
 */
abstract class EnablementListView extends ListView<Item, Class<?>> {

    @Override
    public boolean add(Class<?> element) {
        checkNotNull(element);
        List<Item> list = getDelegate();
        synchronized (list) {
            return list.add(createSource(element, list.isEmpty() ? null : list.get(list.size() - 1), null));
        }
    }

    @Override
    public Class<?> set(int index, Class<?> element) {
        checkNotNull(element);
        List<Item> list = getDelegate();
        synchronized (list) {
            if (index < 0 || index >= list.size()) {
                throw new IndexOutOfBoundsException();
            }
            return toView(getDelegate().set(index, createSource(element, list.get(index).getPriority())));
        }
    }

    @Override
    public void add(int index, Class<?> element) {
        checkNotNull(element);
        List<Item> list = getDelegate();
        synchronized (list) {
            if (index < 0 || index >= list.size()) {
                throw new IndexOutOfBoundsException();
            }
            Item previous = (index > 0) ? list.get(index - 1) : null;
            Item next = (index <= (list.size() - 1)) ? list.get(index) : null;
            list.add(index, createSource(element, previous, next));
        }
    }

    @Override
    protected Class<?> toView(Item source) {
        return source.getJavaClass();
    }

    @Override
    protected Item createSource(Class<?> view) {
        throw new UnsupportedOperationException();
    }

    private Item createSource(Class<?> view, Item previous, Item next) {
        return createSource(view, getPriority(previous, next));
    }

    private Item createSource(Class<?> view, int priority) {
        return new Item(view, priority);
    }

    private int getPriority(Item previous, Item next) {
        int priority;
        if (previous == null && next == null) {
            // No bounds
            priority = Interceptor.Priority.APPLICATION;
        } else if (previous != null && next != null) {
            int gap = (next.getPriority() - previous.getPriority());
            if (gap <= 1) {
                // There is no gap - scale the priorities
                for (Item item : getDelegate()) {
                    item.scalePriority();
                }
                priority = getPriority(previous, next);
            } else {
                priority = (gap / 2) + previous.getPriority();
            }
        } else if (previous != null) {
            priority = previous.getPriority() + Item.ITEM_PRIORITY_SCALE_POWER;
        } else {
            priority = next.getPriority() - Item.ITEM_PRIORITY_SCALE_POWER;
        }
        return priority;
    }

    @Override
    public ListIterator<Class<?>> listIterator() {
        return new EnablementListViewIterator(getDelegate().listIterator());
    }

    @Override
    public ListIterator<Class<?>> listIterator(int index) {
        return new EnablementListViewIterator(getDelegate().listIterator(index));
    }

    class EnablementListViewIterator extends ListViewIterator {

        public EnablementListViewIterator(ListIterator<Item> delegate) {
            super(delegate);
        }

        /**
         * The last item returned by a call to <code>next()</code> or <code>previous()</code>.
         */
        private Item lastItem;

        @Override
        public Class<?> next() {
            lastItem = delegate.next();
            return EnablementListView.this.toView(lastItem);
        }

        @Override
        public Class<?> previous() {
            lastItem = delegate.previous();
            return EnablementListView.this.toView(lastItem);
        }

        @Override
        public void set(Class<?> e) {
            delegate.set(EnablementListView.this.createSource(e, lastItem.getPriority()));
        }

        @Override
        public void add(Class<?> e) {
            Item previous = hasPrevious() ? EnablementListView.this.getDelegate().get(previousIndex()) : null;
            Item next = hasNext() ? EnablementListView.this.getDelegate().get(nextIndex()) : null;
            delegate.add(EnablementListView.this.createSource(e, previous, next));
        }

    }

}
