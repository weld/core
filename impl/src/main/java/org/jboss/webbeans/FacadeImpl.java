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

package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.inject.DuplicateBindingTypeException;
import javax.inject.manager.Manager;

import org.jboss.webbeans.util.Reflections;

/**
 * Common implementation for binding-type-based helpers
 * 
 * @author Gavin King
 * 
 * @param <T>
 */
public abstract class FacadeImpl<T>
{
   // The binding types the helper operates on
   protected final Set<? extends Annotation> bindings;
   // The Web Beans manager
   protected final Manager manager;
   // The type of the operation
   protected final Class<T> type;

   /**
    * Constructor
    * 
    * @param type The event type
    * @param manager The Web Beans manager
    * @param bindings The binding types
    */
   protected FacadeImpl(Class<T> type, Manager manager, Annotation... bindings)
   {
      this.manager = manager;
      this.type = type;
      this.bindings = mergeBindings(new HashSet<Annotation>(), bindings);
   }

   /**
    * Merges and validates the current and new bindings
    * 
    * Checks with an abstract method for annotations to exclude
    * 
    * @param currentBindings Existing bindings
    * @param newBindings New bindings
    * @return The union of the bindings
    */
   protected Set<Annotation> mergeBindings(Set<? extends Annotation> currentBindings, Annotation... newBindings)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      result.addAll(currentBindings);
      for (Annotation newAnnotation : newBindings)
      {
         if (!Reflections.isBindings(newAnnotation))
         {
            throw new IllegalArgumentException(newAnnotation + " is not a binding for " + this);
         }
         if (result.contains(newAnnotation))
         {
            throw new DuplicateBindingTypeException(newAnnotation + " is already present in the bindings list for " + this);
         }
         if (!getFilteredAnnotations().contains(newAnnotation.annotationType()))
         {
            result.add(newAnnotation);
         }
      }
      return result;
   }

   /**
    * Gets a set of annotation classes to ignore
    * 
    * @return A set of annotation classes to ignore
    */
   protected abstract Set<Class<? extends Annotation>> getFilteredAnnotations();

   /**
    * Merges the binding this helper operates upon with the parameters
    * 
    * @param bindings The bindings to merge
    * 
    * @return The union of the binding types
    */
   protected Annotation[] mergeBindings(Annotation... newBindings)
   {
      return mergeBindings(bindings, newBindings).toArray(new Annotation[0]);
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      return "Abstract facade implmentation";
   }

}