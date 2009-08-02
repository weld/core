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
package org.jboss.webbeans.metadata.cache;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.introspector.WBAnnotation;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.ClassTransformer;

/**
 * Abstract representation of an annotation model
 * 
 * @author Pete Muir
 */
public abstract class AnnotationModel<T extends Annotation>
{
   private static final Log log = Logging.getLog(AnnotationModel.class);
   
   // The underlying annotation
   private WBAnnotation<T> annotatedAnnotation;
   // Is the data valid?
   protected boolean valid;

   /**
    * Constructor
    * 
    * @param type The annotation type
    */
   public AnnotationModel(Class<T> type, ClassTransformer transformer)
   {
      this.annotatedAnnotation = transformer.loadAnnotation(type);
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
      if (!Annotation.class.isAssignableFrom(getRawType()))
      {
         throw new DefinitionException(getMetaAnnotation().toString() + " can only be applied to an annotation, it was applied to " + getRawType());
      }
   }

   /**
    * Validates the data for correct annotation
    */
   protected void initValid()
   {
      this.valid = true;
      if (!annotatedAnnotation.isAnnotationPresent(getMetaAnnotation()))
      {
         this.valid = false;
      }
      if (annotatedAnnotation.isAnnotationPresent(Retention.class) && !annotatedAnnotation.getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME))
      {
         this.valid = false;
         log.debug("#0 is missing @Retention(RUNTIME)", annotatedAnnotation);
      }
            
   }

   /**
    * Gets the type of the annotation
    * 
    * @return The type
    */
   public Class<T> getRawType()
   {
      return annotatedAnnotation.getJavaClass();
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
   protected WBAnnotation<T> getAnnotatedAnnotation()
   {
      return annotatedAnnotation;
   }

   /**
    * Gets a string representation of the annotation model
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return (isValid() ? "Valid" : "Invalid") + " annotation model for " + getRawType();
   }

}