/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.transaction.RollbackException;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * {@link ObserverNotifier} with support for transactional observer methods.
 *
 * @author Jozef Hartinger
 */
public class TransactionalObserverNotifier extends ObserverNotifier {

    private final TransactionServices transactionServices;
    private final String contextId;

    protected TransactionalObserverNotifier(String contextId, TypeSafeObserverResolver resolver, ServiceRegistry services, boolean strict) {
        super(resolver, services, strict);
        this.contextId = contextId;
        this.transactionServices = services.get(TransactionServices.class);
    }

    /**
     * Defers an event for processing in a later phase of the current
     * transaction.
     *
     * @param metadata The event object
     */
    private <T> void deferNotification(T event, final EventMetadata metadata, final ObserverMethod<? super T> observer) {
        DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(contextId, event, metadata, observer, currentEventMetadata);
        TransactionPhase transactionPhase = observer.getTransactionPhase();

        if (transactionPhase.equals(TransactionPhase.BEFORE_COMPLETION)) {
            transactionServices.registerSynchronization(new TransactionSynchronizedRunnable(deferredEvent, true));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_COMPLETION)) {
            transactionServices.registerSynchronization(new TransactionSynchronizedRunnable(deferredEvent, false));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_SUCCESS)) {
            transactionServices.registerSynchronization(new TransactionSynchronizedRunnable(deferredEvent, Status.SUCCESS));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_FAILURE)) {
            transactionServices.registerSynchronization(new TransactionSynchronizedRunnable(deferredEvent, Status.FAILURE));
        }
    }

    @Override
    protected <T> void notifyTransactionObservers(List<ObserverMethod<? super T>> observers, T event, EventMetadata metadata) {
        if (observers.isEmpty()) {
            return;
        }
        if (transactionServices == null || !transactionServices.isTransactionActive()) {
            // Transaction is not active - no deferred notifications
            notifySyncObservers(observers, event, metadata);
        } else {
            List<ObserverMethod<? super T>> failedObservers = new ArrayList<>(observers);
            try {
                for (ObserverMethod<? super T> observer : observers) {
                    deferNotification(event, metadata, observer);
                    failedObservers.remove(observer);
                }
            } catch (Exception e) {
                if (e.getCause() instanceof RollbackException || e.getCause() instanceof IllegalStateException) {

                    List<ObserverMethod<? super T>> filteredObservers = new ArrayList<>(failedObservers);
                    for (Iterator<ObserverMethod<? super T>> it = filteredObservers.iterator(); it.hasNext(); ) {
                        ObserverMethod<? super T> observerMethod = it.next();
                        if (observerMethod.getTransactionPhase().equals(TransactionPhase.AFTER_SUCCESS)) {
                            it.remove();
                        }
                    }

                    Collections.sort(filteredObservers, new Comparator<ObserverMethod<? super T>>() {
                        @Override
                        public int compare(ObserverMethod<? super T> o1, ObserverMethod<? super T> o2) {
                            // using descending order since we only need to ensure that BEFORE_COMPLETION precedes AFTER_COMPLETION
                            return o2.getTransactionPhase().toString().compareTo(o1.getTransactionPhase().toString());
                        }
                    });
                    notifySyncObservers(filteredObservers, event, metadata);
                } else {
                    throw e;
                }

            }
        }
    }
}
