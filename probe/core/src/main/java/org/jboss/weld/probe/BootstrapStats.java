/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;

/**
 *
 * @author Martin Kouba
 *
 */
@Vetoed
class BootstrapStats {

    private final EnumMap<EventType, AtomicInteger> counts;

    BootstrapStats() {
        this.counts = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            this.counts.put(type, new AtomicInteger(0));
        }
    }

    void increment(EventType evenType) {
        counts.get(evenType).incrementAndGet();
    }

    /**
     * @return the counts
     */
    EnumMap<EventType, AtomicInteger> getCounts() {
        return counts;
    }

    enum EventType {

        PAT(ProcessAnnotatedType.class, 1), PP(ProcessProducer.class, 6), PB(ProcessBean.class, 5), PIP(ProcessInjectionPoint.class,
                2), PIT(ProcessInjectionTarget.class, 3), PBA(ProcessBeanAttributes.class, 4), POM(ProcessObserverMethod.class, 7);

        private Class<?> eventType;

        private int priority;

        EventType(Class<?> eventType, int priority) {
            this.eventType = eventType;
            this.priority = priority;
        }

        String getType() {
            return eventType.getName();
        }

        int getPriority() {
            return priority;
        }

    }
}
