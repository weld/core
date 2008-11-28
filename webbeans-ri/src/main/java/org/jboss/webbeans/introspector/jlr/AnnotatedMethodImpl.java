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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.util.Reflections;

import com.google.common.collect.ForwardingMap;

/**
 * Represents an annotated method
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedMethodImpl<T> extends AbstractAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   /**
    * A annotation type -> list of parameter abstractions with given annotations
    * present mapping
    */
   private class AnnotatedParameters extends ForwardingMap<Class<? extends Annotation>, List<AnnotatedParameter<Object>>>
   {
      private Map<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> delegate;

      public AnnotatedParameters()
      {
         delegate = new HashMap<Class<? extends Annotation>, List<AnnotatedParameter<Object>>>();
      }

      @Override
      protected Map<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> delegate()
      {
         return delegate;
      }

      @Override
      public String toString()
      {
         StringBuffer buffer = new StringBuffer();
         buffer.append("Annotation type -> parameter abstraction mappings: " + super.size() + "\n");
         int i = 0;
         for (Entry<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> entry : delegate.entrySet())
         {
            for (AnnotatedParameter<?> parameter : entry.getValue())
            {
               buffer.append(++i + " - " + entry.getKey().toString() + ": " + parameter.toString() + "\n");
            }
         }
         return buffer.toString();
      }
   }

   // The actual type arguments
   private Type[] actualTypeArguments = new Type[0];
   // The underlying method
   private Method method;

   // The abstracted parameters
   private List<AnnotatedParameter<Object>> parameters;
   // A mapping from annotation type to parameter abstraction with that
   // annotation present
   private AnnotatedParameters annotatedParameters;

   // The property name
   private String propertyName;

   // The abstracted declaring class
   private AnnotatedType<?> declaringClass;

   /**
    * Constructor
    * 
    * Initializes the superclass with the built annotation map, sets the method
    * and declaring class abstraction and detects the actual type arguments
    * 
    * @param method The underlying method
    * @param declaringClass The declaring class abstraction
    */
   public AnnotatedMethodImpl(Method method, AnnotatedType<?> declaringClass)
   {
      super(buildAnnotationMap(method));
      this.method = method;
      this.declaringClass = declaringClass;
      if (method.getGenericReturnType() instanceof ParameterizedType)
      {
         actualTypeArguments = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments();
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
      if (parameters == null)
      {
         initParameters();
      }
      return parameters;
   }

   /**
    * Initializes the parameter abstractions
    * 
    * Iterates over the method abstraction parameters and creates an abstraction
    * of the parameter
    */
   @SuppressWarnings("unchecked")
   private void initParameters()
   {
      this.parameters = new ArrayList<AnnotatedParameter<Object>>();
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(method.getParameterAnnotations()[i], (Class<Object>) clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(new Annotation[0], (Class<Object>) clazz);
            parameters.add(parameter);
         }
      }
   }

   // TODO: Don't get this one - NIK
   // public List<AnnotatedParameter<Object>> getAnnotatedMethods(Class<?
   // extends Annotation> annotationType)
   // {
   // if (annotatedParameters == null)
   // {
   // initAnnotatedParameters();
   // }
   //
   // if (!annotatedParameters.containsKey(annotationType))
   // {
   // return new ArrayList<AnnotatedParameter<Object>>();
   // }
   // else
   // {
   // return annotatedParameters.get(annotationType);
   // }
   // }

   /**
    * Initializes the annotated parameters
    * 
    * If the parameters are null, they are initialized first. Iterates over the
    * parameter abstractions and for each annotation present, maps the parameter
    * abstraction under that annotation type key.
    */
   private void initAnnotatedParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      annotatedParameters = new AnnotatedParameters();
      for (AnnotatedParameter<Object> parameter : parameters)
      {
         for (Annotation annotation : parameter.getAnnotations())
         {
            if (!annotatedParameters.containsKey(annotation))
            {
               annotatedParameters.put(annotation.annotationType(), new ArrayList<AnnotatedParameter<Object>>());
            }
            annotatedParameters.get(annotation.annotationType()).add(parameter);
         }
      }
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
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new ArrayList<AnnotatedParameter<Object>>();
      }
      return annotatedParameters.get(annotationType);
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
         return this.getDelegate().equals(that.getDelegate());
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
   public T invoke(ManagerImpl manager, Object instance)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, getParameterValues(parameters, manager));
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
      if (propertyName == null)
      {
         propertyName = Reflections.getPropertyName(getDelegate());
         if (propertyName == null)
         {
            propertyName = getName();
         }
      }
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
      StringBuffer buffer = new StringBuffer();
      buffer.append("AnnotatedMethodImpl:\n");
      buffer.append(super.toString() + "\n");
      buffer.append("Actual type arguments: " + actualTypeArguments.length + "\n");
      int i = 0;
      for (Type actualTypeArgument : actualTypeArguments)
      {
         buffer.append(++i + " - " + actualTypeArgument.toString());
      }
      buffer.append(annotatedParameters == null ? "" : (annotatedParameters.toString() + "\n"));
      buffer.append("Declaring class:\n");
      buffer.append(declaringClass.toString());
      buffer.append("Method:\n");
      buffer.append(method.toString());
      buffer.append("Property name: " + propertyName + "\n");
      i = 0;
      buffer.append("Parameters: " + getParameters().size() + "\n");
      for (AnnotatedParameter<?> parameter : parameters)
      {
         buffer.append(++i + " - " + parameter.toString() + "\n");
      }
      return buffer.toString();
   }

}
