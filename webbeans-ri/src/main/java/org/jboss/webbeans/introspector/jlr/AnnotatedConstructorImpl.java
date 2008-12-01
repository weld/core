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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.webbeans.ExecutionException;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.AnnotatedType;

/**
 * Represents an annotated constructor
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class AnnotatedConstructorImpl<T> extends AbstractAnnotatedMember<T, Constructor<T>> implements AnnotatedConstructor<T>
{
   // The type arguments
   private static final Type[] actualTypeArguments = new Type[0];
   // The underlying constructor
   private Constructor<T> constructor;

   // The list of parameter abstractions
   private List<AnnotatedParameter<Object>> parameters;
   // The mapping of annotation -> parameter abstraction
   private AnnotatedParameterMap annotatedParameters;

   // The declaring class abstraction
   private AnnotatedType<T> declaringClass;

   /**
    * Constructor
    * 
    * Initializes the superclass with the build annotations map
    * 
    * @param constructor The constructor method
    * @param declaringClass The declaring class
    */
   public AnnotatedConstructorImpl(Constructor<T> constructor, AnnotatedType<T> declaringClass)
   {
      super(buildAnnotationMap(constructor));
      this.constructor = constructor;
      this.declaringClass = declaringClass;
   }

   /**
    * Gets the constructor
    * 
    * @return The constructor
    */
   public Constructor<T> getAnnotatedConstructor()
   {
      return constructor;
   }

   /**
    * Gets the delegate (constructor)
    * 
    * @return The delegate
    */
   public Constructor<T> getDelegate()
   {
      return constructor;
   }

   /**
    * Gets the type of the constructor
    * 
    * @return The type of the constructor
    */
   public Class<T> getType()
   {
      return constructor.getDeclaringClass();
   }

   /**
    * Gets the actual type arguments
    * 
    * @return The type arguments
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedConstructor#getActualTypeArguments()
    */
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }

   /**
    * Gets the abstracted parameters
    * 
    * If the parameters are null, initalize them first
    * 
    * @return A list of annotated parameter abstractions
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedConstructor#getParameters()
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
    * Iterates over the constructor parameters, adding the parameter abstraction
    * to the parameters list.
    */
   @SuppressWarnings("unchecked")
   private void initParameters()
   {
      parameters = new ArrayList<AnnotatedParameter<Object>>();
      for (int i = 0; i < constructor.getParameterTypes().length; i++)
      {
         if (constructor.getParameterAnnotations()[i].length > 0)
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(constructor.getParameterAnnotations()[i], (Class<Object>) clazz);
            parameters.add(parameter);
         }
         else
         {
            Class<? extends Object> clazz = constructor.getParameterTypes()[i];
            AnnotatedParameter<Object> parameter = new AnnotatedParameterImpl<Object>(new Annotation[0], (Class<Object>) clazz);
            parameters.add(parameter);
         }
      }
   }

   /**
    * Gets the parameter abstractions with a given annotation type
    * 
    * if the annotated parameters map is null, it is initialized first.
    * 
    * @param annotationType The annotation type to match
    * @return The list of parameter abstractions with given annotation type. An
    *         empty list is returned if there are no matches.
    */
   public List<AnnotatedParameter<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
      return annotatedParameters.get(annotationType);
   }

   /**
    * Initializes the annotated parameters
    * 
    * If the parameters are null, they are initialized first. Iterate over the
    * parameters and for each parameter annotation map it under the annotation
    * type.
    */
   private void initAnnotatedParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      annotatedParameters = new AnnotatedParameterMap();
      for (AnnotatedParameter<Object> parameter : parameters)
      {
         for (Annotation annotation : parameter.getAnnotations())
         {
            annotatedParameters.put(annotation.annotationType(), parameter);
         }
      }
   }

   /**
    * Gets parameter abstractions with a given annotation type.
    * 
    * If the parameters are null, they are initializes first.
    * 
    * @param annotationType The annotation type to match
    * @return A list of matching parameter abstractions. An empty list is
    *         returned if there are no matches.
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedConstructor#getAnnotatedParameters(Class)
    */
   public List<AnnotatedParameter<Object>> getAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
      return annotatedParameters.get(annotationType);
   }

   /**
    * Creates a new instance
    * 
    * @param manager The Web Beans manager
    * @return An instance
    * 
    * @see org.jboss.webbeans.introspector.AnnotatedConstructor#newInstance(ManagerImpl)
    */
   public T newInstance(ManagerImpl manager)
   {
      try
      {
         // TODO: more details in the exceptions
         return getDelegate().newInstance(getParameterValues(parameters, manager));
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException(e);
      }
      catch (InstantiationException e)
      {
         throw new ExecutionException(e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new ExecutionException(e);
      }
   }

   /**
    * The overridden equals operation
    * 
    * @param other The instance to compare to
    * @return True if equal, false otherwise
    */
   @Override
   public boolean equals(Object other)
   {

      if (super.equals(other) && other instanceof AnnotatedConstructor)
      {
         AnnotatedConstructor<?> that = (AnnotatedConstructor<?>) other;
         return this.getDelegate().equals(that.getDelegate());
      }
      return false;
   }

   /**
    * The overridden hashcode
    * 
    * Gets the hash code from the delegate
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

   /**
    * Gets the declaring class
    * 
    * @return The declaring class
    */
   public AnnotatedType<T> getDeclaringClass()
   {
      return declaringClass;
   }

   /**
    * Gets a string representation of the constructor
    * 
    * @return A string representation
    */
   public String toString()
   {
      StringBuffer buffer = new StringBuffer();
      // buffer.append("AnnotatedConstructorImpl:\n");
      // buffer.append(super.toString() + "\n");
      // buffer.append("Actual type arguments: " + actualTypeArguments.length +
      // "\n");
      // int i = 0;
      // for (Type actualTypeArgument : actualTypeArguments)
      // {
      // buffer.append(++i + " - " + actualTypeArgument.toString());
      // }
      // buffer.append("Declaring class:\n");
      // buffer.append(declaringClass.toString() + "\n");
      // buffer.append("Constructor:\n");
      // buffer.append(constructor.toString() + "\n");
      // buffer.append("Parameters: " + getParameters().size() + "\n");
      // i = 0;
      // for (AnnotatedParameter<?> parameter : getParameters())
      // {
      // buffer.append(++i + " - " + parameter.toString());
      // }
      // buffer.append(annotatedParameters.toString() + "\n");
      return buffer.toString();
   }

}
