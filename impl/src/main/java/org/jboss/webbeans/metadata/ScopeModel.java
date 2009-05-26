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
package org.jboss.webbeans.metadata;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ScopeType;

import org.jboss.webbeans.resources.ClassTransformer;

/**
 * 
 * Model of a scope
 * 
 * @author Pete Muir
 * 
 */
public class ScopeModel<T extends Annotation> extends AnnotationModel<T>
{
   /**
    * Constrctor
    * 
    * @param scope The scope type
    */
   public ScopeModel(Class<T> scope, ClassTransformer classTransformer)
   {
      super(scope, classTransformer);
   }

   /**
    * Indicates if the scope is "normal"
    * 
    * @return True if normal, false otherwise
    */
   public boolean isNormal()
   {
      return getAnnotatedAnnotation().getAnnotation(ScopeType.class).normal();
   }

   /**
    * Indicates if the scope is "passivating"
    * 
    * @return True if passivating, false otherwise
    */
   public boolean isPassivating()
   {
      return getAnnotatedAnnotation().getAnnotation(ScopeType.class).passivating();
   }

   /**
    * Gets the corresponding meta-annotation type class
    * 
    * @return The ScopeType class
    */
   @Override
   protected Class<? extends Annotation> getMetaAnnotation()
   {
      return ScopeType.class;
   }

   /**
    * Gets a string representation of the scope model
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      String valid = isValid() ? "Valid " : "Invalid";
      String normal = isNormal() ? "normal " : "non-normal ";
      String passivating = isPassivating() ? "passivating " : "pon-passivating ";
      return valid + normal + passivating + " scope model for " + getRawType();
   }

}
