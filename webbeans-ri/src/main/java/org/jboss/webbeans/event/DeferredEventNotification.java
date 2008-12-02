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

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.webbeans.Observer;

import static org.jboss.webbeans.event.EventManager.TransactionObservationPhase.AFTER_COMPLETION;
import static org.jboss.webbeans.event.EventManager.TransactionObservationPhase.AFTER_SUCCESS;
import static org.jboss.webbeans.event.EventManager.TransactionObservationPhase.AFTER_FAILURE;
import static org.jboss.webbeans.event.EventManager.TransactionObservationPhase.BEFORE_COMPLETION;

/**
 * A synchronization object which will deliver the event to the observer after
 * the JTA transaction currently in effect is committed.
 * 
 * @author David Allen
 * @see javax.transaction.Synchronization
 */
public class DeferredEventNotification<T> implements Synchronization
{
   // The observer
   private ObserverImpl<T> observer;
   // The event object
   private T event;

   /**
    * Creates a new deferred event notifier.
    * 
    * @param manager The Web Beans manager
    * @param observer The observer to be notified
    * @param event The event being fired
    */
   public DeferredEventNotification(T event, Observer<T> observer)
   {
      this.observer = (ObserverImpl<T>) observer;
      this.event = event;
   }

   /**
    * Gets the observer
    * 
    * @return the observer
    */
   public final Observer<T> getObserver()
   {
      return observer;
   }

   /**
    * Called after completion of a transaction
    * 
    * Checks if the observer is interested in this particular transaction phase
    * and if so, notifies the observer.
    * 
    * @param status The status of the transaction
    * @see javax.transaction.Status
    */
   public void afterCompletion(int status)
   {
      if (observer.isInterestedInTransactionPhase(AFTER_COMPLETION))
      {
         observer.notify(event);
      }
      switch (status)
      {
      case Status.STATUS_COMMITTED:
         if (observer.isInterestedInTransactionPhase(AFTER_SUCCESS))
         {
            observer.notify();
         }
         break;
      case Status.STATUS_ROLLEDBACK:
         if (observer.isInterestedInTransactionPhase(AFTER_FAILURE))
         {
            observer.notify();
         }
         break;
      }
   }

   /**
    * Called before completion of a transaction
    * 
    * Checks if the observer is interested in this particular transaction phase
    * and if so, notifies the observer.
    */
   public void beforeCompletion()
   {
      if (observer.isInterestedInTransactionPhase(BEFORE_COMPLETION))
      {
         observer.notify(event);
      }
   }
}
