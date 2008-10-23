package org.jboss.webbeans.event;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
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
   private final Map<Integer, Set<Observer<?>>>  registeredObservers;
   private final TransactionManager           tm;
   private String                             tmName = "java:/TransactionManager";
   
   /**
    * Initializes a new instance of the EventBus.  This includes looking up the
    * transaction manager which is needed to defer events till the end of a
    * transaction.
    * TODO Should be able to configure JNDI name of transaction manager
    * 
    */
   public EventBus()
   {
      registeredObservers = new HashMap<Integer, Set<Observer<?>>>();
      tm = JNDI.lookup(tmName, TransactionManager.class);
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.  The
    * event information is already encapsulated as part of the observer.
    * @param o The observer that should receive events
    */
   public void addObserver(Observer<?> o)
   {
      int key = 1 /*TODO generateKey(o.getEventType(), o.get)*/;
      Set<Observer<?>> l = registeredObservers.get(key);
      if (l == null)
         l = new HashSet<Observer<?>>();
      l.add(o);
      registeredObservers.put(key, l);
   }

   /**
    * Defers delivery of an event till the end of the currently active transaction.
    * 
    * @param container The WebBeans container
    * @param event The event object to deliver
    * @param o The observer to receive the event
    * @throws SystemException
    * @throws IllegalStateException
    * @throws RollbackException
    */
   public void deferEvent(Object event, Observer<Object> o) throws SystemException, IllegalStateException, RollbackException
   {
      if (tm != null) {
         // Get the current transaction associated with the thread
         Transaction t = tm.getTransaction();
         if (t != null)
            t.registerSynchronization(new DeferredEventNotification<Object>(event, o));
      }
   }

   /**
    * Resolves the list of observers to be notified for a given event and
    * optional event bindings.
    * @param event The event object
    * @param bindings Optional event bindings
    * @return A set of Observers
    */
   public <T> Set<Observer<T>> getObservers(T event, Annotation... bindings)
   {
      Set<Observer<T>> results = new HashSet<Observer<T>>();
      for (Observer<?> observer : registeredObservers.get(generateKey(event.getClass(), Arrays.asList(bindings))))
      {
         results.add((Observer<T>) observer);
      }
      return results;
   }

   /**
    * Removes an observer from the event bus.
    * @param o The observer to remove
    */
   public void removeObserver(Observer<?> o)
   {
      // TODO fix
    //  Set<Observer<?>> l = registeredObservers.get(generateKey(o.getEventType(), o.getEventBindingTypes()));
      //if (l != null) {
        // l.remove(o);
      //}
   }
   
   /**
    * Creates a key that can be used in a map for an observer that is notified of
    * events of a specific type with optional event bindings.
    * @param eventType The class of the event being observed
    * @param eventBindings An optional set of event bindings
    * @return
    */
   public int generateKey(Class<?> eventType, Collection<Annotation> eventBindings)
   {
      // Produce the sum of the hash codes for the event type and the set of
      // event bindings.
      int key = eventType.hashCode();
      if (eventBindings != null)
         key += eventBindings.hashCode();
      return key;
   }
}
