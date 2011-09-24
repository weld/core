/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.context.cache;

import java.util.LinkedList;
import java.util.List;

/**
 * Caches beans over the life of a request, to allow for efficient bean lookups from proxies.
 *
 * @author Stuart Douglas
 */
public class RequestScopedBeanCache {

    private static final ThreadLocal<List<RequestScopedItem>> CACHE = new ThreadLocal<List<RequestScopedItem>>();

    public static boolean isActive() {
        return CACHE.get() != null;
    }

    public static void addItem(final RequestScopedItem item) {
        final List<RequestScopedItem> cache = CACHE.get();
        if (cache == null) {
            throw new IllegalStateException("Unable to add request scoped cache item when request cache is not active");
        }
        cache.add(item);
    }

    public static void addItem(final ThreadLocal item) {
        final List<RequestScopedItem> cache = CACHE.get();
        if (cache == null) {
            throw new IllegalStateException("Unable to add request scoped cache item when request cache is not active");
        }
        cache.add(new RequestScopedItem() {
            public void invalidate() {
                item.remove();
            }
        });
    }

    public static void beginRequest() {
        CACHE.set(new LinkedList<RequestScopedItem>());
    }

    /**
     * ends the request and clears the cache. This can be called before the request is over,
     * in which case the cache will be unavailable for the rest of the request.
     */
    public static void endRequest() {
        final List<RequestScopedItem> result = CACHE.get();
        CACHE.remove();
        if (result != null) {
            for (final RequestScopedItem item : result) {
                item.invalidate();
            }
        }
    }

}
