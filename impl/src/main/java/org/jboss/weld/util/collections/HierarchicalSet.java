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

import java.lang.ref.SoftReference;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link Set} backed by an {@link ArrayList} and a parent instance.
 * 
 * @author David Allen
 */
public class HierarchicalSet<E> extends AbstractSet<E>
{
   private SoftReference<HashSet<E>> aggregateSet;
   private final HierarchicalSet<E>  parent;
   private final Collection<E>       initialItems;

   public HierarchicalSet(HierarchicalSet<E> parent, Collection<E> initialItems)
   {
      this.parent = parent;
      this.initialItems = initialItems;
   }

   protected void initializeSet()
   {
      if (isSetCleared())
      {
         HashSet<E> underlyingSet = new HashSet<E>();
         aggregateSet = new SoftReference<HashSet<E>>(underlyingSet);
         addInitialItems(underlyingSet);
      }
   }

   /**
    * First adds the items from the parent, if it exists, and then adds the
    * items from this instance to the given set.
    * 
    * @param resultSet the set to add items to during tail recursion
    */
   protected void addInitialItems(HashSet<E> resultSet)
   {
      if (parent != null)
         parent.addInitialItems(resultSet);
      resultSet.addAll(initialItems);
   }

   protected final boolean isSetCleared()
   {
      return (aggregateSet == null) || (aggregateSet.get() == null);
   }

   @Override
   public Iterator<E> iterator()
   {
      initializeSet();
      return aggregateSet.get().iterator();
   }

   @Override
   public int size()
   {
      initializeSet();
      return aggregateSet.get().size();
   }

   @Override
   public boolean add(E e)
   {
      initializeSet();
      return super.add(e);
   }

   @Override
   public boolean addAll(Collection<? extends E> c)
   {
      initializeSet();
      return aggregateSet.get().addAll(c);
   }

   @Override
   public void clear()
   {
      if (!isSetCleared())
      {
         aggregateSet.get().clear();
      }
   }

   @Override
   public boolean contains(Object o)
   {
      initializeSet();
      return aggregateSet.get().contains(o);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   {
      initializeSet();
      return aggregateSet.get().containsAll(c);
   }

   @Override
   public boolean remove(Object o)
   {
      initializeSet();
      return aggregateSet.get().remove(o);
   }

   @Override
   public boolean retainAll(Collection<?> c)
   {
      initializeSet();
      return aggregateSet.get().retainAll(c);
   }
}
