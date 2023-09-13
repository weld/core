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
package org.jboss.weld.environment.se.test.container.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;

import org.jboss.weld.environment.se.events.ContainerBeforeShutdown;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.events.ContainerShutdown;
import org.jboss.weld.test.util.ActionSequence;

@ApplicationScoped
public class ContainerObserver {

    public void onAppScopeInit(@Observes @Initialized(ApplicationScoped.class) Object event) {
        ActionSequence.addAction(ApplicationScoped.class.getName(),
                event.getClass().getName() + ApplicationScoped.class.getName());
    }

    public void onAppScopeDestroy(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        ActionSequence.addAction(ApplicationScoped.class.getName(),
                event.getClass().getName() + ApplicationScoped.class.getName());
    }

    public void onAppScopeBeforeDestroy(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
        ActionSequence.addAction(ApplicationScoped.class.getName(),
                event.getClass().getName() + ApplicationScoped.class.getName());
    }

    public void onContainerInitWithQualifier(@Observes @Initialized(ApplicationScoped.class) ContainerInitialized event) {
        ActionSequence.addAction(event.getClass().getName() + ApplicationScoped.class.getName() + event.getContainerId());
    }

    public void onContainerInit(@Observes ContainerInitialized event) {
        ActionSequence.addAction(event.getClass().getName() + event.getContainerId());
    }

    public void onContainerShutdownWithQualifier(@Observes @Destroyed(ApplicationScoped.class) ContainerShutdown event) {
        ActionSequence.addAction(event.getClass().getName() + ApplicationScoped.class.getName() + event.getContainerId());
    }

    public void onContainerShutdown(@Observes ContainerShutdown event) {
        ActionSequence.addAction(event.getClass().getName() + event.getContainerId());
    }

    public void onContainerBeforeShutdown(@Observes ContainerBeforeShutdown event) {
        ActionSequence.addAction(event.getClass().getName() + event.getContainerId());
    }

    public void onContainerBeforeShutdownWithQualifier(
            @Observes @BeforeDestroyed(ApplicationScoped.class) ContainerBeforeShutdown event) {
        ActionSequence.addAction(event.getClass().getName() + ApplicationScoped.class.getName() + event.getContainerId());
    }

}