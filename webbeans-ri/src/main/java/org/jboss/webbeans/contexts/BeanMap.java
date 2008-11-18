package org.jboss.webbeans.contexts;

import javax.webbeans.manager.Bean;

public interface BeanMap
{
   public abstract <T extends Object> T get(Bean<? extends T> bean);
   public abstract <T extends Object> T remove(Bean<? extends T> bean);
   public abstract void clear();
   public abstract Iterable<Bean<? extends Object>> keySet();
   public abstract <T extends Object> T put(Bean<? extends T> bean, T instance);
}