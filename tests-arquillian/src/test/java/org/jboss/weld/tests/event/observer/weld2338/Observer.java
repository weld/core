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
package org.jboss.weld.tests.event.observer.weld2338;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class Observer implements Extension {

    public static final AtomicInteger timesNonCleNotified = new AtomicInteger(0);
    public static final AtomicBoolean nonCleFooInjected = new AtomicBoolean(false);

    public static final AtomicInteger timesCleNotified = new AtomicInteger(0);

    public void observeNonCLE(@Observes @Experimental Object o, Foo foo) {
        timesNonCleNotified.incrementAndGet();
        nonCleFooInjected.set(foo != null);
    }

    /**
     * This should be considered a container lifecycle event(CLE) observer.
     * E.g. it should be notified many times over.
     */
    void observeCLE(@Observes Object event) {
        timesCleNotified.incrementAndGet();
    }
}
