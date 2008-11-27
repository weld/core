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

public class AnnotatedMethodImpl<T> extends AbstractAnnotatedMember<T, Method> implements AnnotatedMethod<T>
{
   
   private Type[] actualTypeArguments = new Type[0];
   
   private Method method;
   
   private List<AnnotatedParameter<Object>> parameters;
   private Map<Class<? extends Annotation>, List<AnnotatedParameter<Object>>> annotatedParameters;

   private String propertyName;

   private AnnotatedType<?> declaringClass;
   
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

   public Method getAnnotatedMethod()
   {
      return method;
   }

   public Method getDelegate()
   {
      return method;
   }
   
   public Class<T> getType()
   {
      return (Class<T>) method.getReturnType();
   }
   
   public Type[] getActualTypeArguments()
   {
      return actualTypeArguments;
   }
   
   public List<AnnotatedParameter<Object>> getParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      return parameters;
   }
   
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
   
   public List<AnnotatedParameter<Object>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      if (annotatedParameters == null)
      {
         initAnnotatedParameters();
      }
       
      if (!annotatedParameters.containsKey(annotationType))
      {
         return new ArrayList<AnnotatedParameter<Object>>();
      }
      else
      {
         return annotatedParameters.get(annotationType);
      }
   }

   private void initAnnotatedParameters()
   {
      if (parameters == null)
      {
         initParameters();
      }
      annotatedParameters = new HashMap<Class<? extends Annotation>, List<AnnotatedParameter<Object>>>();
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
   
   @Override
   public int hashCode()
   {
      return getDelegate().hashCode();
   }

   public T invoke(ManagerImpl manager, Object instance)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, getParameterValues(parameters, manager));
   }
   
   public T invoke(Object instance, Object... parameters)
   {
      return (T) Reflections.invokeAndWrap(getDelegate(), instance, parameters);
   }
   
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

   public AnnotatedType<?> getDeclaringClass()
   {
      return declaringClass;
   }

}
