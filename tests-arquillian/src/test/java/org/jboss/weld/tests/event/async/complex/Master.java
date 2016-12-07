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
package org.jboss.weld.tests.event.async.complex;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Splits the work and delegates it to workers. In addition, it aggregates the result.
 *
 * @author Jozef Hartinger
 *
 */
@ApplicationScoped
public class Master {

    @Inject
    private Event<Work> event;
    @Inject
    private Event<PiApproximation> resultEvent;

    private long start;

    private double pi;
    private int pendingMessages;

    public synchronized void compute(CalculationConfiguration configuration) {
        this.pendingMessages = configuration.getNumberOfMessages();
        start = System.currentTimeMillis();
        for (int i = 0; i < configuration.getNumberOfMessages(); i++) {
            event.fireAsync(new Work(i, configuration.getNumberOfElements()));
        }
    }

    public synchronized void receiveResults(@Observes Result result) {
        pi += result.getValue();
        pendingMessages--;

        if (pendingMessages == 0) {
            resultEvent.fire(new PiApproximation(pi, System.currentTimeMillis() - start));
        }
    }

}
