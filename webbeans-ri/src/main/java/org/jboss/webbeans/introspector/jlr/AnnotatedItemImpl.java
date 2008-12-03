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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Represents an annotated item
 * 
 * This class is immutable, and thus threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <S>
 */
public class AnnotatedItemImpl<T, S> extends AbstractAnnotatedItem<T, S>
{
   // The actual type arguments
   private final Type[] actualTypeArguments;
   // The type of the item
   private final Class<T> type;
   // The actual annotations
   private final Annotation[] actualAnnotations;

   /**
    * Constructor
    * 
    * @param annotations The annotations array of the type
    * @param type The type of the item
    * @param actualTypeArguments The actual type arguments array
    */
   public AnnotatedItemImpl(Annotation[] annotations, Class<T> type, Type[] actualTypeArguments)
   {
      super(buildAnnotationMap(annotations));
      this.type = type;
      this.actualTypeArguments = actualTypeArguments;
      this.actualAnnotations = annotations;
   }

   /**
    * Gets the delegate (null)
    * 
    * @return null
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getDelegate()
    */
   public S getDelegate()
   {
      return null;
   }

   /**
    * Gets the item type
    * 
    * @return The type
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getType()
    */
   public Class<T> getType()
   {
      return type;
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The actual type arguments array
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   /**
    * Gets the actual annotations
    * 
    * @return The annotations array
    */
   public Annotation[] getActualAnnotations()
   {
      return actualAnnotations;
   }

   /**
    * Indicates if the item is static
    * 
    * @return false
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isStatic()
    */
   public boolean isStatic()
   {
      return false;
   }

   /**
    * Indicates if the item is final
    * 
    * @return false
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#isFinal()
    */
   public boolean isFinal()
   {
      return false;
   }

   /**
    * Gets the name. Should be overridden
    * 
    * @throws IllegalArgumentException.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedItem#getName()
    */
   public String getName()
   {
      throw new IllegalArgumentException("Unable to determine name");
   }

   /**
    * Gets a string representation of the item
    * 
    * @return A string representation
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
//      buffer.append("AnnotatedItemImpl:\n");
//      buffer.append(super.toString() + "\n");
//      buffer.append("Actual type arguments: " + actualTypeArguments.length + "\n");
//      int i = 0;
//      for (Type actualTypeArgument : actualTypeArguments)
//      {
//         buffer.append(++i + " - " + actualTypeArgument.toString());
//      }
//      buffer.append("Actual annotations: " + actualAnnotations.length + "\n");
//      i = 0;
//      for (Annotation actualAnnotation : actualAnnotations)
//      {
//         buffer.append(++i + " - " + actualAnnotation.toString());
//      }
      return buffer.toString();
   }

}
