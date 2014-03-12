/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * Tests the observing of both built-in and application-specific events.
 *
 * @author Peter Royle
 */
public class ObserverTestBean implements Extension {

    private static boolean builtInObserved = false;
    private static boolean customObserved = false;
    private static boolean initObserved = false;
    private static boolean initializedObserved = false;
    private static boolean destroyedObserved = false;

    public ObserverTestBean() {
    }

    public void observeBuiltInEvent(@Observes AfterDeploymentValidation after) {
        builtInObserved = true;
    }

    public void observeCustomEvent(@Observes CustomEvent event) {
        customObserved = true;
    }

    public void observeInitEvent(@Observes ContainerInitialized event) {
        initObserved = true;
    }

    public void observeInitialized(@Observes @Initialized(ApplicationScoped.class) Object event) {
        initializedObserved = event != null;
    }

    public void observeDestroyed(@Observes @Destroyed(ApplicationScoped.class) Object event) {
        destroyedObserved = event != null;
    }

    public static void reset() {
        customObserved = false;
        builtInObserved = false;
        initObserved = false;
        initializedObserved = false;
        destroyedObserved = false;
    }

    /**
     * @return the observed
     */
    public static boolean isBuiltInObserved() {
        return builtInObserved;
    }

    /**
     * @return
     */
    public static boolean isCustomObserved() {
        return customObserved;
    }

    /**
     * @return
     */
    public static boolean isInitObserved() {
        return initObserved;
    }

    public static boolean isInitializedObserved() {
        return initializedObserved;
    }

    public static boolean isDestroyedObserved() {
        return destroyedObserved;
    }
}
