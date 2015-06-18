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
package org.jboss.weld.tests.contexts.application.event;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

public abstract class AbstractObserver {

    public abstract String getName();

    private Object observed;

    synchronized void observeObject(@Observes @Initialized(ApplicationScoped.class) Object event) {
        storeLocally(event);
        if (!EventRepository.OBJECTS.add(getName())) {
            throw new IllegalStateException("Event delivered multiple times " + event);
        }
    }

    synchronized void observeServletContext(@Observes @Initialized(ApplicationScoped.class) ServletContext event) {
        if (!event.getContextPath().equals("/" + getName())) {
            throw new IllegalArgumentException("Excepted /" + getName() + " but received " + event.getContextPath());
        }
        storeLocally(event);
        if (!EventRepository.SERVLET_CONTEXTS.add(getName())) {
            throw new IllegalStateException("Event delivered multiple times " + event);
        }
    }

    private void storeLocally(Object event) {
        if (observed != null && observed != event) {
            throw new IllegalStateException("Event delivered multiple times " + event + "; " + observed);
        }
        observed = event;
    }
}
