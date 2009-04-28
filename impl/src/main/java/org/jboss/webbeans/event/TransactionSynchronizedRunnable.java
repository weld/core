/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.event;

import static javax.transaction.Status.STATUS_COMMITTED;

import javax.transaction.Synchronization;

import org.jboss.webbeans.transaction.spi.TransactionServices;

/**
 * A JTA transaction sychronization which wraps a Runnable.
 * 
 * @author David Allen
 * 
 */
public class TransactionSynchronizedRunnable implements Synchronization
{
   private final TransactionServices.Status desiredStatus;
   private final Runnable task;
   private final boolean before;

   public TransactionSynchronizedRunnable(Runnable task, boolean before)
   {
      this(task, TransactionServices.Status.ALL, before);
   }

   public TransactionSynchronizedRunnable(Runnable task, TransactionServices.Status desiredStatus)
   {
      this(task, desiredStatus, false); // Status is only applicable after the transaction
   }

   private TransactionSynchronizedRunnable(Runnable task, TransactionServices.Status desiredStatus, boolean before)
   {
      this.task = task;
      this.desiredStatus = desiredStatus;
      this.before = before;
   }

   /*
   * (non-Javadoc)
   *
   * @see javax.transaction.Synchronization#afterCompletion(int)
   */
   public void afterCompletion(int status)
   {
      if ((desiredStatus == TransactionServices.Status.SUCCESS && status == STATUS_COMMITTED) || (desiredStatus == TransactionServices.Status.FAILURE && status != STATUS_COMMITTED) || (desiredStatus == TransactionServices.Status.ALL))
      {
         task.run();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.transaction.Synchronization#beforeCompletion()
    */
   public void beforeCompletion()
   {
      if (before)
      {
         task.run();
      }
   }
}
