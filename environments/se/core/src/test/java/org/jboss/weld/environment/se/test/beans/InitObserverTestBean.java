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

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * Tests the observing of both built-in and application-specific events.
 *
 * @author Peter Royle
 */
public class InitObserverTestBean {

    private static boolean initObserved = false;

    @Inject
    MainTestBean bean;

    public InitObserverTestBean() {
    }

    public void observeInitEvent(@Observes ContainerInitialized event) {
        initObserved = true;
        assert this.bean != null;
    }

    public static void reset() {
        initObserved = false;
    }

    /**
     * @return
     */
    public static boolean isInitObserved() {
        return initObserved;
    }

}
