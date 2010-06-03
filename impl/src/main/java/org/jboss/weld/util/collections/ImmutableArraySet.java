/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.util.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <p>
 * A {@link Set} which is immutable and backed by a simple array of elements.
 * This provides all the behaviors of a set except for those methods which would
 * otherwise modify the contents of the set.
 * </p>
 * <p>
 * The primary use of this set is for those cases where small sets exists and
 * will not be changed. The savings in memory is significant compared to hash
 * sets which may contain many empty buckets.
 * </p>
 * 
 * @author David Allen
 */
public class ImmutableArraySet<E> implements Set<E>
{

   private Object[] elements;

   public ImmutableArraySet(Collection<E> initialElements)
   {
      addElements(initialElements, initialElements.size());
   }

   public ImmutableArraySet(Collection<E> initialElements, E lastElement)
   {
      addElements(initialElements, initialElements.size() + 1);
      this.elements[initialElements.size()] = lastElement;
   }

   protected void addElements(Collection<E> otherSet, int desiredSize)
   {
      boolean realSet = otherSet instanceof Set<?>;
      Iterator<E> setIterator = otherSet.iterator();
      int i = 0;
      elements = new Object[desiredSize];
      while (setIterator.hasNext())
      {
         E element = setIterator.next();
         if (realSet || !contains(element))
         {
            elements[i++] = element;
         }
      }
      // Compute the reduction due to duplicates, if any
      int reduceBy = otherSet.size() - i - 1;
      if (reduceBy > 0)
      {
         Object[] newElements = new Object[desiredSize - reduceBy];
         System.arraycopy(elements, 0, newElements, 0, desiredSize - reduceBy);
         elements = newElements;
      }
   }

   public boolean add(E e)
   {
      throw new UnsupportedOperationException();
   }

   public boolean addAll(Collection<? extends E> c)
   {
      throw new UnsupportedOperationException();
   }

   public void clear()
   {
      throw new UnsupportedOperationException();
   }

   public boolean contains(Object o)
   {
      for (int i = 0; i < elements.length; i++)
      {
         if (elements[i] == null)
         {
            break; // End of valid values
         }
         if ((o == elements[i]) || (o.equals(elements[i])))
         {
            return true;
         }
      }
      return false;
   }

   public boolean containsAll(Collection<?> c)
   {
      for (Object object : c)
      {
         if (contains(object))
         {
            return true;
         }
      }
      return false;
   }

   public boolean isEmpty()
   {
      return elements == null ? true : elements.length == 0;
   }

   class UnmodifiableIterator implements Iterator<E>
   {
      private int currentElement = 0;

      public boolean hasNext()
      {
         return elements == null ? false : currentElement < elements.length;
      }

      @SuppressWarnings("unchecked")
      public E next()
      {
         if (!hasNext())
         {
            throw new NoSuchElementException();
         }
         return (E) elements[currentElement++];
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }

   }

   public Iterator<E> iterator()
   {
      return new UnmodifiableIterator();
   }

   public boolean remove(Object o)
   {
      throw new UnsupportedOperationException();
   }

   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }

   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }

   public int size()
   {
      return elements == null ? 0 : elements.length;
   }

   public Object[] toArray()
   {
      return elements == null ? new Object[0] : elements.length == 0 ? elements : Arrays.copyOf(elements, elements.length);
   }

   @SuppressWarnings("unchecked")
   public <T> T[] toArray(T[] a)
   {
      int elementQuantity = size();
      if (a.length < elementQuantity)
      {
         return (T[]) Arrays.copyOf(elements, elements.length, a.getClass());
      }
      if (elementQuantity > 0)
      {
         System.arraycopy(elements, 0, a, 0, elementQuantity);
      }
      for (int i = elementQuantity; i < a.length; i++)
      {
         a[i] = null;
      }
      return a;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj instanceof Set<?>)
      {
         int elementQuantity = size();
         Object[] otherArray = ((Set<?>) obj).toArray();
         if (elementQuantity != otherArray.length)
         {
            return false;
         }
         boolean arraysEqual = true;
         for (int i = 0; i < elementQuantity; i++)
         {
            boolean objFound = false;
            for (int j = 0; j < otherArray.length; j++)
            {
               if (elements[i].equals(otherArray[j]))
               {
                  objFound = true;
                  break;
               }
            }
            if (!objFound)
            {
               arraysEqual = false;
               break;
            }
         }
         return arraysEqual;
      }
      return false;
   }
   
   @Override
   public int hashCode()
   {
      return Arrays.hashCode(elements);
   }

   @Override
   public String toString()
   {
      return Arrays.toString(elements);
   }

}
