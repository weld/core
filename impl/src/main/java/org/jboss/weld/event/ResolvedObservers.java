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

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.util.collections.ImmutableList;

public class ResolvedObservers<T> {

    private static final ResolvedObservers<Object> EMPTY = new ResolvedObservers<Object>(Collections.emptyList(), Collections.emptyList()) {
        public boolean isEmpty() {
            return true;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> ResolvedObservers<T> of(List<ObserverMethod<? super T>> observers) {
        if (observers.isEmpty()) {
            return (ResolvedObservers<T>) EMPTY;
        }
        List<ObserverMethod<? super T>> immediateObservers = new ArrayList<ObserverMethod<? super T>>();
        List<ObserverMethod<? super T>> transactionObservers = new ArrayList<ObserverMethod<? super T>>();
        for (ObserverMethod<? super T> observer : observers) {
            if (TransactionPhase.IN_PROGRESS == observer.getTransactionPhase()) {
                immediateObservers.add(observer);
            } else {
                transactionObservers.add(observer);
            }
        }
        return new ResolvedObservers<>(copyOf(immediateObservers), copyOf(transactionObservers));
    }

    private final List<ObserverMethod<? super T>> immediateObservers;
    private final List<ObserverMethod<? super T>> transactionObservers;

    private ResolvedObservers(List<ObserverMethod<? super T>> immediateObservers, List<ObserverMethod<? super T>> transactionObservers) {
        this.immediateObservers = immediateObservers;
        this.transactionObservers = transactionObservers;
    }

    List<ObserverMethod<? super T>> getImmediateObservers() {
        return immediateObservers;
    }

    List<ObserverMethod<? super T>> getTransactionObservers() {
        return transactionObservers;
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns all observer methods. First part of the list consists of the ordered sequence of {@link TransactionPhase#IN_PROGRESS} observers followed by
     * an ordered sequence of transactional observers.
     * TODO: we may need to preserve ordering of the entire list
     */
    public List<ObserverMethod<? super T>> getAllObservers() {
        return ImmutableList.<ObserverMethod<? super T>>builder().addAll(immediateObservers).addAll(transactionObservers).build();
    }
}
