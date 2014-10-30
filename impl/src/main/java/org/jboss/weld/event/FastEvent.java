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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * An optimized internal facility for dispatching events.
 *
 * <p>
 * FastEvent eliminates most of the overhead associated with request dispatching by resolving observer methods upfront. It is therefore suitable for cases when
 * certain event is dispatched repeatedly. A FastEvent instance should be created once for a given event type / qualifiers combination and then reused every
 * time a given event is dispatched.
 * </p>
 *
 * <p>
 * FastEvent provides a subset of functionality provided by {@link Event} and these additional constraints apply to its usage:
 * </p>
 *
 * <ul>
 * <li>Event type and qualifiers must be known at FastEvent construction time. The actual type of the event object passed to the {@link #fire(Object)} method is
 * not considered for observer method resolution.</li>
 * <li>Events dispatched using FastEvent are always delivered immediately. If an observer method is transactional it will be notified immediately and not during
 * the matching transaction phase.</li>
 * <li>FastEvent is not serializable</li>
 * </ul>
 *
 * <p>
 * These constraints should always be carefully considered when deciding whether to use FastEvent or not. FastEvent is an internal construct and <strong>should
 * not</strong> be used by an application.
 * </p>
 *
 * @author Jozef Hartinger
 *
 * @param <T> event type
 */
public class FastEvent<T> {

    @SuppressWarnings("serial")
    private static final Type EVENT_METADATA_INSTANCE_TYPE = new TypeLiteral<Instance<EventMetadata>>() {
    }.getType();

    /**
     * Determines whether any of the resolved observer methods is either extension-provided or contains an injection point with {@link EventMetadata} type.
     */
    private static boolean isMetadataRequired(List<? extends ObserverMethod<?>> resolvedObserverMethods) {
        for (ObserverMethod<?> observer : resolvedObserverMethods) {
            if (observer instanceof ObserverMethodImpl<?, ?>) {
                ObserverMethodImpl<?, ?> observerImpl = (ObserverMethodImpl<?, ?>) observer;
                for (WeldInjectionPointAttributes<?, ?> ip : observerImpl.getInjectionPoints()) {
                    Type type = ip.getType();
                    if (EventMetadata.class.equals(type) || EVENT_METADATA_INSTANCE_TYPE.equals(type)) {
                        return true;
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Same as {@link #of(Class, BeanManagerImpl, Annotation...)}, just the accessible lenient observer notifier is used for observer method resolution
     */
    public static <T> FastEvent<T> of(Class<T> type, BeanManagerImpl manager, Annotation... qualifiers) {
        return of(type, manager, manager.getAccessibleLenientObserverNotifier(), qualifiers);
    }

    /**
     * Constructs a new FastEvent instance
     * @param type the event type
     * @param manager the bean manager
     * @param notifier the notifier to be used for observer method resolution
     * @param qualifiers the event qualifiers
     * @return
     */
    public static <T> FastEvent<T> of(Class<T> type, BeanManagerImpl manager, ObserverNotifier notifier, Annotation... qualifiers) {
        List<ObserverMethod<? super T>> resolvedObserverMethods = notifier.<T> resolveObserverMethods(notifier.buildEventResolvable(type, qualifiers));
        if (isMetadataRequired(resolvedObserverMethods)) {
            EventMetadata metadata = new EventMetadataImpl(type, qualifiers);
            CurrentEventMetadata metadataService = manager.getServices().get(CurrentEventMetadata.class);
            return new FastEventWithMetadataPropagation<T>(resolvedObserverMethods, metadata, metadataService);
        } else {
            return new FastEvent<T>(resolvedObserverMethods);
        }
    }

    private final List<ObserverMethod<? super T>> resolvedObserverMethods;

    private FastEvent(List<ObserverMethod<? super T>> resolvedObserverMethods) {
        this.resolvedObserverMethods = resolvedObserverMethods;
    }

    public void fire(T event) {
        for (ObserverMethod<? super T> observer : resolvedObserverMethods) {
            observer.notify(event);
        }
    }

    private static class FastEventWithMetadataPropagation<T> extends FastEvent<T> {

        private final EventMetadata metadata;
        private final CurrentEventMetadata metadataService;

        private FastEventWithMetadataPropagation(List<ObserverMethod<? super T>> resolvedObserverMethods, EventMetadata metadata,
                CurrentEventMetadata metadataService) {
            super(resolvedObserverMethods);
            this.metadata = metadata;
            this.metadataService = metadataService;
        }

        @Override
        public void fire(T event) {
            if (metadata != null) {
                metadataService.push(metadata);
            }
            try {
                super.fire(event);
            } finally {
                if (metadata != null) {
                    metadataService.pop();
                }
            }
        }
    }

    private static class EventMetadataImpl implements EventMetadata {

        private final Set<Annotation> qualifiers;
        private final Type type;

        private EventMetadataImpl(Type type, Annotation... qualifiers) {
            this.type = type;
            this.qualifiers = ImmutableSet.of(qualifiers);
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return qualifiers;
        }

        @Override
        public InjectionPoint getInjectionPoint() {
            return null;
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
