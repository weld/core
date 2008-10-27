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
import javax.webbeans.Observer;

import org.jboss.webbeans.util.JNDI;

/**
 * The event bus is where observers are registered and events are fired.
 * 
 * @author David Allen
 * 
 */
public class EventBus
{
   private final Map<Class<?>, ArrayList<EventObserver<?>>> registeredObservers;
   private final TransactionManager tm;
   private String tmName = "java:/TransactionManager";

   /**
    * Initializes a new instance of the EventBus. This includes looking up the
    * transaction manager which is needed to defer events till the end of a
    * transaction. TODO Should be able to configure JNDI name of transaction
    * manager
    * 
    */
   public EventBus()
   {
      registeredObservers = new HashMap<Class<?>, ArrayList<EventObserver<?>>>();
      tm = JNDI.lookup(tmName, TransactionManager.class);
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.
    * The event information is already encapsulated as part of the observer.
    * 
    * @param o
    *           The observer that should receive events
    */
   public <T> void addObserver(Observer<T> o, Class<T> eventType,
         Annotation... bindings)
   {
      ArrayList<EventObserver<?>> l = registeredObservers.get(eventType);
      if (l == null)
      {
         l = new ArrayList<EventObserver<?>>();
         registeredObservers.put(eventType, l);
      }
      EventObserver<T> eventObserver = new EventObserver<T>(o, eventType, bindings);
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
    * @param o
    *           The observer to receive the event
    * @throws SystemException
    * @throws IllegalStateException
    * @throws RollbackException
    */
   public void deferEvent(Object event, Observer<Object> o)
         throws SystemException, IllegalStateException, RollbackException
   {
      if (tm != null)
      {
         // Get the current transaction associated with the thread
         Transaction t = tm.getTransaction();
         if (t != null)
            t.registerSynchronization(new DeferredEventNotification<Object>(
                  event, o));
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
         // TODO Verify bindings match before adding
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
}
