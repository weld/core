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

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * {@link ObserverNotifier} whith support for transactional observer methods.
 *
 * @author Jozef Hartinger
 *
 */
public class TransactionalObserverNotifier extends ObserverNotifier {

    private final TransactionServices transactionServices;

    protected TransactionalObserverNotifier(TypeSafeObserverResolver resolver, BeanManagerImpl beanManager) {
        super(resolver, beanManager);
        this.transactionServices = beanManager.getServices().get(TransactionServices.class);
    }

    @Override
    protected <T> void notifyObserver(final T event, final ObserverMethod<? super T> observer) {
        if (immediateDispatch(observer)) {
            super.notifyObserver(event, observer);
        } else {
            deferNotification(event, observer);
        }
    }

    private boolean immediateDispatch(ObserverMethod<?> observer) {
        return TransactionPhase.IN_PROGRESS.equals(observer.getTransactionPhase()) || transactionServices == null || !transactionServices.isTransactionActive();
    }

    /**
     * Defers an event for processing in a later phase of the current
     * transaction.
     *
     * @param event The event object
     */
    private <T> void deferNotification(final T event, final ObserverMethod<? super T> observer) {
        DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, observer, beanManager.getContextId());
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
}
