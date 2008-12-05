package org.jboss.webbeans.util;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.google.common.collect.ForwardingMap;

public class ConcurrentCache<K, V> extends ForwardingMap<K, Future<V>>
{

   private ConcurrentMap<K, Future<V>> map;

   public ConcurrentCache()
   {
      map = new ConcurrentHashMap<K, Future<V>>();
   }

   @SuppressWarnings("unchecked")
   public <T extends V> Future<T> getFuture(K key)
   {
      return (Future<T>) super.get(key);
   }
   
   @SuppressWarnings("unchecked")
   public <T extends V> T getValue(K key)
   {
      Future<T> value = (Future<T>) map.get(key);
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
            };
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
   
   @SuppressWarnings("unchecked")
   public <E> E putIfAbsent(K key, Callable<E> callable)
   {
      Future<E> value = (Future<E>) map.get(key);
      if (value == null)
      {
         FutureTask<E> task = new FutureTask<E>(callable);
         value = task;
         map.put(key, (Future<V>) task);
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
            };
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

   @Override
   protected Map<K, Future<V>> delegate()
   {
      return map;
   }

   @Override
   public String toString()
   {
      return Strings.mapToString("ProxyPool (bean -> proxy): ", map);
   }
   
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