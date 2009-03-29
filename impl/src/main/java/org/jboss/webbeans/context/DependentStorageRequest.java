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
package org.jboss.webbeans.context;

/**
 * A dependent instance store and storage key combination for selecting the correct receiving 
 * store and holding key for dependent instances in the dependent context
 *  
 * @author Nicklas Karlsson
 */
public class DependentStorageRequest
{
   // The dependent instances store to target
   private final DependentInstancesStore dependentInstancesStore;
   // The key in the store
   private final Object key;

   /**
    * Create a new DependentStoreKey
    * 
    * @param dependentInstancesStore The dependent instances store
    * @param key The storage key
    */
   protected DependentStorageRequest(DependentInstancesStore dependentInstancesStore, Object key)
   {
      this.dependentInstancesStore = dependentInstancesStore;
      this.key = key;
   }

   /**
    * Static factory method
    * 
    * @param scopeType The scope type of the dependent instances store
    * @param key The storage key
    * @return A new DependentStoreKey
    */
   public static DependentStorageRequest of(DependentInstancesStore dependentInstancesStore, Object key)
   {
      return new DependentStorageRequest(dependentInstancesStore, key);
   }

   /**
    * Gets the store
    * 
    * @return The store
    */
   public DependentInstancesStore getDependentInstancesStore()
   {
      return dependentInstancesStore;
   }

   /**
    * Gets the key
    * 
    * @return The key
    */
   public Object getKey()
   {
      return key;
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof DependentStorageRequest)
      {
         DependentStorageRequest that = (DependentStorageRequest) other;
         if (this.getDependentInstancesStore().equals(that.getDependentInstancesStore()) && this.getKey().equals(that.getKey()))
         {
            return true;
         }
         else
         {
            return false;
         }
      }
      else
      {
         return false;
      }
   }

}
