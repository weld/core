package org.jboss.weld.util.collections;

import java.util.Iterator;

import com.google.common.base.Function;

public class IterableToIteratorFunction<T> implements Function<Iterable<T>, Iterator<T>>
{

   private static final Function<?, ?> INSTANCE = new IterableToIteratorFunction<Object>();
   
   @SuppressWarnings("unchecked")
   public static <T> Function<Iterable<T>, Iterator<T>> instance()
   {
      return (Function<Iterable<T>, Iterator<T>>) INSTANCE;
   }
   
   public Iterator<T> apply(Iterable<T> iterable)
   {
      return iterable.iterator();
   }
   
}