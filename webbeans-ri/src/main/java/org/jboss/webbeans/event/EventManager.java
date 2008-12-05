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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.webbeans.Observer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.transaction.TransactionListener;
import org.jboss.webbeans.util.JNDI;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

/**
 * The event bus is where observers are registered and events are fired.
 * 
 * @author David Allen
 * 
 */
public class EventManager
{
   /**
    * The known transactional phases a transactional event observer can be
    * interested in
    */ 
   protected enum TransactionObservationPhase
   {
      NONE, BEFORE_COMPLETION, AFTER_COMPLETION, AFTER_FAILURE, AFTER_SUCCESS
   }   
   
   /**
    * An event type -> observer list map
    */
   private class RegisteredObserversMap extends ForwardingMap<Class<?>, List<EventObserver<?>>>
   {

      // The map delegate
      private Map<Class<?>, List<EventObserver<?>>> delegate;

      /**
       * Constructor. Initializes the delegate
       */
      public RegisteredObserversMap()
      {
         delegate = new ConcurrentHashMap<Class<?>, List<EventObserver<?>>>();
      }

      /**
       * Returns the delegate for the ForwardingMap
       * 
       * @return The delegate
       */
      @Override
      protected Map<Class<?>, List<EventObserver<?>>> delegate()
      {
         return delegate;
      }

      /**
       * Gets the list of observers for a given event type
       * 
       * @param eventType The event type
       * @return The list of interested observers. An empty list is returned if
       *         there are no matches.
       */
      @Override
      public CopyOnWriteArrayList<EventObserver<?>> get(Object eventType)
      {
         CopyOnWriteArrayList<EventObserver<?>> observers = (CopyOnWriteArrayList<EventObserver<?>>) super.get(eventType);
         return observers != null ? observers : new CopyOnWriteArrayList<EventObserver<?>>();
      }

      /**
       * Adds an observer for a given event type
       * 
       * Implicitly creates a new list if there is none for the event type. Only adds the observer if
       * it is not already present
       * 
       * @param eventType The event type
       * @param observer The observer to add
       */
      public void put(Class<?> eventType, EventObserver<?> observer)
      {
         List<EventObserver<?>> observers = super.get(eventType);
         if (observers == null)
         {
            observers = new CopyOnWriteArrayList<EventObserver<?>>();
            super.put(eventType, observers);
         }
         if (!observers.contains(observer))
         {
            observers.add(observer);
         }
      }

      /**
       * Gets a string representation of the map
       * 
       * @return A string representation
       */
      @Override
      public String toString()
      {
         return Strings.mapToString("RegisteredObserversMap (event type -> observers list): ", delegate);
      }

   }

   // The map of registered observers for a give
   private final RegisteredObserversMap registeredObservers;
   // The current UserTransaction
   UserTransaction userTransaction;

   /**
    * Initializes a new instance of the EventManager.
    */
   public EventManager()
   {
      registeredObservers = new RegisteredObserversMap();
      // TODO. Check where to *really* get this from
      userTransaction = (UserTransaction) JNDI.lookup("java:/UserTransaction");
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.
    * 
    * @param observer The observer that should receive events
    * @param eventType The event type the observer is interested in
    * @param bindings The bindings the observer wants to filter on
    */
   public <T> void addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, bindings);
      registeredObservers.put(eventType, eventObserver);
   }

   /**
    * Resolves the list of observers to be notified for a given event and
    * optional event bindings.
    * 
    * @param event The event object
    * @param bindings Optional event bindings
    * @return A set of Observers. An empty set is returned if there are no
    *         matches.
    */
   @SuppressWarnings("unchecked")
   public <T> Set<Observer<T>> getObservers(T event, Annotation... bindings)
   {
      Set<Observer<T>> interestedObservers = new HashSet<Observer<T>>();
      for (EventObserver<?> observer : registeredObservers.get(event.getClass()))
      {
         if (observer.isObserverInterested(bindings))
         {
            interestedObservers.add((Observer<T>) observer.getObserver());
         }
      }
      return interestedObservers;
   }

   /**
    * Checks if there is currently a transaction active
    * 
    * @return True if there is one, false otherwise
    */
   private boolean isTransactionActive()
   {
      try
      {
         return userTransaction.getStatus() == Status.STATUS_ACTIVE;
      }
      catch (SystemException e)
      {
         return false;
      }
   }

   /**
    * Iterates over the interested observers. If the observers is transactional
    * and there is a transaction currently in progress, the event is deferred.
    * In other cases, the observer is notified immediately.
    * 
    * @param observers The interested observers
    * @param event The event type
    */
   public <T> void notifyObservers(Set<Observer<T>> observers, T event)
   {
      for (Observer<T> observer : observers)
      {
         if (isTransactionActive() && ((ObserverImpl<?>) observer).isTransactional())
         {
            deferEvent(event, observer);
         }
         else
         {
            observer.notify(event);
         }
      }
   }

   /**
    * Defers an event with regard to current transaction phase
    * 
    * Gets the transaction listener, creates a deferred event representation and
    * registers the deferred event.
    * 
    * @param event The event type
    * @param observer The interested observer
    */
   private <T> void deferEvent(T event, Observer<T> observer)
   {
      TransactionListener transactionListener = ManagerImpl.instance().getInstanceByType(TransactionListener.class);
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, observer);
      transactionListener.registerSynhronization(deferredEvent);
   }

   /**
    * Removes an observer from the event bus.
    * 
    * @param observer The observer to remove
    * @param eventType The event type of the observer to remove
    * @param bindings The bindings of the observer to remove
    */
   public <T> void removeObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      List<EventObserver<?>> observers = registeredObservers.get(eventType);
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, bindings);
      observers.remove(eventObserver);
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Event manager\n");
      buffer.append(registeredObservers.toString());
      return buffer.toString();
   }
}
