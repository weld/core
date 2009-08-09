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
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedParameter;

public abstract class ForwardingWBMethod<T, X> extends ForwardingWBMember<T,X, Method> implements WBMethod<T, X>
{

   @Override
   protected abstract WBMethod<T, X> delegate();

   public Method getAnnotatedMethod()
   {
      return delegate().getAnnotatedMethod();
   }

   public List<WBParameter<?, ?>> getAnnotatedParameters(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getAnnotatedWBParameters(metaAnnotationType);
   }

   public Class<?>[] getParameterTypesAsArray()
   {
      return delegate().getParameterTypesAsArray();
   }

   public List<? extends WBParameter<?, ?>> getWBParameters()
   {
      return delegate().getWBParameters();
   }

   public String getPropertyName()
   {
      return delegate().getPropertyName();
   }

   public T invoke(Object instance, Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
   {
      return delegate().invoke(instance, parameters);
   }

   public T invokeOnInstance(Object instance, Object... parameters) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
   {
      return delegate().invokeOnInstance(instance, parameters);
   }

   public boolean isEquivalent(Method method)
   {
      return delegate().isEquivalent(method);
   }

   public MethodSignature getSignature()
   {
      return delegate().getSignature();
   }

   public List<WBParameter<?, ?>> getAnnotatedWBParameters(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getAnnotatedWBParameters(metaAnnotationType);
   }

   public List<AnnotatedParameter<X>> getParameters()
   {
      return delegate().getParameters();
   }

}
