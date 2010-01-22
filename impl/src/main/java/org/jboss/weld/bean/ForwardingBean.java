/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * A delegating bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public abstract class ForwardingBean<T> implements Bean<T>
{

   /**
    * Creates an instance of the delegate
    * 
    * @return an instance of the delegate
    */
   public T create(CreationalContext<T> creationalContext)
   {
      return delegate().create(creationalContext);
   }

   /**
    * Abstract getter for the delegate
    * 
    * @return The delegate
    */
   protected abstract Bean<T> delegate();

   /**
    * Destroys an instance through the delegate
    * 
    * @param instance The instance to destroy
    */
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      delegate().destroy(instance, creationalContext);
   }

   /**
    * Compares an object with the delegate
    * 
    * @return True if equals, false otherwise
    */
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }

   public Class<?> getBeanClass()
   {
      return delegate().getBeanClass();
   }

   /**
    * Gets the binding types of the delegate
    * 
    * @return The binding types
    */
   public Set<Annotation> getQualifiers()
   {
      return delegate().getQualifiers();
   }

   public Set<InjectionPoint> getInjectionPoints()
   {
      return delegate().getInjectionPoints();
   }

   /**
    * Gets the name of the delegate
    * 
    * @return The name
    */
   public String getName()
   {
      return delegate().getName();
   }

   /**
    * The stereotypes applied to this bean
    * 
    * @return stereotypes if any
    */
   public Set<Class<? extends Annotation>> getStereotypes()
   {
      return delegate().getStereotypes();
   }

   /**
    * Gets the scope type of the delegate
    * 
    * @return The scope type
    */
   public Class<? extends Annotation> getScope()
   {
      return delegate().getScope();
   }

   /**
    * Gets the API types of the delegate
    * 
    * @return The API types
    */
   public Set<Type> getTypes()
   {
      return delegate().getTypes();
   }

   /**
    * Gets the hash code of the delegate
    * 
    * @return The hash code
    */
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }

   /**
    * Indicates if the delegate is nullable
    * 
    * @return True if nullable, false otherwise
    */
   public boolean isNullable()
   {
      return delegate().isNullable();
   }

   public boolean isAlternative()
   {
      return delegate().isAlternative();
   }

   /**
    * Returns a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return "ForwardingBean " + getName() + " for " + delegate().toString();
   }

}
