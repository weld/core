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
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Provides an abstraction for delegating access to an annotated item
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <S>
 */
public abstract class ForwardingWBAnnotated<T, S> extends ForwardingAnnotated<T, S> implements WBAnnotated<T, S>
{

   public Type[] getActualTypeArguments()
   {
      return delegate().getActualTypeArguments();
   }

   public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotations(metaAnnotationType);
   }

   public Annotation[] getMetaAnnotationsAsArray(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getMetaAnnotationsAsArray(metaAnnotationType);
   }

   @Deprecated
   public Set<Annotation> getQualifiers()
   {
      return delegate().getQualifiers();
   }

   @Deprecated
   public Annotation[] getBindingsAsArray()
   {
      return delegate().getBindingsAsArray();
   }

   public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
   {
      return delegate().getDeclaredMetaAnnotations(metaAnnotationType);
   }

   public String getName()
   {
      return delegate().getName();
   }

   public Class<T> getJavaClass()
   {
      return delegate().getJavaClass();
   }

   public boolean isAssignableFrom(WBAnnotated<?, ?> that)
   {
      return delegate().isAssignableFrom(that);
   }

   public boolean isAssignableFrom(Class<?> type, Type[] actualTypeArguments)
   {
      return delegate().isAssignableFrom(type, actualTypeArguments);
   }

   public boolean isFinal()
   {
      return delegate().isFinal();
   }

   public boolean isStatic()
   {
      return delegate().isStatic();
   }

   public boolean isProxyable()
   {
      return delegate().isProxyable();
   }

   public boolean isPublic()
   {
      return delegate().isPublic();
   }
   
   public boolean isPrivate()
   {
      return delegate().isPrivate();
   }
   
   public boolean isPackagePrivate()
   {
      return delegate().isPackagePrivate();
   }
   
   public Package getPackage()
   {
      return delegate().getPackage();
   }

   @Override
   protected abstract WBAnnotated<T, S> delegate();

   public boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationType)
   {
      return delegate().isDeclaredAnnotationPresent(annotationType);
   }

   public Set<Type> getInterfaceOnlyFlattenedTypeHierarchy()
   {
      return delegate().getInterfaceOnlyFlattenedTypeHierarchy();
   }

   public boolean isParameterizedType()
   {
      return delegate().isParameterizedType();
   }

}
