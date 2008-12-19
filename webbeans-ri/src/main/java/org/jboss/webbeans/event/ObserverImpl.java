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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.webbeans.AfterTransactionCompletion;
import javax.webbeans.AfterTransactionFailure;
import javax.webbeans.AfterTransactionSuccess;
import javax.webbeans.BeforeTransactionCompletion;
import javax.webbeans.DefinitionException;
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.ExecutionException;
import javax.webbeans.IfExists;
import javax.webbeans.Initializer;
import javax.webbeans.Observer;
import javax.webbeans.ObserverException;
import javax.webbeans.Observes;
import javax.webbeans.Produces;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.transaction.UserTransaction;
import org.jboss.webbeans.util.Reflections;

/**
 * <p>
 * Reference implementation for the Observer interface, which represents an
 * observer method. Each observer method has an event type which is the class of
 * the event object being observed, and event binding types that are annotations
 * applied to the event parameter to narrow the event notifications delivered.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class ObserverImpl<T> implements Observer<T>
{
   /**
    * The known transactional phases a transactional event observer can be
    * interested in
    */ 
   protected enum TransactionObservationPhase
   {
      NONE, BEFORE_COMPLETION, AFTER_COMPLETION, AFTER_FAILURE, AFTER_SUCCESS
   }   
   
   private Bean<?> observerBean;
   private final AnnotatedMethod<Object> observerMethod;
   private TransactionObservationPhase transactionObservationPhase;
   private boolean conditional;
   private ManagerImpl manager;

   
   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    * 
    * @param observer The observer
    * @param observerBean The observer bean
    * @param manager The Web Beans manager
    */
   public ObserverImpl(final AnnotatedMethod<Object> observer, final Bean<?> observerBean, final ManagerImpl manager)
   {
      this.manager = manager;
      this.observerBean = observerBean;
      this.observerMethod = observer;
      validateObserverMethod();
      initTransactionObservationPhase();
      conditional = !observerMethod.getAnnotatedParameters(IfExists.class).isEmpty();
   }

   private void initTransactionObservationPhase()
   {
      List<TransactionObservationPhase> observationPhases = new ArrayList<TransactionObservationPhase>();
      if (!observerMethod.getAnnotatedParameters(BeforeTransactionCompletion.class).isEmpty())
      {
         observationPhases.add(TransactionObservationPhase.BEFORE_COMPLETION);
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
         throw new DefinitionException("Transactional observers can only observe on a single phase");
      }
      else if (observationPhases.size() == 1)
      {
         transactionObservationPhase = observationPhases.iterator().next();
      }
      else
      {
         transactionObservationPhase = TransactionObservationPhase.NONE;
      }
   }

   /**
    * Performs validation of the observer method for compliance with the specifications.
    */
   private void validateObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<AnnotatedParameter<Object>> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(this + " is invalid because it contains more than event parameter");
      }
      // Make sure the event object above is not parameterized with a type
      // variable or wildcard
      if (eventObjects.size() > 0)
      {
         AnnotatedParameter<Object> eventParam = eventObjects.iterator().next();
         if (Reflections.isParameterizedType(eventParam.getType()))
         {
            throw new DefinitionException(this + " cannot observe parameterized event types");
         }
      }
      // Check for parameters annotated with @Disposes
      List<AnnotatedParameter<Object>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
      if (disposeParams.size() > 0)
      {
         throw new DefinitionException(this + " cannot have any parameters annotated with @Dispose");
      }
      // Check annotations on the method to make sure this is not a producer
      // method, initializer method, or destructor method.
      if ( this.observerMethod.isAnnotationPresent(Produces.class) )
      {
         throw new DefinitionException(this + " cannot be annotated with @Produces");
      }
      if (this.observerMethod.isAnnotationPresent(Initializer.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Initializer");
      }
      if ( this.observerMethod.isAnnotationPresent(Destructor.class) )
      {
         throw new DefinitionException(this + " cannot be annotated with @Destructor");
      }
   }

   public void notify(final T event)
   {
      // Get the most specialized instance of the component
      Object instance = getInstance(!isConditional());
      if (instance != null)
      {
         try
         {
            if ( isTransactional() && isTransactionActive() )
            {
               deferEvent(event);
            }
            else
            {
               observerMethod.invokeWithSpecialValue(instance, Observes.class, event, manager);
            }
         }
         catch (ExecutionException e)
         {
            if ((e.getCause() != null) && (e.getCause() instanceof InvocationTargetException))
            {
               InvocationTargetException wrappedException = (InvocationTargetException) e.getCause();
               if ((wrappedException.getCause() != null) && (RuntimeException.class.isAssignableFrom(wrappedException.getCause().getClass())))
               {
                  throw (RuntimeException) wrappedException.getCause();
               }
               else
               {
                  throw new ObserverException(wrappedException.getCause().getMessage(), wrappedException.getCause());
               }
            }
         }
      }
   }

   /**
    * Uses the container to retrieve the most specialized instance of this
    * observer.
    * 
    * @param create True if the instance should be created if not already done
    * 
    * @return the most specialized instance
    */
   protected Object getInstance(boolean create)
   {
      // Return the most specialized instance of the component
      return manager.getMostSpecializedInstance(observerBean, create);
   }

   /**
    * Checks if there is currently a transaction active
    * 
    * @return True if there is one, false otherwise
    */
   private boolean isTransactionActive()
   {
      UserTransaction userTransaction = manager.getInstanceByType(UserTransaction.class);
      try
      {
         return userTransaction!=null && userTransaction.getStatus() == Status.STATUS_ACTIVE;
      }
      catch (SystemException e)
      {
         return false;
      }
   }

   /**
    * Defers an event for processing in a later phase of the current transaction.
    * 
    * Gets the transaction listener, creates a deferred event representation and
    * registers the deferred event.
    * 
    * @param event The event type
    */
   private void deferEvent(T event)
   {
      UserTransaction userTransaction = manager.getInstanceByType(UserTransaction.class);
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, this);
      userTransaction.registerSynchronization(deferredEvent);
   }

   /**
    * Indicates if the observer is transactional
    * 
    * @return True if transactional, false otherwise
    */
   public boolean isTransactional()
   {
      return !TransactionObservationPhase.NONE.equals(transactionObservationPhase);
   }

   /**
    * Indicates if the observer is conditional
    * 
    * @return True if conditional, false otherwise
    */
   public boolean isConditional()
   {
      return conditional;
   }

   /**
    * Checks if the observer is interested in a particular transactional phase
    * 
    * @param currentPhase The phase to check
    * @return True if interested, false otherwise
    */
   public boolean isInterestedInTransactionPhase(TransactionObservationPhase currentPhase)
   {
      return transactionObservationPhase.equals(currentPhase);
   }
   
   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("Observer Implentation: \n");
      builder.append("  Observer (Declaring) bean: " + observerBean);
      builder.append("  Observer method: " + observerMethod);
      return builder.toString();
   }

}
