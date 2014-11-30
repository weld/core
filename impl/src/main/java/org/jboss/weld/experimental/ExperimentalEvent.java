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
package org.jboss.weld.experimental;

import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Event;
import javax.enterprise.event.TransactionPhase;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link Event}.
 *
 * @author Jozef Hartinger
 * @seeIssue WELD-1793
 *
 * @param <X> the event type
 */
public interface ExperimentalEvent<T> extends Event<T> {

    /**
     * <p>
     * Fires an event with the specified qualifiers and notifies observers asynchronously.
     * </p>
     * <p>
     * Observers bound to a transaction phase are registered to be executed in the corresponding transaction phase.
     * Observers with {@link TransactionPhase#IN_PROGRESS} are notified in a configured thread pool. The ordering of
     * observers is preserved. The message may be mutable and is guaranteed to be safely propagated between observers.
     * </p>
     * If there are multiple observers for the given event, a given observer is guaranteed to observe the event in a consistent
     * state in which is:
     * <ul>
     * <li>the state in which the event was left in by an observer executing before the given observer, or</li>
     * <li>the initial state of the event if the given observer is the first one</li>
     * </ul>
     * <p>
     * The returned {@link CompletionStage} allows actions, which execute upon termination of this asynchronous dispatch, to be registered.
     * </p>
     *
     * @see CompletionStage
     * @param event the event object
     * @throws IllegalArgumentException if the runtime type of the event object contains a type variable
     * @return completion stage which allows additional actions to be bound to the asynchronous event dispatch
     */
    <U extends T> CompletionStage<U> fireAsync(U event);
}
