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
import java.util.List;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.event.TransactionNotificationSynchronization.TransactionNotification;
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

    @Override
    public <T> void notifyObservers(EventPacket<T> eventPacket, List<ObserverMethod<? super T>> observers) {

        if(transactionServices == null || !transactionServices.isTransactionActive()) {
            // Transaction is not active - no deferred notifications
            super.notifyObservers(eventPacket, observers);
        } else {
            List<TransactionNotificationSynchronization.TransactionNotification> notifications = new ArrayList<TransactionNotificationSynchronization.TransactionNotification>();
            currentEventMetadata.push(eventPacket);
            try {
                for (ObserverMethod<? super T> observer : observers) {
                    if(TransactionPhase.IN_PROGRESS.equals(observer.getTransactionPhase())) {
                        super.notifyObserver(eventPacket, observer);
                    } else {
                        deferNotification(eventPacket, observer, notifications);
                    }
                }
            } finally {
                currentEventMetadata.pop();
            }
            if (!notifications.isEmpty()) {
                transactionServices.registerSynchronization(new TransactionNotificationSynchronization(notifications));
            }
        }
    }

    /**
     * Defers an event for processing in a later phase of the current
     * transaction.
     *
     * @param eventPacket The event object
     */
    private <T> void deferNotification(final EventPacket<T> packet, final ObserverMethod<? super T> observer, final List<TransactionNotificationSynchronization.TransactionNotification> notifications) {
        DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(contextId, packet, observer, currentEventMetadata);
        TransactionPhase transactionPhase = observer.getTransactionPhase();
        if (transactionPhase.equals(TransactionPhase.BEFORE_COMPLETION)) {
            notifications.add(new TransactionNotification(deferredEvent, true));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_COMPLETION)) {
            notifications.add(new TransactionNotification(deferredEvent, false));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_SUCCESS)) {
            notifications.add(new TransactionNotification(deferredEvent, Status.SUCCESS));
        } else if (transactionPhase.equals(TransactionPhase.AFTER_FAILURE)) {
            notifications.add(new TransactionNotification(deferredEvent, Status.FAILURE));
        }
    }
}
