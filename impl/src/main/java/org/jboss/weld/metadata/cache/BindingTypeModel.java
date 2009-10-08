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
package org.jboss.weld.metadata.cache;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.enterprise.inject.Nonbinding;
import javax.inject.Qualifier;

import org.jboss.weld.DefinitionException;
import org.jboss.weld.introspector.WBMethod;
import org.jboss.weld.log.Log;
import org.jboss.weld.log.Logging;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.Reflections;
import org.jboss.weld.util.collections.Arrays2;

/**
 * 
 * Model of a binding type
 * 
 * @author Pete Muir
 * 
 */
public class BindingTypeModel<T extends Annotation> extends AnnotationModel<T>
{
   
   
   private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Arrays2.<Class<? extends Annotation>>asSet(Qualifier.class);
   private static final Log log = Logging.getLog(BindingTypeModel.class);
   
   // The non-binding types
   private Set<WBMethod<?, ?>> nonBindingTypes;
   

   /**
    * Constructor
    * 
    * @param type The type
    */
   public BindingTypeModel(Class<T> type, ClassTransformer transformer)
   {
      super(type, transformer);
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
      for (WBMethod<?, ?> annotatedMethod : getAnnotatedAnnotation().getMembers())
      {
         if ((Reflections.isArrayType(annotatedMethod.getJavaClass()) || Annotation.class.isAssignableFrom(annotatedMethod.getJavaClass())) && !nonBindingTypes.contains(annotatedMethod))
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
   protected Set<Class<? extends Annotation>> getMetaAnnotationTypes() 
   {
      return META_ANNOTATIONS;
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
   public Set<WBMethod<?, ?>> getNonBindingTypes()
   {
      return nonBindingTypes;
   }

   /**
    * Initializes the non-binding types
    */
   protected void initNonBindingTypes()
   {
      nonBindingTypes = getAnnotatedAnnotation().getAnnotatedMembers(Nonbinding.class);
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
      if (instance.annotationType().equals(getRawType()) && other.annotationType().equals(getRawType()))
      {
         for (WBMethod<?, ?> annotatedMethod : getAnnotatedAnnotation().getMembers())
         {
            if (!nonBindingTypes.contains(annotatedMethod))
            {
               try
               {
                  Object thisValue = annotatedMethod.invoke(instance);
                  Object thatValue = annotatedMethod.invoke(other);
                  if (!thisValue.equals(thatValue))
                  {
                     return false;
                  }
               }
               catch (IllegalArgumentException e)
               {
                  throw new RuntimeException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new RuntimeException(e);
               }
               catch (InvocationTargetException e)
               {
                  throw new RuntimeException(e);
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
     return (isValid() ? "Valid" : "Invalid") + " binding type model for " + getRawType() + " with non-binding types " + getNonBindingTypes();
   }

}
