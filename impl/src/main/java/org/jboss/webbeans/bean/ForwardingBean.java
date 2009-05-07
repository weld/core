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
import java.lang.reflect.Type;
import java.util.Set;

import javax.context.CreationalContext;
import javax.inject.manager.Bean;
import javax.inject.manager.InjectionPoint;
import javax.inject.manager.Manager;

/**
 * A delegating bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public abstract class ForwardingBean<T> extends Bean<T>
{

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   public ForwardingBean(Manager manager)
   {
      super(manager);
   }

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
    * Destroys an instance through the delegate
    * 
    * @param instance The instance to destroy
    */
   public void destroy(T instance)
   {
      delegate().destroy(instance);
   }

   /**
    * Gets the binding types of the delegate
    * 
    * @return The binding types
    */
   @Override
   public Set<Annotation> getBindings()
   {
      return delegate().getBindings();
   }

   /**
    * Gets the deployment types of the delegate
    * 
    * @return The deployment types
    */
   @Override
   public Class<? extends Annotation> getDeploymentType()
   {
      return delegate().getDeploymentType();
   }

   /**
    * Gets the name of the delegate
    * 
    * @return The name
    */
   @Override
   public String getName()
   {
      return delegate().getName();
   }

   /**
    * Gets the scope type of the delegate
    * 
    * @return The scope type
    */
   @Override
   public Class<? extends Annotation> getScopeType()
   {
      return delegate().getScopeType();
   }

   /**
    * Gets the API types of the delegate
    * 
    * @return The API types
    */
   @Override
   public Set<Type> getTypes()
   {
      return delegate().getTypes();
   }

   /**
    * Indicates if the delegate is nullable
    * 
    * @return True if nullable, false otherwise
    */
   @Override
   public boolean isNullable()
   {
      return delegate().isNullable();
   }

   /**
    * Indicates if the delegate is serializable
    * 
    * @return True if serializable, false otherwise
    */
   @Override
   public boolean isSerializable()
   {
      return delegate().isSerializable();
   }
   
   @Override
   public Set<InjectionPoint> getInjectionPoints()
   {
      return delegate().getInjectionPoints();
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
    * Compares an object with the delegate
    * 
    * @return True if equals, false otherwise
    */
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }

   /**
    * Abstract getter for the delegate
    * 
    * @return The delegate
    */
   protected abstract Bean<T> delegate();

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
