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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observer;

import org.jboss.webbeans.BeanManagerImpl;
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
   
   private final BeanManagerImpl manager;


   /**
    * Initializes a new instance of the EventManager.
    */
   public EventManager(BeanManagerImpl manager)
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
//      checkEventType(event.getClass());
      Set<Observer<T>> interestedObservers = new HashSet<Observer<T>>();
      Set<Type> types = new HierarchyDiscovery(event.getClass()).getFlattenedTypes();
      for (Type type : types)
      {
         for (EventObserver<?> observer : manager.getRegisteredObservers().get(type))
         {
            log.trace("Checking observer " + observer + " to see if it is interested in event [" + event + "]");
            if (observer.isObserverInterested(bindings))
            {
               @SuppressWarnings("unchecked")
               Observer<T> o = (Observer<T>) observer.getObserver();
               interestedObservers.add(o);
               log.trace("Added observer " + observer + " for event [" + event + "]");
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
         DependentContext.instance().setActive(true);
         for (Observer<T> observer : observers)
         {
            if (observer.notify(event))
            {
               // We can remove the once-only observer
               removeObserver(observer);
            }
         }
      }
      finally
      {
         // TODO This breaks SE shutdown, also we need to tidy up how dependent context is activated....
         //DependentContext.instance().setActive(false);
      }
   }

   /**
    * Removes an observer from the event bus.
    * 
    * @param observer The observer to remove
    * @param eventType The event type of the observer to remove
    */
   public void removeObserver(Observer<?> observer) 
   {
      Collection<EventObserver<?>> observers = manager.getRegisteredObservers().get(getTypeOfObserver(observer));
      for (EventObserver<?> eventObserver : observers)
         if (eventObserver.getObserver() == observer)
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

   public Type getTypeOfObserver(Observer<?> observer)
   {
      for (Type type : observer.getClass().getGenericInterfaces())
      {
         if (type instanceof ParameterizedType)
         {
            ParameterizedType ptype = (ParameterizedType) type;
            if (Observer.class.isAssignableFrom((Class<?>) ptype.getRawType()))
            {
               return ptype.getActualTypeArguments()[0];
            }
         }
      }
      throw new RuntimeException("Cannot find observer's event type: " + observer);
   }
}
