package org.jboss.webbeans.util;

import java.util.HashMap;

import javax.webbeans.manager.Bean;

public class BeanMap<V> extends MapWrapper<Bean<? extends V>, V>
{

   public BeanMap()
   {
      super(new HashMap<Bean<? extends V>, V>());
   }
   
   @SuppressWarnings("unchecked")
   public <T extends V> T get(Bean<? extends T> key)
   {
      return (T) super.get(key);
   }

}
