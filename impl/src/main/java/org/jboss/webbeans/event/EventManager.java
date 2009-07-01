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
import java.util.Set;

import javax.enterprise.event.Observer;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;

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
      manager.getRegisteredObservers().add(eventObserver);
      log.debug("Added observer " + observer + " observing event type " + eventType);
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
            observer.notify(event);
         }
      }
      finally
      {
         // TODO This breaks SE shutdown, also we need to tidy up how dependent context is activated....
         //DependentContext.instance().setActive(false);
      }
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
