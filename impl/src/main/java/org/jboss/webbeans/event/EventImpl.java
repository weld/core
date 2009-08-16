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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.TypeLiteral;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.builtin.AbstractFacade;
import org.jboss.webbeans.util.Strings;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T> The type of event being wrapped
 * @see javax.enterprise.event.Event
 */
public class EventImpl<T> extends AbstractFacade<T, Event<T>> implements Event<T>
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
   private EventImpl(Type eventType, BeanManagerImpl manager, Set<Annotation> bindings)
   {
      super(eventType, manager, bindings);
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

   public void fire(T event)
   {
      getManager().fireEvent(event, mergeInBindings());
   }
   
   public Event<T> select(Annotation... bindings)
   {
      return selectEvent(this.getType(), bindings);
   }

   public <U extends T> Event<U> select(Class<U> subtype, Annotation... bindings)
   {
      return selectEvent(subtype, bindings);
   }

   public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... bindings)
   {
      return selectEvent(subtype.getType(), bindings);
   }
   
   private <U extends T> Event<U> selectEvent(Type subtype, Annotation[] bindings)
   {
      return new EventImpl<U>(
            subtype, 
            this.getManager(), 
            new HashSet<Annotation>(Arrays.asList(mergeInBindings(bindings))));
   } 

}
