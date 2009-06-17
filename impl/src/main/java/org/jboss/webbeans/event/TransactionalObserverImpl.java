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

import java.util.ArrayList;
import java.util.List;

import javax.event.AfterTransactionCompletion;
import javax.event.AfterTransactionFailure;
import javax.event.AfterTransactionSuccess;
import javax.event.Asynchronously;
import javax.event.BeforeTransactionCompletion;
import javax.transaction.Synchronization;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.transaction.spi.TransactionServices;

/**
 * @author David Allen
 * 
 */
class TransactionalObserverImpl<T> extends ObserverImpl<T>
{
   /**
    * The known transactional phases a transactional event observer can be
    * interested in
    */
   protected enum TransactionObservationPhase
   {
      BEFORE_COMPLETION, AFTER_COMPLETION, AFTER_FAILURE, AFTER_SUCCESS
   }

   private TransactionObservationPhase transactionObservationPhase;

   /**
    * Tests an observer method to see if it is transactional.
    * 
    * @param observer The observer method
    * @return true if the observer method is annotated as transactional
    */
   public static boolean isObserverMethodTransactional(WBMethod<?> observer)
   {
      boolean transactional = true;
      if ((observer.getAnnotatedParameters(BeforeTransactionCompletion.class).isEmpty()) && (observer.getAnnotatedParameters(AfterTransactionCompletion.class).isEmpty()) && (observer.getAnnotatedParameters(AfterTransactionSuccess.class).isEmpty()) && (observer.getAnnotatedParameters(AfterTransactionFailure.class).isEmpty()))
      {
         transactional = false;
      }
      return transactional;
   }

   /**
    * Creates a new instance of a transactional observer method implicit object.
    * 
    * @param observer The observer method
    * @param observerBean The bean declaring the observer method
    * @param manager The JCDI manager in use
    */
   protected TransactionalObserverImpl(WBMethod<?> observer, RIBean<?> observerBean, BeanManagerImpl manager)
   {
      super(observer, observerBean, manager);
   }

   @Override
   public void initialize()
   {
      super.initialize();
      initTransactionObservationPhase();
   }

   @Override
   public boolean notify(T event)
   {
      if ((manager.getServices().get(TransactionServices.class) != null)  && (manager.getServices().get(TransactionServices.class).isTransactionActive()))
      {
         deferEvent(event);
      }
      else
      {
         sendEvent(event);
      }
      return false;
   }

   private void initTransactionObservationPhase()
   {
      List<TransactionObservationPhase> observationPhases = new ArrayList<TransactionObservationPhase>();
      if (!observerMethod.getAnnotatedParameters(BeforeTransactionCompletion.class).isEmpty())
      {
         observationPhases.add(TransactionObservationPhase.BEFORE_COMPLETION);
         if (!observerMethod.getAnnotatedParameters(Asynchronously.class).isEmpty())
         {
            throw new DefinitionException("@BeforeTransactionCompletion cannot be used with @Asynchronously on " + observerMethod);
         }
      }
      if (!observerMethod.getAnnotatedParameters(AfterTransactionCompletion.class).isEmpty())
      {
         observationPhases.add(TransactionObservationPhase.AFTER_COMPLETION);
      }
      if (!observerMethod.getAnnotatedParameters(AfterTransactionFailure.class).isEmpty())
      {
         observationPhases.add(TransactionObservationPhase.AFTER_FAILURE);
      }
      if (!observerMethod.getAnnotatedParameters(AfterTransactionSuccess.class).isEmpty())
      {
         observationPhases.add(TransactionObservationPhase.AFTER_SUCCESS);
      }
      if (observationPhases.size() > 1)
      {
         throw new DefinitionException("Transactional observers can only observe on a single phase: " + observerMethod);
      }
      else if (observationPhases.size() == 1)
      {
         transactionObservationPhase = observationPhases.iterator().next();
      }
      else
      {
         throw new IllegalStateException("This observer method is not transactional: " + observerMethod);
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
      DeferredEventNotification<T> deferredEvent = null;
      if (this.observerMethod.getAnnotatedParameters(Asynchronously.class).isEmpty())
      {
         deferredEvent = new DeferredEventNotification<T>(event, this);
      }
      else
      {
         deferredEvent = new AsynchronousTransactionalEventNotification<T>(event, this);
      }

      Synchronization synchronization = null;
      if (transactionObservationPhase.equals(TransactionObservationPhase.BEFORE_COMPLETION))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, true);
      }
      else if (transactionObservationPhase.equals(TransactionObservationPhase.AFTER_COMPLETION))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, false);
      }
      else if (transactionObservationPhase.equals(TransactionObservationPhase.AFTER_SUCCESS))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, TransactionServices.Status.SUCCESS);
      }
      else if (transactionObservationPhase.equals(TransactionObservationPhase.AFTER_FAILURE))
      {
         synchronization = new TransactionSynchronizedRunnable(deferredEvent, TransactionServices.Status.FAILURE);
      }
      manager.getServices().get(TransactionServices.class).registerSynchronization(synchronization);
   }

}
