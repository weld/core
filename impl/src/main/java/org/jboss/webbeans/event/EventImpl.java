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
import java.util.Set;

import javax.event.Event;
import javax.event.Observer;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.FacadeImpl;
import org.jboss.webbeans.literal.AnyLiteral;
import org.jboss.webbeans.util.Strings;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T> The type of event being wrapped
 * @see javax.event.Event
 */
public class EventImpl<T> extends FacadeImpl<T> implements Event<T>
{
   
   private static final long serialVersionUID = 656782657242515455L;

   public static <E> Event<E> of(Type eventType, BeanManagerImpl manager, Set<Annotation> bindings)
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
   public EventImpl(Type eventType, BeanManagerImpl manager, Set<Annotation> bindings)
   {
      super(eventType, manager, removeBindings(bindings, new AnyLiteral()));
   }

   /**
    * Fires an event
    * 
    * @param event The event object
    * @param bindings Additional binding types
    */
   public void fire(T event, Annotation... bindings)
   {
      getManager().fireEvent(event, mergeInBindings(bindings));
   }

   /**
    * Registers an observer
    * 
    * @param observer
    * @param bindings Additional binding types
    */
   public void observe(Observer<T> observer, Annotation... bindings)
   {
      getManager().addObserver(observer, mergeInBindings(bindings));
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Observable Event:\n");
      buffer.append("  Event Type: " + getType().toString() + "\n");
      buffer.append(Strings.collectionToString("  Event Bindings: ", getBindings()));
      return buffer.toString();
   }

}
