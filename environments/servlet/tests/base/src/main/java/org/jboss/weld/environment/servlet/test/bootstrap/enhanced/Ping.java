/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.servlet.test.bootstrap.enhanced;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

@ApplicationScoped
public class Ping {

    private final AtomicInteger observations = new AtomicInteger(0);
    private final AtomicInteger objectObservations = new AtomicInteger(0);

    public void observesApplicationContext(@Observes @Initialized(ApplicationScoped.class) ServletContext servletContext) {
        observations.incrementAndGet();
    }

    public void observesApplicationContextObject(@Observes @Initialized(ApplicationScoped.class) Object object) {
        objectObservations.incrementAndGet();
    }

    public Integer getObservations() {
        return observations.get();
    }

    public Integer getObjectObservations() {
        return objectObservations.get();
    }
}
