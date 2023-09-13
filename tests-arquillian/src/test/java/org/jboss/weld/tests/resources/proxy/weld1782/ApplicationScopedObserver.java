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
package org.jboss.weld.tests.resources.proxy.weld1782;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class ApplicationScopedObserver {

    private final AtomicBoolean destroyedCalled = new AtomicBoolean();

    @Inject
    private Controller controller;

    void observeRequestDestroyed(@Observes @Destroyed(RequestScoped.class) Object event) {
        destroyedCalled.set(true);
        controller.getContextDestroyedLatch().countDown();

    }

    boolean isDestroyedCalled() {
        return destroyedCalled.get();
    }

    void reset() {
        destroyedCalled.set(false);
    }
}
