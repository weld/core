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

package org.jboss.webbeans.context.api;

import javax.enterprise.context.spi.Contextual;


/**
 * Interface for different implementations of Bean to Bean instance storage.
 * Used primarily by the contexts.
 * 
 * @author Nicklas Karlsson
 * 
 */
public interface BeanStore
{
   /**
    * Gets an instance of a bean from the storage.
    * 
    * @param bean The bean whose instance to return
    * @return The instance. Null if not found
    */
   public abstract <T> BeanInstance<T> get(Contextual<? extends T> bean);

   /**
    * Removes an instance of a bean from the storage
    * 
    * @param bean The bean whose instance to remove
    * @return The removed instance. Null if not found in storage.
    */
   public abstract <T extends Object> T remove(Contextual<? extends T> bean);

   /**
    * Clears the storage of any bean instances
    */
   public abstract void clear();

   /**
    * Returns an Iterable over the current contents in the storage
    * 
    * @return An Iterable over the keys in the storage
    */
   public abstract Iterable<Contextual<? extends Object>> getBeans();

   /**
    * Adds a bean instance to the storage
    * 
    * @param bean The bean type. Used as key
    * @param instance The instance to add
    * @return The instance added
    */
   public abstract <T> void put(BeanInstance<T> beanInstance);
}