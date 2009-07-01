/*
 * 
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * 
 * Copyright (C) 2007 Google Inc.
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Pete Muir
 * @author Kevin Bourrillion
 * @author Scott Bonneau
 * 
 */
public class Iterators
{

   static final UnmodifiableIterator<Object> EMPTY_ITERATOR = new UnmodifiableIterator<Object>()
   {
      public boolean hasNext()
      {
         return false;
      }

      public Object next()
      {
         throw new NoSuchElementException();
      }
   };

   /** Returns the empty {@code Iterator}. */
   // Casting to any type is safe since there are no actual elements.
   @SuppressWarnings("unchecked")
   public static <T> UnmodifiableIterator<T> emptyIterator()
   {
      return (UnmodifiableIterator<T>) EMPTY_ITERATOR;
   }
   
   public static <T> Iterable<T> concat(final Iterable<? extends Iterable<? extends T>> inputs)
   {
      return new Iterable<T>()
      {

         public Iterator<T> iterator()
         {
            return concat(inputs.iterator());
         }
         
      };
   }

   /**
    * Combines multiple iterators into a single iterator. The returned iterator
    * iterates across the elements of each iterator in {@code inputs}. The input
    * iterators are not polled until necessary.
    * 
    * <p>
    * The returned iterator supports {@code remove()} when the corresponding
    * input iterator supports it. The methods of the returned iterator may throw
    * {@code NullPointerException} if any of the input iterators are null.
    */
   public static <T> Iterator<T> concat(final Iterator<? extends Iterable<? extends T>> inputs)
   {
      if (inputs == null)
      {
         throw new NullPointerException();
      }

      return new Iterator<T>()
      {
         Iterator<? extends T> current = emptyIterator();
         Iterator<? extends T> removeFrom;

         public boolean hasNext()
         {
            boolean currentHasNext;
            while (!(currentHasNext = current.hasNext()) && inputs.hasNext())
            {
               current = inputs.next().iterator();
            }
            return currentHasNext;
         }

         public T next()
         {
            if (!hasNext())
            {
               throw new NoSuchElementException();
            }
            removeFrom = current;
            return current.next();
         }

         public void remove()
         {
            if (removeFrom == null)
            {
               throw new IllegalStateException("no calls to next() since last call to remove()");
            }
            removeFrom.remove();
            removeFrom = null;
         }
      };
   }

}
