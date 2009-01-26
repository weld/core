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
import java.lang.reflect.Field;
import java.util.Set;

import javax.inject.manager.Manager;

public abstract class ForwardingAnnotatedField<T> extends ForwardingAnnotatedMember<T, Field> implements AnnotatedField<T>
{

   @Override
   protected abstract AnnotatedField<T> delegate();

   public T get(Object instance)
   {
      return delegate().get(instance);
   }

   public Field getAnnotatedField()
   {
      return delegate().getAnnotatedField();
   }

   public AnnotatedType<?> getDeclaringClass()
   {
      return delegate().getDeclaringClass();
   }

   public String getPropertyName()
   {
      return delegate().getPropertyName();
   }

   public void inject(Object declaringInstance, Manager manager)
   {
      delegate().inject(declaringInstance, manager);
   }

   public void inject(Object declaringInstance, Object value)
   {
      delegate().inject(declaringInstance, value);
   }

   public void injectIntoInstance(Object declaringInstance, Object value)
   {
      delegate().injectIntoInstance(declaringInstance, value);
   }

   public void injectIntoInstance(Object declaringInstance, Manager manager)
   {
      delegate().injectIntoInstance(declaringInstance, manager);
   }

   public boolean isTransient()
   {
      return delegate().isTransient();
   }

   public AnnotatedField<T> wrap(Set<Annotation> annotations)
   {
      throw new UnsupportedOperationException();
   }
   
      
}
