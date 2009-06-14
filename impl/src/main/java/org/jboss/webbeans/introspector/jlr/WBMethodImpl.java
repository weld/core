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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.webbeans.introspector.AnnotationStore;
import org.jboss.webbeans.introspector.MethodSignature;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.introspector.WBType;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.collections.multi.ListHashMultiMap;
import org.jboss.webbeans.util.collections.multi.ListMultiMap;

/**
 * Represents an annotated method
 * 
 * This class is immutable and therefore threadsafe
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class WBMethodImpl<T> extends AbstractWBMember<T, Method> implements WBMethod<T>
{

   // The underlying method
   private final Method method;

   // The abstracted parameters
   private final List<WBParameter<?>> parameters;
   // A mapping from annotation type to parameter abstraction with that
   // annotation present
   private final ListMultiMap<Class<? extends Annotation>, WBParameter<?>> annotatedParameters;

   // The property name
   private final String propertyName;

   // The abstracted declaring class
   private final WBType<?> declaringClass;

   // Cached string representation
   private String toString;

   private final MethodSignature signature;

   public static <T> WBMethodImpl<T> of(Method method, WBType<?> declaringClass)
   {
      return new WBMethodImpl<T>(method, declaringClass);
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
   protected WBMethodImpl(Method method, WBType<?> declaringClass)
   {
      super(AnnotationStore.of(method), method, (Class<T>) method.getReturnType(), method.getGenericReturnType());
      this.method = method;
      this.method.setAccessible(true);
      this.declaringClass = declaringClass;
      this.parameters = new ArrayList<WBParameter<?>>();
      this.annotatedParameters = new ListHashMultiMap<Class<? extends Annotation>, WBParameter<?>>();

      for (int i = 0; i < method.getParameterTypes().length; i++)
      {
         if (method.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = method.getParameterTypes()[i];
            Type type = method.getGenericParameterTypes()[i];
            WBParameter<?> parameter = WBParameterImpl.of(method.getParameterAnnotations()[i], (Class<Object>) clazz, type, this);
            this.parameters.add(parameter);
            for (Annotation annotation : parameter.getAnnotations())
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
            Type type = method.getGenericParameterTypes()[i];
            WBParameter<?> parameter = WBParameterImpl.of(new Annotation[0], (Class<Object>) clazz, type, this);
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
      this.signature = new MethodSignatureImpl(this);
   }

   public Method getAnnotatedMethod()
   {
      return method;
   }

   public Method getDelegate()
   {
      return method;
   }

   public List<WBParameter<?>> getParameters()
   {
      return Collections.unmodifiableList(parameters);
   }

   public Class<?>[] getParameterTypesAsArray()
   {
      return method.getParameterTypes();
   }

   public List<WBParameter<?>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return Collections.unmodifiableList(annotatedParameters.get(annotationType));
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof WBMethod)
      {
         WBMethod<?> that = (WBMethod<?>) other;
         return this.getDeclaringType().equals(that.getDeclaringType()) && this.getName().equals(that.getName()) && this.getParameters().equals(that.getParameters());
      }
      else
      {
         return false;
      }
   }

   public boolean isEquivalent(Method method)
   {
      return this.getDeclaringType().isEquivalent(method.getDeclaringClass()) && this.getName().equals(method.getName()) && Arrays.equals(this.getParameterTypesAsArray(), method.getParameterTypes());
   }

   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

   public T invokeOnInstance(Object instance, Object...parameters) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      Method method = Reflections.lookupMethod(getName(), getParameterTypesAsArray(), instance);
      @SuppressWarnings("unchecked")
      T result = (T) method.invoke(instance, parameters);
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

   public WBType<?> getDeclaringType()
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
      toString = "Annotated method on class " + getDeclaringType().getName() + Names.methodToString(method);
      return toString;
   }

   public MethodSignature getSignature()
   {
      return signature;
   }

}
