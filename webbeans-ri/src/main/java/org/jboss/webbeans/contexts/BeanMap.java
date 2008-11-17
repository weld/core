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

}