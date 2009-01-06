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

package org.jboss.webbeans.model;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.DefinitionException;
import javax.webbeans.NonBinding;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.util.Reflections;

/**
 * 
 * Model of a binding type
 * 
 * @author Pete Muir
 * 
 */
public class BindingTypeModel<T extends Annotation> extends AnnotationModel<T>
{
   // The non-binding types
   private Set<AnnotatedMethod<?>> nonBindingTypes;

   /**
    * Constructor
    * 
    * @param type The type
    */
   public BindingTypeModel(Class<T> type)
   {
      super(type);
   }

   /**
    * Initializes the non-binding types and validates the members
    */
   @Override
   protected void init()
   {
      super.init();
      initNonBindingTypes();
      checkArrayAndAnnotationValuedMembers();
   }

   /**
    * Validates the members
    */
   private void checkArrayAndAnnotationValuedMembers()
   {
      for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMembers())
      {
         if ((Reflections.isArrayType(annotatedMethod.getType()) || Annotation.class.isAssignableFrom(annotatedMethod.getType())) && !nonBindingTypes.contains(annotatedMethod))
         {
            throw new DefinitionException("Member of array type or annotation type must be annotated @NonBinding " + annotatedMethod);
         }
      }

   }

   /**
    * Gets the meta-annotation type
    * 
    * @return The BindingType class
    */
   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return BindingType.class;
   }

   /**
    * Indicates if there are non-binding types present
    * 
    * @return True if present, false otherwise
    */
   public boolean hasNonBindingTypes()
   {
      return nonBindingTypes.size() > 0;
   }

   /**
    * Gets the non-binding types
    * 
    * @return A set of non-binding types, or an empty set if there are none
    *         present
    */
   public Set<AnnotatedMethod<?>> getNonBindingTypes()
   {
      return nonBindingTypes;
   }

   /**
    * Initializes the non-binding types
    */
   protected void initNonBindingTypes()
   {
      nonBindingTypes = getAnnotatedAnnotation().getAnnotatedMembers(NonBinding.class);
   }

   /**
    * Comparator for checking equality
    * 
    * @param instance The instance to check against
    * @param other The other binding type
    * @return True if equal, false otherwise
    */
   public boolean isEqual(Annotation instance, Annotation other)
   {
      if (instance.annotationType().equals(getType()) && other.annotationType().equals(getType()))
      {
         for (AnnotatedMethod<?> annotatedMethod : getAnnotatedAnnotation().getMembers())
         {
            if (!nonBindingTypes.contains(annotatedMethod))
            {
               Object thisValue = annotatedMethod.invoke(instance);
               Object thatValue = annotatedMethod.invoke(other);
               if (!thisValue.equals(thatValue))
               {
                  return false;
               }
            }
         }
         return true;
      }
      return false;
   }

   /**
    * Gets a string representation of the binding type model
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
     return (isValid() ? "Valid" : "Invalid") + " binding type model for " + getType() + " with non-binding types " + getNonBindingTypes();
   }

}
