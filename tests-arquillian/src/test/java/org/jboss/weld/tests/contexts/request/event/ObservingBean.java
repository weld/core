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
package org.jboss.weld.tests.contexts.request.event;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.servlet.ServletRequest;

@ApplicationScoped
public class ObservingBean {

    private final AtomicInteger initializedRequestCount = new AtomicInteger();
    private final AtomicInteger destroyedRequestCount = new AtomicInteger();

    public void observeRequestInitialized(@Observes @Initialized(RequestScoped.class) ServletRequest request) {
        if (!"bar".equals(request.getParameter("foo"))) {
            throw new IllegalArgumentException("Unknown request, parameter foo not set.");
        }
        initializedRequestCount.incrementAndGet();
    }

    public void observeRequestDestroyed(@Observes @Destroyed(RequestScoped.class) ServletRequest request) {
        destroyedRequestCount.incrementAndGet();
    }

    public AtomicInteger getInitializedRequestCount() {
        return initializedRequestCount;
    }

    public AtomicInteger getDestroyedRequestCount() {
        return destroyedRequestCount;
    }
}
