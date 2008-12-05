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
import javax.webbeans.Dependent;
import javax.webbeans.DuplicateBindingTypeException;
import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.Observer;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;

/**
 * Implementation of the Event interface
 * 
 * @author David Allen
 * 
 * @param <T>
 * @see javax.webbeans.Event
 */
@Standard
@Dependent
public class EventImpl<T> implements Event<T>
{
   // The set of binding types
   private Set<? extends Annotation> bindingTypes;
   // The event type
   private Class<T> eventType;
   // The Web Beans manager
   protected static final ManagerImpl manager = ManagerImpl.instance();

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    * @param bindingTypes The binding types
    */
   public EventImpl(Annotation... bindingTypes)
   {
      this.bindingTypes = checkBindingTypes(bindingTypes);
   }

   /**
    * Validates the binding types
    * 
    * Removes @Observable from the list
    * 
    * @param annotations The annotations to validate
    * @return A set of unique binding type annotations (minus @Observable, if it
    *         was present)
    */
   private Set<Annotation> checkBindingTypes(Annotation... annotations)
   {
      Set<Annotation> uniqueAnnotations = new HashSet<Annotation>();
      for (Annotation annotation : annotations)
      {
         if (!annotation.annotationType().isAnnotationPresent(BindingType.class))
         {
            throw new IllegalArgumentException(annotation + " is not a binding type");
         }
         if (uniqueAnnotations.contains(annotation))
         {
            throw new DuplicateBindingTypeException(annotation + " is already present in the bindings list");
         }
         if (!annotation.annotationType().equals(Observable.class))
         {
            uniqueAnnotations.add(annotation);
         }
      }
      return uniqueAnnotations;
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