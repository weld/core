package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

import com.google.common.collect.ForwardingMap;

/**
 * Basic implementation of javax.webbeans.Context, backed by a HashMap
 * 
 * @author Shane Bryzak
 * @author Nicklas Karlsson (nickarls@gmail.com)
 * @author Pete Muir
 * 
 */
public abstract class AbstractContext implements Context
{
   
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
      protected Map<Bean<? extends Object>, Object> delegate()
      {
         return delegate;
      }

   }
   
   private Class<? extends Annotation> scopeType;
   protected AtomicBoolean active;

   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      active = new AtomicBoolean(true);
   }

   public abstract <T> T get(Bean<T> bean, boolean create);

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public abstract void destroy(Manager manager);

   public boolean isActive()
   {
      return active.get();
   }

   public void setActive(boolean value)
   {
      active.set(value);
   }

}
