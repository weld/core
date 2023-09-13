/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.event.container.lifecycle;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.event.Startup;

@ApplicationScoped
public class ObservingBean {

    public static List<String> OBSERVED_STARTING_EVENTS = new ArrayList<>();
    public static List<String> OBSERVED_SHUTDOWN_EVENTS = new ArrayList<>();

    public void startup(@Observes Startup startup) {
        OBSERVED_STARTING_EVENTS.add(Startup.class.getSimpleName());
    }

    public void initAppScope(@Observes @Initialized(ApplicationScoped.class) Object init) {
        OBSERVED_STARTING_EVENTS.add(ApplicationScoped.class.getSimpleName());
    }

    public void shutdown(@Observes Shutdown shutdown) {
        OBSERVED_SHUTDOWN_EVENTS.add(Shutdown.class.getSimpleName());
    }

    public void observeBeforeShutdown(@Observes @BeforeDestroyed(ApplicationScoped.class) Object event) {
        OBSERVED_SHUTDOWN_EVENTS.add(ApplicationScoped.class.getSimpleName());
    }
}
