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
package org.jboss.weld.event;

import javax.enterprise.event.TransactionPhase;
import javax.transaction.Synchronization;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * @author David Allen
 * 
 */
class TransactionalObserverMethodImpl<T, X> extends ObserverMethodImpl<T, X>
{

   /**
    * Creates a new instance of a transactional observer method implicit object.
    * 
    * @param observer The observer method
    * @param observerBean The bean declaring the observer method
    * @param manager The JCDI manager in use
    */
   protected TransactionalObserverMethodImpl(WeldMethod<T, X> observer, RIBean<X> observerBean, TransactionPhase transactionPhase, BeanManagerImpl manager)
   {
      super(observer, observerBean, manager);
      this.transactionPhase = transactionPhase;
   }

   @Override
   public void initialize()
   {
      super.initialize();
   }

   @Override
   public void notify(T event)
   {
      if (ignore(event))
      {
         return;
      }
      if ((manager.getServices().get(TransactionServices.class) != null)  && (manager.getServices().get(TransactionServices.class).isTransactionActive()))
      {
         deferEvent(event);
      }
      else
      {
         sendEvent(event);
      }
   }

   /**
    * Defers an event for processing in a later phase of the current
    * transaction.
    * 
    * @param event The event object
    */
   private void deferEvent(T event)
   {
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, this);;

      Synchronization synchronization = null;
      if (transactionPhase.equals(TransactionPhase.BEFORE_COMPLETION))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, true);
      }
      else if (transactionPhase.equals(TransactionPhase.AFTER_COMPLETION))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, false);
      }
      else if (transactionPhase.equals(TransactionPhase.AFTER_SUCCESS))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, Status.SUCCESS);
      }
      else if (transactionPhase.equals(TransactionPhase.AFTER_FAILURE))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, Status.FAILURE);
      }
      manager.getServices().get(TransactionServices.class).registerSynchronization(synchronization);
   }

}
