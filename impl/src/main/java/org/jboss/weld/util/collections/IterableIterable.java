package org.jboss.weld.util.collections;

import java.util.Iterator;

import com.google.common.collect.Iterators;

public class IterableIterable<T> implements Iterable<T>
{
   
   private final Iterable<Iterable<T>> iterables;
   
   public IterableIterable(Iterable<Iterable<T>> iterables)
   {
      this.iterables = iterables;
   }

   public Iterator<T> iterator()
   {
      return Iterators.concat(Iterators.transform(iterables.iterator(), IterableToIteratorFunction.<T>instance()));
   }

}
