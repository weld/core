package org.jboss.weld.util.collections;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;

public class ArrayListSupplier<T> implements Supplier<List<T>>
{
   
   private static final Supplier<?> INSTANCE = new ArrayListSupplier<Object>();
   
   @SuppressWarnings("unchecked")
   public static <T> Supplier<List<T>> instance()
   {
      return (Supplier<List<T>>) INSTANCE;
   }
   
   private ArrayListSupplier() {}
   
   public List<T> get()
   {
      return new ArrayList<T>();
   }

}
