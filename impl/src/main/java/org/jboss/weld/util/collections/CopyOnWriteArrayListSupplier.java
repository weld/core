package org.jboss.weld.util.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.base.Supplier;

public class CopyOnWriteArrayListSupplier<T> implements Supplier<List<T>>
{
   
   private static final Supplier<?> INSTANCE = new CopyOnWriteArrayListSupplier<Object>();
   
   @SuppressWarnings("unchecked")
   public static <T> Supplier<List<T>> instance()
   {
      return (Supplier<List<T>>) INSTANCE;
   }
   
   private CopyOnWriteArrayListSupplier() {}
   
   public List<T> get()
   {
      return new CopyOnWriteArrayList<T>();
   }

}
