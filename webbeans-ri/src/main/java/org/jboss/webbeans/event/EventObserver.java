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
import java.util.ArrayList;
import java.util.List;

import javax.event.Observer;
import javax.inject.Current;
import javax.inject.DuplicateBindingTypeException;

import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

/**
 * <p>
 * EventObserver wraps various {@link Observer} objects created by application
 * code or by the Web Beans Manager for annotated observer methods. In all
 * cases, this wrapper provides consistent object identification and hashing
 * based on the type of event being observed and any event binding types
 * specified. It also provides a query method to quickly determine if a set of
 * event bindings are exactly what the observer is interested in receiving.
 * </p>
 * 
 * @author David Allen
 * 
 */
public class EventObserver<T>
{

   private final Class<T> eventType;
   private final List<Annotation> eventBindings;
   private final Observer<T> observer;

   /**
    * Constructs a new wrapper for an observer.
    * 
    * @param observer The observer
    * @param eventType The class of event being observed
    * @param eventBindings The array of annotation event bindings, if any
    */
   public EventObserver(final Observer<T> observer, final Class<T> eventType, final Annotation... eventBindings)
   {
      this.observer = observer;
      this.eventType = eventType;
      this.eventBindings = new ArrayList<Annotation>();
      checkEventBindings(eventBindings);
   }

   /**
    * Checks that each event binding specified on the observer is indeed a
    * binding type (annotated with @BindingType) and that there are no duplicate
    * bindings specified.  If the @Current binding type is found, it is removed
    * since this is only a default supplied by the container but no applicable
    * for the actual event objects which get fired.
    */
   private void checkEventBindings(Annotation[] bindingAnnotations)
   {
      for (Annotation annotation : bindingAnnotations)
      {
         if (!Reflections.isBindingType(annotation))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type for " + this);
         }
         if (eventBindings.contains(annotation))
         {
            throw new DuplicateBindingTypeException(annotation + " is already present in the bindings list for " + this);
         }
         if (!annotation.annotationType().equals(Current.class))
         {
            eventBindings.add(annotation);
         }
      }
   }

   /**
    * @return the eventType
    */
   public final Class<T> getEventType()
   {
      return eventType;
   }

   /**
    * @return the eventBindings
    */
   public final List<Annotation> getEventBindings()
   {
      return eventBindings;
   }

   /**
    * @return the observer
    */
   public final Observer<? extends T> getObserver()
   {
      return observer;
   }

   /**
    * Query method to determine if this observer should be notified of an event
    * based on the event bindings and any member values thereof.
    * 
    * @param bindings The event bindings
    * @return true only if all required bindings match
    */
   public boolean isObserverInterested(Annotation... bindings)
   {
      // Simply check that all event bindings specified by the observer are
      // in the list provided.
      if (this.eventBindings.isEmpty())
      {
         return true;
      }
      else
      {
         // List<Annotation> bindingsArray = Arrays.asList(bindings);
         // return bindingsArray.containsAll(this.eventBindings);
         for (Annotation x : eventBindings)
         {
            boolean found = false;
            for (Annotation y : bindings)
            {
               if (MetaDataCache.instance().getBindingTypeModel(x.annotationType()).isEqual(x, y))
               {
                  found = true;
               }
            }
            if (!found)
            {
               return false;
            }
         }
         return true;
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((eventBindings == null) ? 0 : eventBindings.hashCode());
      result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
      result = prime * result + ((observer == null) ? 0 : observer.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other == null || (!(other instanceof EventObserver)))
      {
         return false;
      }
      EventObserver<?> otherObserver = (EventObserver<?>) other;
      if (!eventType.equals(otherObserver.getEventType()))
      {
         return false;
      }
      if (!eventBindings.equals(otherObserver.getEventBindings()))
      {
         return false;
      }
      if (!observer.equals(otherObserver.getObserver()))
      {
         return false;
      }
      return true;
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Event Observer:\n");
      buffer.append("  Event Type: " + eventType.getName() + "\n");
      buffer.append(Strings.collectionToString("  Event Bindings: ", eventBindings));
      buffer.append("  Observer: " + observer);
      return buffer.toString();
   }

}
