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
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Resource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.webbeans.Observer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.transaction.TransactionListener;

/**
 * The event bus is where observers are registered and events are fired.
 * 
 * @author David Allen
 * 
 */
public class EventManager
{
   private final Map<Class<?>, CopyOnWriteArrayList<EventObserver<?>>> registeredObservers;
   private ManagerImpl manager;
   // TODO: can we do this?
   @Resource
   TransactionManager transactionManager;

   /**
    * Initializes a new instance of the EventManager. This includes looking up
    * the transaction manager which is needed to defer events till the end of a
    * transaction.
    */
   public EventManager(ManagerImpl manager)
   {
      registeredObservers = new ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<EventObserver<?>>>();
      this.manager = manager;
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.
    * 
    * @param observer The observer that should receive events
    */
   public <T> void addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      CopyOnWriteArrayList<EventObserver<?>> eventTypeObservers = registeredObservers.get(eventType);
      if (eventTypeObservers == null)
      {
         eventTypeObservers = new CopyOnWriteArrayList<EventObserver<?>>();
         registeredObservers.put(eventType, eventTypeObservers);
      }
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, bindings);
      if (!eventTypeObservers.contains(eventObserver))
      {
         eventTypeObservers.add(eventObserver);
      }
   }

   /**
    * Resolves the list of observers to be notified for a given event and
    * optional event bindings.
    * 
    * @param event The event object
    * @param bindings Optional event bindings
    * @return A set of Observers
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

   private boolean isTransactionActive()
   {
      try
      {
         // TODO: Check NPE conditions;
         return transactionManager.getTransaction().getStatus() == Status.STATUS_ACTIVE;
      }
      catch (SystemException e)
      {
         return false;
      }
   }

   /**
    * Notifies each observer immediately of the event unless a transaction is
    * currently in progress, in which case a deferred event is created and
    * registered.
    * 
    * @param <T>
    * @param observers
    * @param event
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

   private <T> void deferEvent(T event, Observer<T> observer)
   {
      TransactionListener transactionListener = manager.getInstanceByType(TransactionListener.class);
      DeferredEventNotification<T> deferredEvent = new DeferredEventNotification<T>(event, observer);
      transactionListener.registerSynhronization(deferredEvent);
   }

   /**
    * Removes an observer from the event bus.
    * 
    * @param observer The observer to remove
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
      StringBuffer buffer = new StringBuffer();
      buffer.append("Event manager\n");
      buffer.append("Registered observers: " + registeredObservers.size() + "\n");
      int i = 1;
      for (Entry<Class<?>, CopyOnWriteArrayList<EventObserver<?>>> entry : registeredObservers.entrySet())
      {
         for (EventObserver<?> observer : entry.getValue())
         {
            buffer.append(i + " - " + entry.getKey().getName() + ": " + observer.toString() + "\n");
         }
         i++;
      }
      buffer.append("Transaction manager: " + transactionManager + "\n");
      return buffer.toString();
   }
}
