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
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.EMPTY;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;

/**
 *
 * @author Jozef Hartinger
 */
@Vetoed
class EventInfo {

    private static final int EVENT_INFO_STRING_LIMIT = 80;

    private final boolean containerEvent;

    private final Type type;

    private final Set<Annotation> qualifiers;

    private final String eventString;

    private final InjectionPoint injectionPoint;

    private final List<ObserverMethod<?>> observers;

    private final long timestamp;

    /**
     *
     * @param type
     * @param qualifiers
     * @param event
     * @param injectionPoint
     * @param observers
     * @param containerEvent
     * @param timestamp
     */
    EventInfo(Type type, Set<Annotation> qualifiers, Object event, InjectionPoint injectionPoint, List<ObserverMethod<?>> observers, boolean containerEvent,
            long timestamp) {
        this(type, qualifiers, event, injectionPoint, observers, containerEvent, timestamp, true);
    }

    /**
     *
     * @param type
     * @param qualifiers
     * @param event
     * @param injectionPoint
     * @param observers
     * @param containerEvent
     * @param timestamp
     * @param abbreviateEventString
     */
    EventInfo(Type type, Set<Annotation> qualifiers, Object event, InjectionPoint injectionPoint, List<ObserverMethod<?>> observers, boolean containerEvent,
            long timestamp, boolean abbreviateEventString) {
        this.type = type;
        this.qualifiers = qualifiers;
        this.injectionPoint = injectionPoint;
        this.containerEvent = containerEvent;
        this.eventString = event != null ? (abbreviateEventString ? Strings.abbreviate(event.toString(), EVENT_INFO_STRING_LIMIT) : event.toString()) : EMPTY;
        this.observers = observers;
        this.timestamp = timestamp;
    }

    boolean isContainerEvent() {
        return containerEvent;
    }

    Type getType() {
        return type;
    }

    Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    String getEventString() {
        return eventString;
    }

    InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    List<ObserverMethod<?>> getObservers() {
        return observers;
    }

    long getTimestamp() {
        return timestamp;
    }

}