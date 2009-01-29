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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.event.AfterTransactionCompletion;
import javax.event.AfterTransactionFailure;
import javax.event.AfterTransactionSuccess;
import javax.event.BeforeTransactionCompletion;
import javax.event.Fires;
import javax.event.IfExists;
import javax.event.Observes;
import javax.inject.Disposes;

/**
 * AnnotatedType provides a uniform access to the annotations on an annotated
 * class defined either in Java or XML
 * 
 * @author Pete Muir
 * 
 */
public interface AnnotatedMethod<T> extends AnnotatedMember<T, Method>
{
   @SuppressWarnings("unchecked")
   public static final Set<Class<? extends Annotation>> MAPPED_PARAMETER_ANNOTATIONS = new HashSet<Class<? extends Annotation>>(Arrays.asList(Disposes.class, Observes.class, Fires.class, IfExists.class, BeforeTransactionCompletion.class, AfterTransactionCompletion.class, AfterTransactionFailure.class, AfterTransactionSuccess.class));

   /**
    * Gets the abstracted parameters of the method
    * 
    * @return A list of parameters. Returns an empty list if no parameters are
    *         present.
    */
   public List<? extends AnnotatedParameter<?>> getParameters();

   /**
    * Gets the list of annotated parameters for a given annotation
    * 
    * @param metaAnnotationType The annotation to match
    * @return A set of matching parameter abstractions. Returns an empty list if
    *         there are no matches.
    */
   public List<AnnotatedParameter<?>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType);
   
   /**
    * Get the parameter types as an array
    */
   public Class<?>[] getParameterTypesAsArray();

   /**
    * Invokes the method
    * 
    * @param instance The instance to invoke
    * @param parameters The method parameters
    * @return A reference to the instance
    */
   public T invoke(Object instance, Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException;
   
   /**
    * Invokes the method on the class of the passed instance, not the declaring 
    * class. Useful with proxies
    * 
    * @param instance The instance to invoke
    * @param manager The Web Beans manager
    * @return A reference to the instance
    */
   public T invokeOnInstance(Object instance, Object... parameters) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException;

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
   
   /**
    * Checks if a this is equivalent to a JLR method
    * 
    * @param method The JLR method
    * @return true if equivalent
    */
   public boolean isEquivalent(Method method);

   public Method getAnnotatedMethod();

}
