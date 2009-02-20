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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotatedType;
import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

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
   private final Type underlyingType;
   private final Class<T> type;
   // The underlying method
   private final Method method;

   // The abstracted parameters
   private final List<AnnotatedParameter<?>> parameters;
   // A mapping from annotation type to parameter abstraction with that
   // annotation present
   private final AnnotatedParameterMap annotatedParameters;

   // The property name
   private final String propertyName;

   // The abstracted declaring class
   private final AnnotatedType<?> declaringClass;

   // Cached string representation
   private String toString;
   
   public static <T> AnnotatedMethodImpl<T> of(Method method, AnnotatedType<?> declaringClass)
   {
      return new AnnotatedMethodImpl<T>(method, declaringClass);
   }

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
   protected AnnotatedMethodImpl(Method method, AnnotatedType<?> declaringClass)
   {
      super(AnnotationStore.of(method), method);
      this.method = method;
      this.method.setAccessible(true);
      this.declaringClass = declaringClass;
      this.type = (Class<T>) method.getReturnType();
      if (method.getGenericReturnType() instanceof ParameterizedType)
      {
         this.underlyingType = method.getGenericReturnType();
         this.actualTypeArguments = ((ParameterizedType) underlyingType).getActualTypeArguments();
      }
      else
      {
         this.underlyingType = type;
         this.actualTypeArguments = new Type[0];
      }

      this.parameters = new ArrayList<AnnotatedParameter<?>>();
      this.annotatedParameters = new AnnotatedParameterMap();
      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(method.getParameterAnnotations()[i], (Class<Object>) clazz, this);
            this.parameters.add(parameter);
            for (Annotation annotation : parameter.getAnnotationsAsSet())
            {
               if (MAPPED_PARAMETER_ANNOTATIONS.contains(annotation.annotationType()))
               {
                  annotatedParameters.put(annotation.annotationType(), parameter);
               }
            }
         }
         else
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(new Annotation[0], (Class<Object>) clazz, this);
            this.parameters.add(parameter);
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

   public Method getAnnotatedMethod()
   {
      return method;
   }

   public Method getDelegate()
   {
      return method;
   }

   @SuppressWarnings("unchecked")
   public Class<T> getType()
   {
      return type;
   }
   
   @Override
   public Type getUnderlyingType()
   {
      return underlyingType;
   }

   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   public List<AnnotatedParameter<?>> getParameters()
   {
      return Collections.unmodifiableList(parameters);
   }
   
   public Class<?>[] getParameterTypesAsArray()
   {
      return method.getParameterTypes();
   }

   public List<AnnotatedParameter<?>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableList(annotatedParameters.get(annotationType));
   }

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
   
   public boolean isEquivalent(Method method)
   {
      return this.getDeclaringClass().isEquivalent(method.getDeclaringClass()) && this.getName().equals(method.getName()) && Arrays.equals(this.getParameterTypesAsArray(), method.getParameterTypes());
   }

   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }
   
   public T invokeOnInstance(Object instance, Object...parameters) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      @SuppressWarnings("unchecked")
      T result = (T) instance.getClass().getMethod(getName(), getParameterTypesAsArray()).invoke(instance, parameters);
      return result;
   }

   public T invoke(Object instance, Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      @SuppressWarnings("unchecked")
      T result = (T) method.invoke(instance, parameters);
      return result;
   }

   public String getPropertyName()
   {
      return propertyName;
   }

   public AnnotatedType<?> getDeclaringClass()
   {
      return declaringClass;
   }

   @Override
   public String toString()
   {
      if (toString != null)
      {
         return toString;
      }
      toString = "Annotated method " + Names.methodToString(method);
      return toString;
   }
      

}
