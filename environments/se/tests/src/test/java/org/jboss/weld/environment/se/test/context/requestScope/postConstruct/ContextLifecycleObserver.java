/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.context.requestScope.postConstruct;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

import org.junit.Assert;

@ApplicationScoped
public class ContextLifecycleObserver {

    private final AtomicInteger initCount = new AtomicInteger(0);
    private final AtomicInteger beforeDestroyedCount = new AtomicInteger(0);
    private final AtomicInteger destroyedCount = new AtomicInteger(0);

    public void observeRequestScopeInitialization(@Observes @Initialized(RequestScoped.class) Object event) {
        Assert.assertNotNull("Payload of @Initialized(RequestScoped.class) was null!", event);
        initCount.incrementAndGet();
    }

    public void observeRequestScopeBeforeDestruction(@Observes @BeforeDestroyed(RequestScoped.class) Object event) {
        Assert.assertNotNull("Payload of @BeforeDestroyed(RequestScoped.class) was null!", event);
        beforeDestroyedCount.incrementAndGet();

    }

    public void observeRequestScopeDestruction(@Observes @Destroyed(RequestScoped.class) Object event) {
        Assert.assertNotNull("Payload of @Destroyed(RequestScoped.class) was null!", event);
        destroyedCount.incrementAndGet();
    }

    public AtomicInteger getInitCount() {
        return initCount;
    }

    public AtomicInteger getBeforeDestroyedCount() {
        return beforeDestroyedCount;
    }

    public AtomicInteger getDestroyedCount() {
        return destroyedCount;
    }

}
