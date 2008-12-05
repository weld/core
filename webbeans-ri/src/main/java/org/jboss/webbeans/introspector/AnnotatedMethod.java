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

package org.jboss.webbeans.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML
 * 
 * @author Pete Muir
 * 
 */
public interface AnnotatedMethod<T> extends AnnotatedItem<T, Method>
{

   /**
    * Gets the abstracted parameters of the method
    * 
    * @return A list of parameters. Returns an empty list if no parameters are
    *         present.
    */
   public List<AnnotatedParameter<Object>> getParameters();

   /**
    * Gets the list of annotated parameters for a given meta annotation
    * 
    * @param metaAnnotationType The meta annotation to match
    * @return A set of matching parameter abstractions. Returns an empty list if
    *         there are no matches.
    */
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType);

   /**
    * Invokes the method
    * 
    * @param instance The instance to invoke
    * @return A reference to the instance
    */
   public T invoke(Object instance);

   /**
    * Invokes the observer method
    * 
    * @param instance The instance to invoke
    * @param event the event object
    * @return A reference to the instance
    */
   public T invokeWithSpecialValue(Object instance, Class<? extends Annotation> specialParam, Object specialVal);

   /**
    * Invokes the method
    * 
    * @param instance The instance to invoke
    * @param parameters The method parameters
    * @return A reference to the instance
    */
   public T invoke(Object instance, Object... parameters);

   /**
    * Gets the declaring class
    * 
    * @return An abstraction of the declaring class
    */
   public AnnotatedType<?> getDeclaringClass();

   /**
    * Gets the property name
    * 
    * @return The name
    */
   public String getPropertyName();

}
