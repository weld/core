package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.webbeans.Current;
import javax.webbeans.Observer;

/**
 * The event bus is where observers are registered and events are fired.
 * 
 * @author David Allen
 * 
 */
public class EventBus
{
   private final Map<Class<?>, ArrayList<EventObserver<?>>> registeredObservers;
   
   @Current
   private TransactionManager transactionManager;

   /**
    * Initializes a new instance of the EventBus. This includes looking up the
    * transaction manager which is needed to defer events till the end of a
    * transaction. 
    */
   public EventBus()
   {
      registeredObservers = new HashMap<Class<?>, ArrayList<EventObserver<?>>>();
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.
    * 
    * @param observer
    *           The observer that should receive events
    */
   public <T> void addObserver(Observer<T> observer, Class<T> eventType, Annotation... bindings)
   {
      ArrayList<EventObserver<?>> l = registeredObservers.get(eventType);
      if (l == null)
      {
         l = new ArrayList<EventObserver<?>>();
         registeredObservers.put(eventType, l);
      }
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, bindings);
      if (!l.contains(eventObserver))
         l.add(eventObserver);
   }

   /**
    * Defers delivery of an event till the end of the currently active
    * transaction.
    * 
    * @param container
    *           The WebBeans container
    * @param event
    *           The event object to deliver
    * @param observer
    *           The observer to receive the event
    * @throws SystemException
    * @throws IllegalStateException
    * @throws RollbackException
    */
   public <T> void deferEvent(T event, Observer<T> observer)
         throws SystemException, IllegalStateException, RollbackException
   {
      if (transactionManager != null)
      {
         // Get the current transaction associated with the thread
         Transaction t = transactionManager.getTransaction();
         if (t != null)
            t.registerSynchronization(new DeferredEventNotification<T>(
                  event, observer));
      }
   }

   /**
    * Resolves the list of observers to be notified for a given event and
    * optional event bindings.
    * 
    * @param event
    *           The event object
    * @param bindings
    *           Optional event bindings
    * @return A set of Observers
    */
   @SuppressWarnings("unchecked")
   public <T> Set<Observer<T>> getObservers(T event, Annotation... bindings)
   {
      Set<Observer<T>> results = new HashSet<Observer<T>>();
      for (EventObserver<?> observer : registeredObservers.get(event.getClass()))
      {
         if (observer.isObserverInterested(bindings))
            results.add((Observer<T>) observer.getObserver());
      }
      return results;
   }

   /**
    * Removes an observer from the event bus.
    * 
    * @param observer
    *           The observer to remove
    */
   public <T> void removeObserver(Observer<T> observer, Class<T> eventType)
   {
      ArrayList<EventObserver<?>> observers = registeredObservers.get(eventType);
      for (int i = 0; i < observers.size(); i++)
      {
         EventObserver<?> eventObserver = observers.get(i);
         if (eventObserver.getObserver().equals(observer))
         {
            observers.remove(i);
            break;
         }
      }
   }

   /**
    * TODO Remove this once injection of the transaction manager works.
    * @param transactionManager the TransactionManager to set
    */
   public final void setTransactionManager(TransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }
}
