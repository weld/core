/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.event;

import java.lang.annotation.Annotation;

import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.injection.ThreadLocalStack.ThreadLocalStackReference;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Observers;

/**
 * An optimized internal facility for dispatching events.
 *
 * <p>
 * FastEvent eliminates most of the overhead associated with request dispatching by resolving observer methods upfront. It is
 * therefore suitable for cases when
 * certain event is dispatched repeatedly. A FastEvent instance should be created once for a given event type / qualifiers
 * combination and then reused every
 * time a given event is dispatched.
 * </p>
 *
 * <p>
 * FastEvent provides a subset of functionality provided by {@link Event} and these additional constraints apply to its usage:
 * </p>
 *
 * <ul>
 * <li>Event type and qualifiers must be known at FastEvent construction time. The actual type of the event object passed to the
 * {@link #fire(Object)} method is
 * not considered for observer method resolution.</li>
 * <li>Events dispatched using FastEvent are always delivered immediately. If an observer method is transactional it will not be
 * notified</li>
 * <li>FastEvent is not serializable</li>
 * </ul>
 *
 * <p>
 * These constraints should always be carefully considered when deciding whether to use FastEvent or not. FastEvent is an
 * internal construct and <strong>should
 * not</strong> be used by an application.
 * </p>
 *
 * @author Jozef Hartinger
 *
 * @param <T> event type
 */
public class FastEvent<T> {

    /**
     * Same as {@link #of(Class, BeanManagerImpl, Annotation...)}, just the accessible lenient observer notifier is used for
     * observer method resolution
     */
    public static <T> FastEvent<T> of(Class<T> type, BeanManagerImpl manager, Annotation... qualifiers) {
        return of(type, manager, manager.getAccessibleLenientObserverNotifier(), qualifiers);
    }

    /**
     * Constructs a new FastEvent instance
     *
     * @param type the event type
     * @param manager the bean manager
     * @param notifier the notifier to be used for observer method resolution
     * @param qualifiers the event qualifiers
     * @return
     */
    public static <T> FastEvent<T> of(Class<T> type, BeanManagerImpl manager, ObserverNotifier notifier,
            Annotation... qualifiers) {
        ResolvedObservers<T> resolvedObserverMethods = notifier.<T> resolveObserverMethods(type, qualifiers);
        if (resolvedObserverMethods.isMetadataRequired()) {
            EventMetadata metadata = new EventMetadataImpl(type, null, qualifiers);
            CurrentEventMetadata metadataService = manager.getServices().get(CurrentEventMetadata.class);
            return new FastEventWithMetadataPropagation<T>(resolvedObserverMethods, metadata, metadataService);
        } else {
            return new FastEvent<T>(resolvedObserverMethods);
        }
    }

    protected final ResolvedObservers<T> resolvedObserverMethods;

    private FastEvent(ResolvedObservers<T> resolvedObserverMethods) {
        this.resolvedObserverMethods = resolvedObserverMethods;
    }

    public void fire(T event) {
        for (ObserverMethod<? super T> observer : resolvedObserverMethods.getImmediateSyncObservers()) {
            observer.notify(event);
        }
    }

    private static class FastEventWithMetadataPropagation<T> extends FastEvent<T> {

        private final EventMetadata metadata;
        private final CurrentEventMetadata metadataService;

        private FastEventWithMetadataPropagation(ResolvedObservers<T> resolvedObserverMethods, EventMetadata metadata,
                CurrentEventMetadata metadataService) {
            super(resolvedObserverMethods);
            this.metadata = metadata;
            this.metadataService = metadataService;
        }

        @Override
        public void fire(T event) {
            final ThreadLocalStackReference<EventMetadata> stack = metadataService.pushIfNotNull(metadata);
            try {
                for (ObserverMethod<? super T> observer : resolvedObserverMethods.getImmediateSyncObservers()) {
                    Observers.notify(observer, event, metadata);
                }
            } finally {
                stack.pop();
            }
        }
    }
}
