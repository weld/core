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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedParameter;

public abstract class ForwardingWBConstructor<T> extends ForwardingWBMember<T, T, Constructor<T>> implements WBConstructor<T>
{

   @Override
   protected abstract WBConstructor<T> delegate();

   public List<WBParameter<?, ?>> getAnnotatedWBParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedWBParameters(annotationType);
   }

   public WBClass<T> getDeclaringType()
   {
      return delegate().getDeclaringType();
   }

   public List<? extends WBParameter<?, ?>> getWBParameters()
   {
      return delegate().getWBParameters();
   }

   public T newInstance(Object... parameters) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
   {
      return delegate().newInstance(parameters);
   }
   
   public ConstructorSignature getSignature()
   {
      return delegate().getSignature();
   }
   
   public List<AnnotatedParameter<T>> getParameters()
   {
      return delegate().getParameters();
   }
   
}
