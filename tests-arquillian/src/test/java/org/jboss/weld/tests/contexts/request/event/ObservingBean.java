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
package org.jboss.weld.tests.contexts.request.event;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.servlet.ServletRequestEvent;

@ApplicationScoped
public class ObservingBean {

    private final AtomicInteger initializedRequestCount = new AtomicInteger();
    private final AtomicInteger destroyedRequestCount = new AtomicInteger();

    public void observeRequestInitialized(@Observes @Initialized(RequestScoped.class) ServletRequestEvent event) {
        if (!"bar".equals(event.getServletRequest().getParameter("foo"))) {
            throw new IllegalArgumentException("Unknown request, parameter foo not set.");
        }
        initializedRequestCount.incrementAndGet();
    }

    public void observeRequestDestroyed(@Observes @Destroyed(RequestScoped.class) ServletRequestEvent event) {
        destroyedRequestCount.incrementAndGet();
    }

    public AtomicInteger getInitializedRequestCount() {
        return initializedRequestCount;
    }

    public AtomicInteger getDestroyedRequestCount() {
        return destroyedRequestCount;
    }
}
