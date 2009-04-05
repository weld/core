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
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.event.Observer;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections.HierarchyDiscovery;

/**
 * The event bus is where observers are registered and events are fired.
 * 
 * @author David Allen
 * 
 */
public class EventManager
{
   private static Log log = Logging.getLog(EventManager.class);
   
   private final ManagerImpl manager;


   /**
    * Initializes a new instance of the EventManager.
    */
   public EventManager(ManagerImpl manager)
   {
      this.manager = manager;
   }

   /**
    * Adds an observer to the event bus so that it receives event notifications.
    * 
    * @param observer The observer that should receive events
    * @param eventType The event type the observer is interested in
    * @param bindings The bindings the observer wants to filter on
    */
   public <T> void addObserver(Observer<T> observer, Type eventType, Annotation... bindings)
   {
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, manager, bindings);
      manager.getRegisteredObservers().put(eventType, eventObserver);
      log.debug("Added observer " + observer + " observing event type " + eventType);
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
   public <T> Set<Observer<T>> getObservers(T event, Annotation... bindings)
   {
      Set<Observer<T>> interestedObservers = new HashSet<Observer<T>>();
      Set<Type> types = new HierarchyDiscovery(event.getClass()).getFlattenedTypes();
      for (Type type : types)
      {
         for (EventObserver<?> observer : manager.getRegisteredObservers().get(type))
         {
            log.debug("Checking observer " + observer + " to see if it is interested in event [" + event + "]");
            if (observer.isObserverInterested(bindings))
            {
               @SuppressWarnings("unchecked")
               Observer<T> o = (Observer<T>) observer.getObserver();
               interestedObservers.add(o);
               log.debug("Added observer " + observer + " for event [" + event + "]");
            }
         }
      }
      return interestedObservers;
   }

   /**
    * Iterates over the interested observers. If an observer is transactional
    * and there is a transaction currently in progress, the event is deferred.
    * In other cases, the observer is notified immediately.
    * 
    * @param observers The interested observers
    * @param event The event type
    */
   public <T> void notifyObservers(Set<Observer<T>> observers, T event)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         for (Observer<T> observer : observers)
         {
            observer.notify(event);
         }
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
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
      Collection<EventObserver<?>> observers = manager.getRegisteredObservers().get(eventType);
      EventObserver<T> eventObserver = new EventObserver<T>(observer, eventType, manager, bindings);
      observers.remove(eventObserver);
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Event manager\n");
      buffer.append(manager.getRegisteredObservers().toString());
      return buffer.toString();
   }

}
