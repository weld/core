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
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.Observer;

import org.jboss.webbeans.ManagerImpl;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T>
 * @see javax.webbeans.Event
 */
public class EventImpl<T> implements Event<T>
{
   // The set of binding types
   private final Set<? extends Annotation> bindingTypes;
   // The event type
   private final Class<T> eventType;
   private final ManagerImpl manager;

   /**
    * Constructor
    * 
    * @param bindingTypes The binding types
    */
   public EventImpl(ManagerImpl manager, Class<T> eventType, Annotation... bindingTypes)
   {
      this.manager = manager;
      this.bindingTypes = getBindingTypes(bindingTypes);
      this.eventType = eventType;
   }

   /**
    * Validates the binding types
    * 
    * Removes @Observable from the list
    * 
    * @param annotations The annotations to validate
    * @return A set of binding type annotations (minus @Observable, if it was
    *         present)
    */
   private static Set<Annotation> getBindingTypes(Annotation... annotations)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      for (Annotation annotation : annotations)
      {
         if (!annotation.annotationType().isAnnotationPresent(BindingType.class))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type");
         }
         if (!annotation.annotationType().equals(Observable.class))
         {
            result.add(annotation);
         }
      }
      return result;
   }

   /**
    * Validates the binding types and checks for dupes
    * 
    * @param annotations The annotations to validate
    * @return A set of unique binding type annotations
    */
   private Set<Annotation> checkBindingTypes(Annotation... annotations)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      for (Annotation annotation : annotations)
      {
         if (!annotation.annotationType().isAnnotationPresent(BindingType.class))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type");
         }
         for (Annotation bindingAnnotation : this.bindingTypes)
         {
            if (bindingAnnotation.annotationType().equals(annotation.annotationType()))
            {
               throw new DuplicateBindingTypeException(annotation + " is already present in the bindings list");
            }
         }
         result.add(annotation);
      }
      return result;
   }

   /**
    * Fires an event
    * 
    * @param event The event object
    * @param bindingTypes Additional binding types
    */
   public void fire(T event, Annotation... bindingTypes)
   {
      Set<Annotation> bindingParameters = checkBindingTypes(bindingTypes);
      bindingParameters.addAll(this.bindingTypes);
      manager.fireEvent(event, bindingParameters.toArray(new Annotation[0]));
   }

   /**
    * Registers an observer
    * 
    * @param observer
    * @param bindingTypes Additional binding types
    */
   public void observe(Observer<T> observer, Annotation... bindingTypes)
   {
      Set<Annotation> bindingParameters = checkBindingTypes(bindingTypes);
      bindingParameters.addAll(this.bindingTypes);
      manager.addObserver(observer, eventType, bindingParameters.toArray(new Annotation[0]));
   }

}
