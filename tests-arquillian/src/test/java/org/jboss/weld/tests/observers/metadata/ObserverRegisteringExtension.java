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

package org.jboss.weld.tests.observers.metadata;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

public class ObserverRegisteringExtension implements Extension {

    public static int TIMES_OBSERVERS_NOTIFIED = 0;

    public void register(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        // synth observer, its declaring bean will be null
        afterBeanDiscovery.addObserverMethod().observedType(String.class)
                .notifyWith(eventContext -> TIMES_OBSERVERS_NOTIFIED++);
    }
}
