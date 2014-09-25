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
package org.jboss.weld.event;

import static javax.transaction.Status.STATUS_COMMITTED;

import java.util.List;

import javax.transaction.Synchronization;

/**
 * A JTA transaction synchronization which wraps all defferred transactional event notifications.
 *
 * @author David Allen
 */
public class TransactionNotificationSynchronization implements Synchronization {

    private final List<TransactionNotification> notifications;

    /**
     *
     * @param notifications The ordered list of notifications
     */
    public TransactionNotificationSynchronization(List<TransactionNotification> notifications) {
        this.notifications = notifications;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(int status) {
        for (TransactionNotification notification : notifications) {
            if (!notification.isBefore()
                    && ((notification.getStatus() == Status.SUCCESS && status == STATUS_COMMITTED)
                            || (notification.getStatus() == Status.FAILURE && status != STATUS_COMMITTED) || (notification.getStatus() == Status.ALL))) {
                notification.run();
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.transaction.Synchronization#beforeCompletion()
     */
    public void beforeCompletion() {
        for (TransactionNotification notification : notifications) {
            if (notification.isBefore()) {
                notification.run();
            }
        }
    }

    static class TransactionNotification {

        private final Status status;
        private final Runnable task;
        private final boolean before;

        public TransactionNotification(Runnable task, boolean before) {
            this(task, Status.ALL, before);
        }

        public TransactionNotification(Runnable task, Status desiredStatus) {
            this(task, desiredStatus, false); // Status is only applicable after the transaction
        }

        private TransactionNotification(Runnable task, Status status, boolean before) {
            this.task = task;
            this.status = status;
            this.before = before;
        }

        public Status getStatus() {
            return status;
        }

        public void run() {
            task.run();
        }

        public boolean isBefore() {
            return before;
        }

    }
}
