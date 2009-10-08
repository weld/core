package org.jboss.weld.mock.cluster;

import java.util.Hashtable;
import java.util.Map;

import org.jboss.weld.bootstrap.api.Singleton;
import org.jboss.weld.bootstrap.api.SingletonProvider;

public class SwitchableSingletonProvider extends SingletonProvider
{
   
   public static void use(Integer id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id cannot be null");
      }
      SwitchableSingleton.id = id;
   }
   
   private static class SwitchableSingleton<T> implements Singleton<T>
   {
      
      private static Integer id = 0;
      
      private final Map<Integer, T> store;
      
      public SwitchableSingleton()
      {
         this.store = new Hashtable<Integer, T>();
      }

      public void clear()
      {
         store.remove(id);
      }

      public T get()
      {
         return store.get(id);
      }

      public boolean isSet()
      {
         return store.containsKey(id);
      }

      public void set(T object)
      {
         store.put(id, object);         
      }
      
   }

   @Override
   public <T> Singleton<T> create(Class<? extends T> expectedType)
   {
      return new SwitchableSingleton<T>();
   }

}
