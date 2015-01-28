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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.interceptor.Interceptor;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.experimental.Prioritized;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Catch-all observer with low priority (called first) that captures all events within the application and keeps information about them.
 *
 * @author Jozef Hartinger
 *
 */
class ProbeObserver implements ObserverMethod<Object>, Prioritized {

    private static final int PRIORITY_OFFSET = 100;

    static class EventInfo {

        final boolean containerEvent;
        final Type type;
        final Set<Annotation> qualifiers;
        final String eventString;
        final InjectionPoint injectionPoint;

        private EventInfo(Type type, Set<Annotation> qualifiers, Object event, InjectionPoint injectionPoint) {
            this.type = type;
            this.qualifiers = qualifiers;
            this.injectionPoint = injectionPoint;
            this.containerEvent = initContainerEvent(qualifiers);
            this.eventString = initEventString(event, containerEvent);
        }

        private boolean initContainerEvent(Set<Annotation> qualifiers) {
            for (Annotation annotation : qualifiers) {
                if (annotation.annotationType().equals(Initialized.class) || annotation.annotationType().equals(Destroyed.class)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * Workaround for Undertow's ugly toString(). TODO: also check Tomcat/Jetty and consider removing this if appropriate
         */
        private String initEventString(Object event, boolean containerEvent) {
            if (containerEvent && event instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) event;
                StringBuilder builder = new StringBuilder();
                builder.append(HttpServletRequest.class.getSimpleName());
                builder.append(' ');
                builder.append(request.getMethod());
                builder.append(' ');
                builder.append(request.getRequestURI());
                return builder.toString();
            }
            return event.toString();
        }
    }

    private final List<EventInfo> events = Collections.synchronizedList(new ArrayList<EventInfo>());
    private final CurrentEventMetadata currentEventMetadata;

    ProbeObserver(BeanManagerImpl manager) {
        this.currentEventMetadata = manager.getServices().get(CurrentEventMetadata.class);
    }

    @Override
    public Class<?> getBeanClass() {
        return ProbeExtension.class;
    }

    @Override
    public Type getObservedType() {
        return Object.class;
    }

    @Override
    public Set<Annotation> getObservedQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Reception getReception() {
        return Reception.ALWAYS;
    }

    @Override
    public TransactionPhase getTransactionPhase() {
        return TransactionPhase.IN_PROGRESS;
    }

    @Override
    public void notify(Object event) {
        EventMetadata metadata = currentEventMetadata.peek();
        EventInfo info = new EventInfo(metadata.getType(), metadata.getQualifiers(), event, metadata.getInjectionPoint());
        events.add(0, info);
    }

    @Override
    public int getPriority() {
        return Interceptor.Priority.PLATFORM_BEFORE + PRIORITY_OFFSET;
    }

    /**
     * Returns a mutable copy of the captured event information.
     * @return mutable copy of the captured event information
     */
    public List<EventInfo> getEvents() {
        synchronized (events) {
            return new ArrayList<EventInfo>(events);
        }
    }

    /**
     * Clear the state
     * @return the number of captured events before the state is cleared.
     */
    public int clear() {
        synchronized (events) {
            int count = events.size();
            events.clear();
            return count;
        }
    }
}
