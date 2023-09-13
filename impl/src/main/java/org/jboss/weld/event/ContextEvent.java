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
package org.jboss.weld.event;

import java.io.Serializable;

import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;

/**
 * General event payload for {@link Initialized} / {@link Destroyed} events. A more specific payload is necessary
 * for certain contexts.
 *
 * @author Jozef Hartinger
 *
 */
public final class ContextEvent implements Serializable {

    private static final long serialVersionUID = -1197351184144276424L;

    public static final ContextEvent APPLICATION_INITIALIZED = new ContextEvent("Application context initialized.");
    public static final ContextEvent APPLICATION_BEFORE_DESTROYED = new ContextEvent(
            "Application context is about to be destroyed.");
    public static final ContextEvent APPLICATION_DESTROYED = new ContextEvent("Application context destroyed.");
    public static final Object REQUEST_INITIALIZED_EJB = new ContextEvent("Request context initialized for EJB invocation");
    public static final Object REQUEST_BEFORE_DESTROYED_EJB = new ContextEvent(
            "Request context is about to be destroyed after EJB invocation");
    public static final Object REQUEST_DESTROYED_EJB = new ContextEvent("Request context destroyed after EJB invocation");

    private final String message;

    ContextEvent(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
