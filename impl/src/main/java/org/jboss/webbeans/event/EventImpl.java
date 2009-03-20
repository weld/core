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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.event.Event;
import javax.event.Fires;
import javax.event.Observer;
import javax.inject.manager.Manager;

import org.jboss.webbeans.FacadeImpl;
import org.jboss.webbeans.util.Strings;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T> The type of event being wrapped
 * @see javax.event.Event
 */
public class EventImpl<T> extends FacadeImpl<T> implements Event<T>, Serializable
{
   
   private static final long serialVersionUID = 8130060821283091287L;
   
   @SuppressWarnings("unchecked")
   private static final Set<Class<? extends Annotation>> FILTERED_ANNOTATIONS = new HashSet<Class<? extends Annotation>>(Arrays.asList(Fires.class));

   public static <E> Event<E> of(Class<E> eventType, Manager manager, Set<Annotation> bindings)
   {
      return new EventImpl<E>(eventType, manager, bindings);
   }
   
   
   /**
    * Constructor
    * 
    * @param eventType The event type
    * @param manager The Web Beans manager
    * @param bindings The binding types
    */
   public EventImpl(Class<T> eventType, Manager manager, Set<Annotation> bindings)
   {
      super(eventType, manager, bindings);
   }

   /**
    * Fires an event
    * 
    * @param event The event object
    * @param bindings Additional binding types
    */
   public void fire(T event, Annotation... bindings)
   {
      manager.fireEvent(event, mergeInBindings(bindings));
   }

   /**
    * Registers an observer
    * 
    * @param observer
    * @param bindings Additional binding types
    */
   public void observe(Observer<T> observer, Annotation... bindings)
   {
      manager.addObserver(observer, type, mergeInBindings(bindings));
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Observable Event:\n");
      buffer.append("  Event Type: " + type.getName() + "\n");
      buffer.append(Strings.collectionToString("  Event Bindings: ", bindings));
      return buffer.toString();
   }

}
