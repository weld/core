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

package org.jboss.webbeans.util.collections;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * Represents a thread safe map
 * 
 * @author Pete Muir
 */
public class ConcurrentCache<K, V> extends ForwardingMap<K, Future<V>>
{

   // The backing map with the value wrapped in a Future instance
   private ConcurrentHashMap<K, Future<V>> map;

   /**
    * Constructor
    */
   public ConcurrentCache()
   {
      map = new ConcurrentHashMap<K, Future<V>>();
   }

   /**
    * Gets the Future value from the map
    *  
    * @param key The key to look for
    * @return The Future instance of the value
    */
   
   public <T extends V> Future<T> getFuture(K key)
   {
      @SuppressWarnings("unchecked")
      Future<T> future = (Future<T>) super.get(key);
      return future;
   }

   /**
    * Gets a value from the map. Blocks until it is available
    *  
    * @param key The key to look for
    * @return The value
    */
   public <T extends V> T getValue(K key)
   {
      @SuppressWarnings("unchecked")
      Future<T> value = (Future<T>) map.get(key);
      if (value != null)
      {
         boolean interrupted = false;
         try
         {
            while (true)
            {
               try
               {
                  return value.get();
               }
               catch (InterruptedException e)
               {
                  interrupted = true;
               }
               catch (ExecutionException e)
               {
                  rethrow(e);
               }
            }
         }
         finally
         {
            if (interrupted)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
      else
      {
         return null;
      }
   }

   /**
    * Adds an item to the map if it's not already there

    * @param key The key to place the item under
    * @param callable The item, wrapped in a Callable instance
    * @return The item added
    */
   public <E> E putIfAbsent(K key, Callable<E> callable)
   {
      @SuppressWarnings("unchecked")
      Future<E> future = (Future<E>) map.get(key);
      Future<E> value = future;
      if (value == null)
      {
         FutureTask<E> task = new FutureTask<E>(callable);
         value = task;
         @SuppressWarnings("unchecked")
         Future<V> t = (Future<V>) task;
         map.put(key, t);
         task.run();
      }
      boolean interrupted = false;
      try
      {
         while (true)
         {
            try
            {
               return value.get();
            }
            catch (InterruptedException e)
            {
               interrupted = true;
            }
            catch (ExecutionException e)
            {
               rethrow(e);
            }
         }
      }
      finally
      {
         if (interrupted)
         {
            Thread.currentThread().interrupt();
         }
      }
   }

   /**
    * Gets the delegate map
    * 
    * @return The backing map
    */
   @Override
   protected Map<K, Future<V>> delegate()
   {
      return map;
   }

   /**
    * Examines and re-throws an exception
    * 
    * @param e The exception that happened during execution
    */
   protected void rethrow(ExecutionException e)
   {
      if (e.getCause() instanceof RuntimeException)
      {
         throw (RuntimeException) e.getCause();
      }
      else if (e.getCause() instanceof Error)
      {
         throw (Error) e.getCause();
      }
      else
      {
         throw new IllegalStateException(e.getCause());
      }
   }

}