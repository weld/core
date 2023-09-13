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
package org.jboss.weld.event;

import static org.jboss.weld.util.collections.ImmutableList.copyOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.util.Observers;
import org.jboss.weld.util.collections.ImmutableList;

/**
 * Immutable information about observer methods resolved for a type/qualifiers combination.
 *
 * @author Jozef Hartinger
 *
 * @param <T> the event type
 */
public class ResolvedObservers<T> {

    private static final ResolvedObservers<Object> EMPTY = new ResolvedObservers<Object>(Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList(), false) {
        public boolean isEmpty() {
            return true;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> ResolvedObservers<T> of(List<ObserverMethod<? super T>> observers) {
        if (observers.isEmpty()) {
            return (ResolvedObservers<T>) EMPTY;
        }
        boolean metadataRequired = false;
        List<ObserverMethod<? super T>> immediateSyncObservers = new ArrayList<ObserverMethod<? super T>>();
        List<ObserverMethod<? super T>> transactionObservers = new ArrayList<ObserverMethod<? super T>>();
        List<ObserverMethod<? super T>> asyncObservers = new ArrayList<ObserverMethod<? super T>>();
        for (ObserverMethod<? super T> observer : observers) {
            if (observer.isAsync()) {
                asyncObservers.add(observer);
            } else if (TransactionPhase.IN_PROGRESS == observer.getTransactionPhase()) {
                immediateSyncObservers.add(observer);
            } else {
                transactionObservers.add(observer);
            }
            if (!metadataRequired && Observers.isEventMetadataRequired(observer)) {
                metadataRequired = true;
            }
        }
        return new ResolvedObservers<>(copyOf(immediateSyncObservers), copyOf(asyncObservers), copyOf(transactionObservers),
                metadataRequired);
    }

    private final List<ObserverMethod<? super T>> immediateSyncObservers;
    private final List<ObserverMethod<? super T>> asyncObservers;
    private final List<ObserverMethod<? super T>> transactionObservers;
    private final boolean metadataRequired;

    private ResolvedObservers(List<ObserverMethod<? super T>> immediateSyncObservers,
            List<ObserverMethod<? super T>> asyncObservers, List<ObserverMethod<? super T>> transactionObservers,
            boolean metadataRequired) {
        this.immediateSyncObservers = immediateSyncObservers;
        this.asyncObservers = asyncObservers;
        this.transactionObservers = transactionObservers;
        this.metadataRequired = metadataRequired;
    }

    /**
     *
     * @return the list of sync immediate observers
     */
    List<ObserverMethod<? super T>> getImmediateSyncObservers() {
        return immediateSyncObservers;
    }

    /**
     *
     * @return the list of sync transactional observers
     */
    List<ObserverMethod<? super T>> getTransactionObservers() {
        return transactionObservers;
    }

    /**
     *
     * @return the list of async observers
     */
    List<ObserverMethod<? super T>> getAsyncObservers() {
        return asyncObservers;
    }

    /**
     * Indicates whether any of the resolved observer methods is either an extension-provided one or declares an explicit
     * {@link EventMetadata} injection point.
     *
     * @return true iff any of the resolved observer methods requires event metadata
     */
    boolean isMetadataRequired() {
        return metadataRequired;
    }

    /**
     * Indicates whether this object represents an empty set of observer methods.
     *
     * @return true iff this object represents an empty set of observer methods
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns all observer methods. First part of the list consists of the ordered sequence of
     * {@link TransactionPhase#IN_PROGRESS} observers followed by the
     * ordered sequence of async obervers followed by an ordered sequence of transactional observers.
     */
    public List<ObserverMethod<? super T>> getAllObservers() {
        return ImmutableList.<ObserverMethod<? super T>> builder().addAll(immediateSyncObservers).addAll(asyncObservers)
                .addAll(transactionObservers).build();
    }
}
