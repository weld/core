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

public abstract class ForwardingWBClass<T> extends ForwardingWBAnnotated<T, Class<T>> implements WBClass<T>
{

   protected abstract WBClass<T> delegate();

   public Set<WBConstructor<T>> getAnnotatedWBConstructors(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedWBConstructors(annotationType);
   }

   public Set<WBField<?, ?>> getAnnotatedWBFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedWBFields(annotationType);
   }

   public Set<WBMethod<?, ?>> getAnnotatedWBMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getAnnotatedWBMethods(annotationType);
   }

   public WBConstructor<T> getNoArgsWBConstructor()
   {
      return delegate().getNoArgsWBConstructor();
   }

   public Set<WBConstructor<T>> getWBConstructors()
   {
      return delegate().getWBConstructors();
   }
   
   public Set<WBMethod<?, ?>> getWBMethods()
   {
      return delegate().getWBMethods();
   }

   public Set<WBField<?, ?>> getDeclaredAnnotatedWBFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedWBFields(annotationType);
   }

   public Set<WBMethod<?, ?>> getDeclaredAnnotatedWBMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredAnnotatedWBMethods(annotationType);
   }

   public Set<WBMethod<?, T>> getWBDeclaredMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getWBDeclaredMethodsWithAnnotatedParameters(annotationType);
   }

   public Set<WBField<?, ?>> getWBFields()
   {
      return delegate().getWBFields();
   }

   public Set<WBField<?, ?>> getMetaAnnotatedWBFields(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotatedWBFields(metaAnnotationType);
   }

   @Deprecated
   public WBMethod<?, ?> getWBMethod(Method method)
   {
      return delegate().getWBMethod(method);
   }
   
   public <M> WBMethod<M, ?> getWBMethod(MethodSignature signature)
   {
      return delegate().getWBMethod(signature);
   }

   public Set<WBMethod<?, ?>> getWBMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getWBMethodsWithAnnotatedParameters(annotationType);
   }
   
   public Set<WBConstructor<?>> getWBConstructorsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getWBConstructorsWithAnnotatedParameters(annotationType);
   }

   public WBClass<?> getWBSuperclass()
   {
      return delegate().getWBSuperclass();
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
   public WBMethod<?, ?> getDeclaredWBMethod(Method method)
   {
      return delegate().getDeclaredWBMethod(method);
   }
   
   public <F> WBField<F, ?> getDeclaredWBField(String fieldName, WBClass<F> expectedType)
   {
      return delegate().getDeclaredWBField(fieldName, expectedType);
   }
   
   public <M> WBMethod<M, ?> getDeclaredWBMethod(MethodSignature signature, WBClass<M> expectedReturnType) 
   {
      return delegate().getDeclaredWBMethod(signature, expectedReturnType);
   }
   
   public WBConstructor<T> getDeclaredWBConstructor(ConstructorSignature signature)
   {
      return delegate().getDeclaredWBConstructor(signature);
   }
   
   public <U> WBClass<? extends U> asWBSubclass(WBClass<U> clazz)
   {
      return delegate().asWBSubclass(clazz);
   }
   
   public <S> S cast(Object object)
   {
      return delegate().<S>cast(object);
   }
   
   public boolean isEquivalent(Class<?> clazz)
   {
      return delegate().isEquivalent(clazz);
   }
   
   public String getSimpleName()
   {
      return delegate().getSimpleName();
   }
   
}
