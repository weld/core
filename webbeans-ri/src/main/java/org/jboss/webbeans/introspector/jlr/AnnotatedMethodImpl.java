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
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.Strings;

/**
 * Represents an annotated method
 * 
 * This class is immutable and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedMethodImpl<T> extends AbstractAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   // The actual type arguments
   private final Type[] actualTypeArguments;
   // The underlying method
   private final Method method;

   // The abstracted parameters
   private final List<AnnotatedParameter<Object>> parameters;
   // A mapping from annotation type to parameter abstraction with that
   // annotation present
   private final AnnotatedParameterMap annotatedParameters;

   // The property name
   private final String propertyName;

   // The abstracted declaring class
   private final AnnotatedType<?> declaringClass;

   // Cached string representation
   private String toString;

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map, sets the method
    * and declaring class abstraction and detects the actual type arguments
    * 
    * @param method The underlying method
    * @param declaringClass The declaring class abstraction
    */
   @SuppressWarnings("unchecked")
   public AnnotatedMethodImpl(Method method, AnnotatedType<?> declaringClass)
   {
      super(buildAnnotationMap(method), method);
      this.method = method;
      this.declaringClass = declaringClass;
      if (method.getGenericReturnType() instanceof ParameterizedType)
      {
         this.actualTypeArguments = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
      }
      else
      {
         this.actualTypeArguments = new Type[0];
      }

      this.parameters = new ArrayList<AnnotatedParameter<Object>>();
      this.annotatedParameters = new AnnotatedParameterMap();
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(method.getParameterAnnotations()[i], (Class<Object>) clazz);
            this.parameters.add(parameter);
            for (Annotation annotation : parameter.getAnnotations())
            {
               annotatedParameters.put(annotation.annotationType(), parameter);
            }
         }
         else
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(new Annotation[0], (Class<Object>) clazz);
            this.parameters.add(parameter);
            for (Annotation annotation : parameter.getAnnotations())
            {
               annotatedParameters.put(annotation.annotationType(), parameter);
            }
         }
      }

      String propertyName = Reflections.getPropertyName(getDelegate());
      if (propertyName == null)
      {
         this.propertyName = getName();
      }
      else
      {
         this.propertyName = propertyName;
      }
   }

   /**
    * Gets the annotated method
    * 
    * @return The method
    */
   public Method getAnnotatedMethod()
   {
      return method;
   }

   /**
    * Gets the delegate
    * 
    * @return The delegate
    */
   public Method getDelegate()
   {
      return method;
   }

   /**
    * Gets the type of the method
    * 
    * @return The return type of the method
    */
   @SuppressWarnings("unchecked")
   public Class<T> getType()
   {
      return (Class<T>) method.getReturnType();
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The actual type arguments
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   /**
    * Gets the annotated parameters
    * 
    * If the parameters are null, they are initialized first
    * 
    * @return The annotated parameters
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#getParameters()
    */
   public List<AnnotatedParameter<Object>> getParameters()
   {
      return Collections.unmodifiableList(parameters);
   }

   /**
    * Gets the parameter abstractions with a given annotation type
    * 
    * If the parameter abstractions are null, they are initialized first
    * 
    * @param annotationType The annotation type to match
    * @return The list of abstracted parameters with given annotation type
    *         present. An empty list is returned if there are no matches
    */
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableList(annotatedParameters.get(annotationType));
   }

   /**
    * Compares two annotated methods (delegates)
    * 
    * @return True if equals, false otherwise
    */
   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AnnotatedMethod)
      {
         AnnotatedMethod<?> that = (AnnotatedMethod<?>) other;
         return this.getDeclaringClass().equals(that.getDeclaringClass()) && this.getName().equals(that.getName()) && this.getParameters().equals(that.getParameters());
      }
      else
      {
         return false;
      }
   }

   /**
    * Gets the hash code (of the delegate)
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

   /**
    * Invokes the method on an instance with current parameters from manager
    * 
    * @param mananger The Web Beans manager
    * @param instance The instance to invoke on
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#invoke(ManagerImpl,
    *      Object)
    */
   @SuppressWarnings("unchecked")
   public T invoke(Object instance)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, getParameterValues(parameters));
   }

   /**
    * Invokes the method on an instance with given parameters
    * 
    * @param instance The instance to invoke on
    * @param parameters The parameters
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#invoke(Object,
    *      Object...)
    */
   @SuppressWarnings("unchecked")
   public T invoke(Object instance, Object... parameters)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, parameters);
   }

   /**
    * Gets the name of the property
    * 
    * @return The name
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#getPropertyName()
    */
   public String getPropertyName()
   {
      return propertyName;
   }

   /**
    * Gets the declaring class
    * 
    * @return The declaring class
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedMethod#getDeclaringClass()
    */
   public AnnotatedType<?> getDeclaringClass()
   {
      return declaringClass;
   }

   /**
    * Gets a string representation of the method
    * 
    * @return A string representation
    */
   public String toString()
   {
      if (toString != null)
      {
         return toString;
      }
      StringBuilder buffer = new StringBuilder();
      buffer.append("AnnotatedMethodImpl:\n");
      buffer.append(super.toString() + "\n");
      buffer.append(Strings.collectionToString("Actual type arguments: ", Arrays.asList(getActualTypeArguments())));
      buffer.append(annotatedParameters == null ? "" : (annotatedParameters.toString() + "\n"));
      buffer.append("Declaring class:\n");
      buffer.append(declaringClass.getName() + "[ " + declaringClass.getType() + "]" + "\n");
      buffer.append("Method:\n");
      buffer.append(method.toString());
      buffer.append("Property name: " + propertyName + "\n");
      buffer.append(Strings.collectionToString("Parameters: ", getParameters()));
      toString = buffer.toString();
      return toString;
   }

}
