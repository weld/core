package org.jboss.webbeans.contexts;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.webbeans.manager.Bean;

import com.google.common.collect.ForwardingMap;

/**
 * A BeanMap that uses a simple forwarding HashMap as backing map
 * 
 * @author Nicklas Karlsson
 *
 */
public class SimpleBeanMap extends ForwardingMap<Bean<? extends Object>, Object> implements BeanMap
{

   protected Map<Bean<? extends Object>, Object> delegate;

   public SimpleBeanMap()
   {
      delegate = new ConcurrentHashMap<Bean<? extends Object>, Object>();
   }

   @SuppressWarnings("unchecked")
   public <T extends Object> T get(Bean<? extends T> bean)
   {
      return (T) super.get(bean);
   }

   @Override
   public Map<Bean<? extends Object>, Object> delegate()
   {
      return delegate;
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Object> T remove(Bean<? extends T> bean)
   {
      return (T) super.remove(bean);
   }
   
   public void clear() {
      delegate.clear();
   }
   
   public Set<Bean<? extends Object>> keySet() {
      return delegate.keySet();
   }
   
   @SuppressWarnings("unchecked")
   public <T> T put(Bean<? extends T> bean, T instance)
   {
      return (T) delegate.put(bean, instance);
   }

}