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

import javax.webbeans.DefinitionException;

import org.jboss.webbeans.introspector.AnnotatedAnnotation;
import org.jboss.webbeans.introspector.jlr.AnnotatedAnnotationImpl;

/**
 * Abstract representation of an annotation model
 * 
 * @author Pete Muir
 */
public abstract class AnnotationModel<T extends Annotation>
{
   // The underlying annotation
   private AnnotatedAnnotation<T> annotatedAnnotation;
   // Is the data valid?
   private boolean valid;

   /**
    * Constructor
    * 
    * @param type The annotation type
    */
   public AnnotationModel(Class<T> type)
   {
      this.annotatedAnnotation = new AnnotatedAnnotationImpl<T>(type);
      init();
   }

   /**
    * Initializes the type and validates it
    */
   protected void init()
   {
      initType();
      initValid();
   }

   /**
    * Initializes the type
    */
   protected void initType()
   {
      if (!Annotation.class.isAssignableFrom(getType()))
      {
         throw new DefinitionException(getMetaAnnotation().toString() + " can only be applied to an annotation, it was applied to " + getType());
      }
   }

   /**
    * Validates the data for correct annotation
    */
   protected void initValid()
   {
      this.valid = annotatedAnnotation.isAnnotationPresent(getMetaAnnotation());
   }

   /**
    * Gets the type of the annotation
    * 
    * @return The type
    */
   public Class<T> getType()
   {
      return annotatedAnnotation.getType();
   }

   /**
    * Gets the meta-annotation that should be present
    * 
    * @return
    */
   protected abstract Class<? extends Annotation> getMetaAnnotation();

   /**
    * Indicates if the annotation is valid
    * 
    * @return True if valid, false otherwise
    */
   public boolean isValid()
   {
      return valid;
   }

   /**
    * Gets the annotated annotation
    * 
    * @return The annotation
    */
   protected AnnotatedAnnotation<T> getAnnotatedAnnotation()
   {
      return annotatedAnnotation;
   }

   /**
    * Gets a string representation of the annotation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("AnnotationModel:\n");
      buffer.append("Annotated annotation: " + getAnnotatedAnnotation().toString());
      buffer.append("Valid: " + isValid());
      return buffer.toString();
   }
}