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

import javax.transaction.Synchronization;

import static javax.transaction.Status.STATUS_COMMITTED;

/**
 * A JTA transaction sychronization which wraps a Runnable.
 *
 * @author David Allen
 */
public class TransactionSynchronizedRunnable implements Synchronization {
    private final Status desiredStatus;
    private final Runnable task;
    private final boolean before;

    public TransactionSynchronizedRunnable(Runnable task, boolean before) {
        this(task, Status.ALL, before);
    }

    public TransactionSynchronizedRunnable(Runnable task, Status desiredStatus) {
        this(task, desiredStatus, false); // Status is only applicable after the transaction
    }

    private TransactionSynchronizedRunnable(Runnable task, Status desiredStatus, boolean before) {
        this.task = task;
        this.desiredStatus = desiredStatus;
        this.before = before;
    }

    /*
    * (non-Javadoc)
    * @see javax.transaction.Synchronization#afterCompletion(int)
    */
    public void afterCompletion(int status) {
        if (!before) {
            if ((desiredStatus == Status.SUCCESS && status == STATUS_COMMITTED) || (desiredStatus == Status.FAILURE && status != STATUS_COMMITTED) || (desiredStatus == Status.ALL)) {
                task.run();
            }
        }
    }

    /*
    * (non-Javadoc)
    * 
    * @see javax.transaction.Synchronization#beforeCompletion()
    */
    public void beforeCompletion() {
        if (before) {
            task.run();
        }
    }
}
