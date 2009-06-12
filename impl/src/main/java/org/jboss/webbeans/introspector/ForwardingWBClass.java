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
import java.util.Set;

public abstract class ForwardingWBClass<T> extends ForwardingWBType<T> implements WBClass<T>
{

   protected abstract WBClass<T> delegate();

   public Set<WBConstructor<T>> getAnnotatedConstructors(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedConstructors(annotationType);
   }

   public Set<WBField<?>> getAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedFields(annotationType);
   }

   public Set<WBMethod<?>> getAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedMethods(annotationType);
   }

   public WBConstructor<T> getNoArgsConstructor()
   {
      return delegate().getNoArgsConstructor();
   }

   public Set<WBConstructor<T>> getConstructors()
   {
      return delegate().getConstructors();
   }

   public Set<WBField<?>> getDeclaredAnnotatedFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedFields(annotationType);
   }

   public Set<WBMethod<?>> getDeclaredAnnotatedMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedMethods(annotationType);
   }

   public Set<WBMethod<?>> getDeclaredMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredMethodsWithAnnotatedParameters(annotationType);
   }

   public Set<WBField<?>> getFields()
   {
      return delegate().getFields();
   }

   public Set<WBField<?>> getMetaAnnotatedFields(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotatedFields(metaAnnotationType);
   }

   @Deprecated
   public WBMethod<?> getMethod(Method method)
   {
      return delegate().getMethod(method);
   }

   public Set<WBMethod<?>> getMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getMethodsWithAnnotatedParameters(annotationType);
   }
   
   public Set<WBConstructor<?>> getConstructorsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getConstructorsWithAnnotatedParameters(annotationType);
   }

   public WBClass<?> getSuperclass()
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
   
   public boolean isEnum()
   {
      return delegate().isEnum();
   }
   
   @Deprecated
   public WBMethod<?> getDeclaredMethod(Method method)
   {
      return delegate().getDeclaredMethod(method);
   }
   
   public <F> WBField<F> getDeclaredField(String fieldName, WBClass<F> expectedType)
   {
      return delegate().getDeclaredField(fieldName, expectedType);
   }
   
   public <M> WBMethod<M> getDeclaredMethod(MethodSignature signature, WBClass<M> expectedReturnType) 
   {
      return delegate().getDeclaredMethod(signature, expectedReturnType);
   }
   
   public WBConstructor<T> getDeclaredConstructor(ConstructorSignature signature)
   {
      return delegate().getDeclaredConstructor(signature);
   }
   
   public <U> WBClass<? extends U> asSubclass(WBClass<U> clazz)
   {
      return delegate().asSubclass(clazz);
   }
   
   public <S> S cast(Object object)
   {
      return delegate().<S>cast(object);
   }
   
   
   
}
