package org.jboss.webbeans.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapWrapper<K, V> implements Map<K, V>
{
   
   private Map<K, V> delegate;
   
   

   public MapWrapper(Map<K, V> delegate)
   {
      this.delegate = delegate;
   }

   public void clear()
   {
      delegate.clear();
   }

   public boolean containsKey(Object key)
   {
      return delegate.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return delegate.containsValue(value);
   }

   public Set<Entry<K, V>> entrySet()
   {
      return delegate.entrySet();
   }

   public V get(Object key)
   {
      return delegate.get(key);
   }

   public boolean isEmpty()
   {
      return delegate.isEmpty();
   }

   public Set<K> keySet()
   {
      return delegate.keySet();
   }

   public V put(K key, V value)
   {
      return delegate.put(key, value);
   }

   public void putAll(Map<? extends K, ? extends V> t)
   {
      delegate.putAll(t);
   }

   public V remove(Object key)
   {
      return delegate.remove(key);
   }

   public int size()
   {
      return delegate.size();
   }

   public Collection<V> values()
   {
      return delegate.values();
   }
   
   @Override
   public boolean equals(Object o)
   {
      return delegate.equals(o);
   }
   
   @Override
   public int hashCode()
   {
      return delegate.hashCode();
   }
   
   @Override
   public String toString()
   {
      return delegate.toString();
   }
   
}
