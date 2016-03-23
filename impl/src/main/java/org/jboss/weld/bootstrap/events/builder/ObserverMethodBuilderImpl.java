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
package org.jboss.weld.bootstrap.events.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.builder.ObserverMethodConfigurator;

import org.jboss.weld.event.SyntheticObserverMethod;
import org.jboss.weld.util.collections.ImmutableSet;

/**
 *
 * @author Martin Kouba
 */
public class ObserverMethodBuilderImpl<T> {
    // implements ObserverMethodBuilder<T> {

    private final ObserverMethodConfiguratorImpl<T> configurator;

    /**
     *
     * @param configurator
     */
    public ObserverMethodBuilderImpl(ObserverMethodConfiguratorImpl<T> configurator) {
        this.configurator = configurator;
    }

    // @Override
    public ObserverMethodConfigurator<T> configure() {
        return configurator;
    }

    // @Override
    public ObserverMethod<T> build() {
        return new ImmutableObserverMethod<>(configurator);
    }

    /**
     *
     * @author Martin Kouba
     *
     * @param <T>
     */
    static class ImmutableObserverMethod<T> implements SyntheticObserverMethod<T> {

        private final Class<?> beanClass;

        private final Type observedType;

        private final Set<Annotation> observedQualifiers;

        private final Reception reception;

        private final TransactionPhase txPhase;

        private final int priority;

        private final boolean isAsync;

        private final NotificationCallback<T> notificationCallback;

        /**
         *
         * @param configurator
         */
        ImmutableObserverMethod(ObserverMethodConfiguratorImpl<T> configurator) {
            this.beanClass = configurator.getBeanClass();
            this.observedType = configurator.getObservedType();
            this.observedQualifiers = ImmutableSet.copyOf(configurator.getObservedQualifiers());
            this.reception = configurator.getReception();
            this.txPhase = configurator.getTxPhase();
            this.priority = configurator.getPriority();
            this.isAsync = configurator.isAsync();
            this.notificationCallback = NotificationCallback.from(configurator);
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Type getObservedType() {
            return observedType;
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return observedQualifiers;
        }

        @Override
        public Reception getReception() {
            return reception;
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return txPhase;
        }

        @Override
        public void notify(T event) {
            notificationCallback.notify(event, null);
        }

        @Override
        public boolean isAsync() {
            return isAsync;
        }

        @Override
        public void notify(T event, EventMetadata eventMetadata) {
            notificationCallback.notify(event, eventMetadata);
        }

        @Override
        public boolean isEventMetadataRequired() {
            return notificationCallback.isMetadataRequired();
        }

    }

    public static final class NotificationCallback<T> {

        private final Consumer<T> notifySimple;

        private final BiConsumer<T, EventMetadata> notifyMetadata;

        static <T> NotificationCallback<T> from(ObserverMethodConfiguratorImpl<T> configurator) {
            return configurator.getNotifySimple() != null ? new NotificationCallback<>(configurator.getNotifySimple(), null)
                    : new NotificationCallback<>(null, configurator.getNotifyMetadata());
        }

        private NotificationCallback(Consumer<T> notifySimple, BiConsumer<T, EventMetadata> notifyMetadata) {
            this.notifySimple = notifySimple;
            this.notifyMetadata = notifyMetadata;
        }

        void notify(T event, EventMetadata metadata) {
            if (notifySimple != null) {
                notifySimple.accept(event);
            } else {
                notifyMetadata.accept(event, metadata);
            }
        }

        boolean isMetadataRequired() {
            return notifyMetadata != null;
        }

    }

}
