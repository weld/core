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
import java.util.Set;

public abstract class ForwardingAnnotatedClass<T> extends ForwardingAnnotatedType<T> implements AnnotatedClass<T>
{

   protected abstract AnnotatedClass<T> delegate();

   public Set<AnnotatedConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedConstructors(annotationType);
   }

   public Set<AnnotatedField<?>> getAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedFields(annotationType);
   }

   public Set<AnnotatedMethod<?>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedMethods(annotationType);
   }

   public AnnotatedConstructor<T> getNoArgsConstructor()
   {
      return delegate().getNoArgsConstructor();
   }

   public Set<AnnotatedConstructor<T>> getConstructors()
   {
      return delegate().getConstructors();
   }

   public Set<AnnotatedField<?>> getDeclaredAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedFields(annotationType);
   }

   public Set<AnnotatedMethod<?>> getDeclaredAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedMethods(annotationType);
   }

   public Set<AnnotatedMethod<?>> getDeclaredMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredMethodsWithAnnotatedParameters(annotationType);
   }

   public Set<AnnotatedField<?>> getFields()
   {
      return delegate().getFields();
   }

   public Set<AnnotatedField<?>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotatedFields(metaAnnotationType);
   }

   @Deprecated
   public AnnotatedMethod<?> getMethod(Method method)
   {
      return delegate().getMethod(method);
   }

   public Set<AnnotatedMethod<?>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getMethodsWithAnnotatedParameters(annotationType);
   }

   public AnnotatedClass<?> getSuperclass()
   {
      return delegate().getSuperclass();
   }

   public boolean isNonStaticMemberClass()
   {
      return delegate().isNonStaticMemberClass();
   }

   public boolean isParameterizedType()
   {
      return delegate().isParameterizedType();
   }
   
   public boolean isAbstract()
   {
      return delegate().isAbstract();
   }
   
   @Deprecated
   public AnnotatedMethod<?> getDeclaredMethod(Method method)
   {
      return delegate().getDeclaredMethod(method);
   }
   
   public <F> AnnotatedField<F> getDeclaredField(String fieldName, AnnotatedClass<F> expectedType)
   {
      return delegate().getDeclaredField(fieldName, expectedType);
   }
   
   public <M> AnnotatedMethod<M> getDeclaredMethod(String methodName, AnnotatedClass<M> expectedReturnType, AnnotatedClass<?>... parameterTypes) 
   {
      return delegate().getDeclaredMethod(methodName, expectedReturnType, parameterTypes);
   }
   
   public AnnotatedConstructor<T> getDeclaredConstructor(List<AnnotatedClass<?>> parameterTypes)
   {
      return delegate().getDeclaredConstructor(parameterTypes);
   }
   
}
