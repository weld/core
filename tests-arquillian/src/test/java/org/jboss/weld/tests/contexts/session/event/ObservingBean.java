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
package org.jboss.weld.tests.contexts.session.event;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.servlet.http.HttpSessionEvent;

@ApplicationScoped
public class ObservingBean {

    private final AtomicInteger initializedSessionCount = new AtomicInteger();
    private final AtomicInteger destroyedSessionCount = new AtomicInteger();

    public void observeSessionInitialized(@Observes @Initialized(SessionScoped.class) HttpSessionEvent event) {
        initializedSessionCount.incrementAndGet();
    }

    public void observeSessionDestroyed(@Observes @Destroyed(SessionScoped.class) HttpSessionEvent event) {
        destroyedSessionCount.incrementAndGet();
    }

    public AtomicInteger getInitializedSessionCount() {
        return initializedSessionCount;
    }

    public AtomicInteger getDestroyedSessionCount() {
        return destroyedSessionCount;
    }
}
