package org.jboss.webbeans.contexts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.webbeans.manager.Bean;

import com.google.common.collect.ForwardingMap;

public class BeanMap extends ForwardingMap<Bean<? extends Object>, Object>
{

   protected Map<Bean<? extends Object>, Object> delegate;

   public BeanMap()
   {
      delegate = new ConcurrentHashMap<Bean<? extends Object>, Object>();
   }

   @SuppressWarnings("unchecked")
   public <T extends Object> T get(Bean<? extends T> key)
   {
      return (T) super.get(key);
   }

   @Override
   public Map<Bean<? extends Object>, Object> delegate()
   {
      return delegate;
   }

}