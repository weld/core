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

package org.jboss.webbeans.introspector.jlr;

import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.util.Reflections;

/**
 * Represents an abstract annotated type
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public abstract class AbstractAnnotatedType<T> extends AbstractAnnotatedItem<T, Class<T>>
{
   // The superclass abstraction of the type
   private AnnotatedClass<Object> superclass;

   /**
    * Constructor
    * 
    * @param annotationMap The annotation map
    */
   public AbstractAnnotatedType(AnnotationMap annotationMap)
   {
      super(annotationMap);
   }

   /**
    * Indicates if the type is static (through the delegate)
    * 
    * @return True if static, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isStatic()
    */
   public boolean isStatic()
   {
      return Reflections.isStatic(getDelegate());
   }

   /**
    * Indicates if the type if final (through the delegate)
    * 
    * @return True if final, false otherwise
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isFinal()
    */
   public boolean isFinal()
   {
      return Reflections.isFinal(getDelegate());
   }

   /**
    * Gets the name of the type
    * 
    * @returns The name (through the delegate)
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getName()
    */
   public String getName()
   {
      return getDelegate().getName();
   }

   /**
    * Gets the superclass abstraction of the type
    * 
    * @return The superclass abstraction
    */
   @SuppressWarnings("unchecked")
   // TODO Fix this
   public AnnotatedClass<Object> getSuperclass()
   {
      if (superclass == null)
      {
         superclass = new AnnotatedClassImpl(getDelegate().getSuperclass());
      }
      return superclass;
   }

   /**
    * Gets a string representation of the annotated type
    * 
    * @return A string representation
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
//      buffer.append("AbstractAnnotatedType:\n");
//      buffer.append(super.toString() + "\n");
//      buffer.append("Superclass: " + (superclass == null ? "" : superclass.toString()) + "\n");
//      buffer.append("Name: " + getName() + "\n");
//      buffer.append("Final: " + isFinal() + "\n");
//      buffer.append("Static: " + isStatic() + "\n");
      return buffer.toString();
   }

}
