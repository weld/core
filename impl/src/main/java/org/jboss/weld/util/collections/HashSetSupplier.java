package org.jboss.weld.util.collections;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Supplier;

public class HashSetSupplier<T> implements Supplier<Set<T>>
{
   
   private static final Supplier<?> INSTANCE = new HashSetSupplier<Object>();
   
   @SuppressWarnings("unchecked")
   public static <T> Supplier<Set<T>> instance()
   {
      return (Supplier<Set<T>>) INSTANCE;
   }
   
   private HashSetSupplier() {}
   
   public Set<T> get()
   {
      return new HashSet<T>();
   }

}
