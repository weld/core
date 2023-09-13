/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.module.jta;

import static jakarta.transaction.Status.STATUS_COMMITTED;

import jakarta.enterprise.event.TransactionPhase;
import jakarta.transaction.Synchronization;

/**
 * An enumeration of the possible outcomes for a transaction. This is used
 * to keep track of whether an observer wants to see all events regardless of
 * the outcome of the transaction or only those transactions which succeed or
 * fail.
 *
 * @author David Allen
 */
enum Status {

    ALL {
        @Override
        public boolean matches(int status) {
            return true;
        }
    },
    SUCCESS {
        @Override
        public boolean matches(int status) {
            return status == STATUS_COMMITTED;
        }
    },
    FAILURE {
        @Override
        public boolean matches(int status) {
            return status != STATUS_COMMITTED;
        }
    };

    /**
     * Indicates whether the given status code passed in during {@link Synchronization#beforeCompletion()} or
     * {@link Synchronization#afterCompletion(int)}
     * matches this status.
     *
     * @param status the given status code
     * @return true if the status code matches
     */
    public abstract boolean matches(int status);

    public static Status valueOf(TransactionPhase transactionPhase) {
        if (transactionPhase == TransactionPhase.BEFORE_COMPLETION || transactionPhase == TransactionPhase.AFTER_COMPLETION) {
            return Status.ALL;
        }
        if (transactionPhase == TransactionPhase.AFTER_SUCCESS) {
            return Status.SUCCESS;
        }
        if (transactionPhase == TransactionPhase.AFTER_FAILURE) {
            return Status.FAILURE;
        }
        throw new IllegalArgumentException("Unknown transaction phase " + transactionPhase);
    }

}
