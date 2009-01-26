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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.context.Dependent;
import javax.event.AfterTransactionCompletion;
import javax.event.AfterTransactionFailure;
import javax.event.AfterTransactionSuccess;
import javax.event.BeforeTransactionCompletion;
import javax.event.IfExists;
import javax.event.Observer;
import javax.event.ObserverException;
import javax.event.Observes;
import javax.inject.DefinitionException;
import javax.inject.Disposes;
import javax.inject.ExecutionException;
import javax.inject.Initializer;
import javax.inject.Produces;
import javax.inject.manager.Bean;
import javax.transaction.Status;
import javax.transaction.SystemException;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.context.DependentContext;
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

   private final Bean<?> observerBean;
   private final AnnotatedMethod<?> observerMethod;
   private TransactionObservationPhase transactionObservationPhase;
   private final boolean conditional;
   private ManagerImpl manager;
   private final Class<T> eventType;
   private final Annotation[] bindings;

   /**
    * Creates an observer
    * 
    * @param method The observer method abstraction
    * @param declaringBean The declaring bean
    * @param manager The Web Beans manager
    * @return An observer implementation built from the method abstraction
    */
   public static <T> ObserverImpl<T> of(AnnotatedMethod<?> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      return new ObserverImpl<T>(method, declaringBean, manager);
   }

   /**
    * Creates an Observer which describes and encapsulates an observer method
    * (8.5).
    * 
    * @param observer The observer
    * @param observerBean The observer bean
    * @param manager The Web Beans manager
    */
   protected ObserverImpl(final AnnotatedMethod<?> observer, final Bean<?> observerBean, final ManagerImpl manager)
   {
      this.manager = manager;
      this.observerBean = observerBean;
      this.observerMethod = observer;
      checkObserverMethod();

      @SuppressWarnings("unchecked")
      Class<T> c = (Class<T>) observerMethod.getAnnotatedParameters(Observes.class).get(0).getType();
      this.eventType = c;

      this.bindings = observerMethod.getAnnotatedParameters(Observes.class).get(0).getBindingTypesAsArray();
      initTransactionObservationPhase();
      this.conditional = !observerMethod.getAnnotatedParameters(IfExists.class).isEmpty();
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
    * Performs validation of the observer method for compliance with the
    * specifications.
    */
   private void checkObserverMethod()
   {
      // Make sure exactly one and only one parameter is annotated with Observes
      List<AnnotatedParameter<?>> eventObjects = this.observerMethod.getAnnotatedParameters(Observes.class);
      if (eventObjects.size() > 1)
      {
         throw new DefinitionException(this + " is invalid because it contains more than event parameter annotated @Observes");
      }
      // Make sure the event object above is not parameterized with a type
      // variable or wildcard
      if (eventObjects.size() > 0)
      {
         AnnotatedParameter<?> eventParam = eventObjects.iterator().next();
         if (Reflections.isParameterizedType(eventParam.getType()))
         {
            throw new DefinitionException(this + " cannot observe parameterized event types");
         }
      }
      // Check for parameters annotated with @Disposes
      List<AnnotatedParameter<?>> disposeParams = this.observerMethod.getAnnotatedParameters(Disposes.class);
      if (disposeParams.size() > 0)
      {
         throw new DefinitionException(this + " cannot have any parameters annotated with @Dispose");
      }
      // Check annotations on the method to make sure this is not a producer
      // method, initializer method, or destructor method.
      if (this.observerMethod.isAnnotationPresent(Produces.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Produces");
      }
      if (this.observerMethod.isAnnotationPresent(Initializer.class))
      {
         throw new DefinitionException(this + " cannot be annotated with @Initializer");
      }
   }

   public void notify(final T event)
   {
      Object instance = null;
      Object dependentsCollector = new Object();
      try
      {
         if (Dependent.class.equals(observerBean.getScopeType()) && observerBean instanceof AbstractBean)
         {
            DependentContext.INSTANCE.setCurrentInjectionInstance(dependentsCollector);
         }
         // Get the most specialized instance of the component
         instance = manager.getInstance(observerBean, !isConditional());
         if (instance == null)
         {
            return;
         }
         if (isTransactional() && isTransactionActive())
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
      finally
      {
         if (Dependent.class.equals(observerBean.getScopeType()))
         {
            ((AbstractBean<?, ?>) observerBean).getDependentInstancesStore().destroyDependentInstances(dependentsCollector);
            DependentContext.INSTANCE.clearCurrentInjectionInstance(instance);
         }
      }
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
         return userTransaction != null && userTransaction.getStatus() == Status.STATUS_ACTIVE;
      }
      catch (SystemException e)
      {
         return false;
      }
   }

   /**
    * Defers an event for processing in a later phase of the current
    * transaction.
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

   public Class<T> getEventType()
   {
      return eventType;
   }

   public Annotation[] getBindingsAsArray()
   {
      return bindings;
   }

}
