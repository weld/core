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

import java.util.List;

import javax.transaction.Synchronization;

/**
 * A JTA transaction synchronization which wraps all defferred transactional event notifications.
 *
 * @author David Allen
 */
class TransactionNotificationSynchronization implements Synchronization {

    private final List<DeferredEventNotification<?>> notifications;

    /**
     *
     * @param notifications The ordered list of notifications
     */
    public TransactionNotificationSynchronization(List<DeferredEventNotification<?>> notifications) {
        this.notifications = notifications;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.transaction.Synchronization#afterCompletion(int)
     */
    public void afterCompletion(int status) {
        for (DeferredEventNotification<?> notification : notifications) {
            if (!notification.isBefore() && notification.getStatus().matches(status)) {
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
        for (DeferredEventNotification<?> notification : notifications) {
            if (notification.isBefore()) {
                notification.run();
            }
        }
    }
}
