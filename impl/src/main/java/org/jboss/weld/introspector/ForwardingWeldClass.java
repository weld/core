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
package org.jboss.weld.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

public abstract class ForwardingWeldClass<T> extends ForwardingWeldAnnotated<T, Class<T>> implements WeldClass<T>
{

   @Override
   protected abstract WeldClass<T> delegate();

   public Set<WeldConstructor<T>> getWeldConstructors(Class<? extends Annotation> annotationType)
   {
      return delegate().getWeldConstructors(annotationType);
   }

   public Set<WeldField<?, ?>> getWeldFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getWeldFields(annotationType);
   }

   public Set<WeldMethod<?, ?>> getWeldMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getWeldMethods(annotationType);
   }

   public WeldConstructor<T> getNoArgsWeldConstructor()
   {
      return delegate().getNoArgsWeldConstructor();
   }
   
   public Set<WeldMethod<?, ?>> getWeldMethods()
   {
      return delegate().getWeldMethods();
   }

   public Set<WeldField<?, T>> getDeclaredWeldFields(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredWeldFields(annotationType);
   }

   public Set<WeldMethod<?, T>> getDeclaredWeldMethods(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredWeldMethods(annotationType);
   }

   public Set<WeldMethod<?, T>> getDeclaredWeldMethodsWithAnnotatedParameters(Class<? extends Annotation> annotationType)
   {
      return delegate().getDeclaredWeldMethodsWithAnnotatedParameters(annotationType);
   }

   public Set<WeldField<?, ?>> getWeldFields()
   {
      return delegate().getWeldFields();
   }

   @Deprecated
   public WeldMethod<?, ?> getWeldMethod(Method method)
   {
      return delegate().getWeldMethod(method);
   }
   
   public <M> WeldMethod<M, ?> getWeldMethod(MethodSignature signature)
   {
      return delegate().getWeldMethod(signature);
   }

   public WeldClass<?> getWeldSuperclass()
   {
      return delegate().getWeldSuperclass();
   }

   public boolean isLocalClass()
   {
      return delegate().isLocalClass();
   }
   
   public boolean isMemberClass()
   {
      return delegate().isMemberClass();
   }
   
   public boolean isAnonymousClass()
   {
      return delegate().isAnonymousClass();
   }

   @Override
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

   public boolean isSerializable()
   {
      return delegate().isSerializable();
   }

   @Deprecated
   public WeldMethod<?, ?> getDeclaredWeldMethod(Method method)
   {
      return delegate().getDeclaredWeldMethod(method);
   }
   
   public <F> WeldField<F, ?> getDeclaredWeldField(String fieldName)
   {
      return delegate().getDeclaredWeldField(fieldName);
   }
   
   public <M> WeldMethod<M, ?> getDeclaredWeldMethod(MethodSignature signature, WeldClass<M> expectedReturnType) 
   {
      return delegate().getDeclaredWeldMethod(signature);
   }
   
   public WeldConstructor<T> getDeclaredWeldConstructor(ConstructorSignature signature)
   {
      return delegate().getDeclaredWeldConstructor(signature);
   }
   
   public <U> WeldClass<? extends U> asWeldSubclass(WeldClass<U> clazz)
   {
      return delegate().asWeldSubclass(clazz);
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
   
   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getDeclaredMetaAnnotations(metaAnnotationType);
   }
   
}
