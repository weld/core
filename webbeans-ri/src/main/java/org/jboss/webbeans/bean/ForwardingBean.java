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

package org.jboss.webbeans.bean;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

/**
 * A delegating bean
 * 
 * @author Pete Muir
 *
 * @param <T>
 */
public abstract class ForwardingBean<T> extends Bean<T>
{
   
   public ForwardingBean(Manager manager)
   {
      super(manager);
   }

   @Override
   public T create()
   {
      return delegate().create();
   }

   @Override
   public void destroy(T instance)
   {
      delegate().destroy(instance);
   }

   @Override
   public Set<Annotation> getBindingTypes()
   {
      return delegate().getBindingTypes();
   }

   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return delegate().getDeploymentType();
   }

   @Override
   public String getName()
   {
      return delegate().getName();
   }

   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return delegate().getScopeType();
   }

   @Override
   public Set<Class<?>> getTypes()
   {
      return delegate().getTypes();
   }

   @Override
   public boolean isNullable()
   {
      return delegate().isNullable();
   }

   @Override
   public boolean isSerializable()
   {
      return delegate().isSerializable();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }
   
   protected abstract Bean<T> delegate();
   
}
