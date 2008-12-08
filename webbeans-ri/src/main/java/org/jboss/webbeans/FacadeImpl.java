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

import javax.webbeans.DuplicateBindingTypeException;

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
   protected final Set<? extends Annotation> bindingTypes;
   // The Web Beans manager
   protected final ManagerImpl manager;
   // The type of the operation
   protected final Class<T> type;

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    * @param type The event type
    * @param bindingTypes The binding types
    */
   protected FacadeImpl(ManagerImpl manager, Class<T> type, Annotation... bindingTypes)
   {
      this.manager = manager;
      this.type = type;
      this.bindingTypes = mergeBindingTypes(new HashSet<Annotation>(), bindingTypes);
   }

   /**
    * Merges and validates the current and new bindings
    * 
    * Checkes with an abstract method for annotations to exclude
    * 
    * @param currentBindings Existing bindings
    * @param newBindings New bindings
    * @return The union of the bindings
    */
   protected Set<Annotation> mergeBindingTypes(Set<? extends Annotation> currentBindings, Annotation... newBindings)
   {
      Set<Annotation> result = new HashSet<Annotation>();
      result.addAll(currentBindings);
      for (Annotation newAnnotation : newBindings)
      {
         if (!Reflections.isBindingType(newAnnotation))
         {
            throw new IllegalArgumentException(newAnnotation + " is not a binding type for " + this);
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
    * @param bindingTypes The bindings to merge
    * 
    * @return The union of the binding types
    */
   protected Annotation[] mergeBindings(Annotation... newBindingTypes)
   {
      return mergeBindingTypes(bindingTypes, newBindingTypes).toArray(new Annotation[0]);
   }

}