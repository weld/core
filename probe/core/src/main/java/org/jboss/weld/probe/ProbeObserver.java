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
import java.util.regex.Pattern;

import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.Prioritized;
import javax.interceptor.Interceptor;

import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.event.ResolvedObservers;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.reflection.Formats;

/**
 * Catch-all observer with low priority (called first) that captures all events within the application.
 *
 * @author Jozef Hartinger
 *
 */
@Vetoed
class ProbeObserver implements ObserverMethod<Object>, Prioritized {

    private static final int PRIORITY_OFFSET = 100;

    private final Pattern excludePattern;

    private final Probe probe;

    private final CurrentEventMetadata currentEventMetadata;

    private final BeanManagerImpl manager;

    /**
     *
     * @param manager
     * @param excludePattern
     * @param probe
     */
    ProbeObserver(BeanManagerImpl manager, Pattern excludePattern, Probe probe) {
        this.currentEventMetadata = manager.getServices().get(CurrentEventMetadata.class);
        this.manager = manager;
        this.excludePattern = excludePattern;
        this.probe = probe;
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
        if (excludePattern != null && excludePattern.matcher(Formats.formatType(metadata.getType(), false)).matches()) {
            ProbeLogger.LOG.eventExcluded(metadata.getType());
            return;
        }
        boolean containerEvent = isContainerEvent(metadata.getQualifiers());
        List<ObserverMethod<?>> observers = resolveObservers(metadata, containerEvent);
        EventInfo info = new EventInfo(metadata.getType(), metadata.getQualifiers(), event, metadata.getInjectionPoint(), observers, containerEvent,
                System.currentTimeMillis());
        probe.addEvent(info);
    }

    private List<ObserverMethod<?>> resolveObservers(EventMetadata metadata, boolean containerEvent) {
        List<ObserverMethod<?>> observers = new ArrayList<ObserverMethod<?>>();
        final ObserverNotifier notifier = (containerEvent) ? manager.getAccessibleLenientObserverNotifier() : manager.getGlobalLenientObserverNotifier();
        ResolvedObservers<?> resolvedObservers = notifier.resolveObserverMethods(metadata.getType(), metadata.getQualifiers());
        for (ObserverMethod<?> observer : resolvedObservers.getAllObservers()) {
            // do not show ProbeObserver
            if (getBeanClass() != observer.getBeanClass()) {
                observers.add(observer);
            }
        }
        return ImmutableList.copyOf(observers);
    }

    @Override
    public int getPriority() {
        return Interceptor.Priority.PLATFORM_BEFORE + PRIORITY_OFFSET;
    }

    private boolean isContainerEvent(Set<Annotation> qualifiers) {
        for (Annotation annotation : qualifiers) {
            if (annotation.annotationType() == Initialized.class || annotation.annotationType() == Destroyed.class
                    || annotation.annotationType() == BeforeDestroyed.class) {
                return true;
            }
        }
        return false;
    }
}
