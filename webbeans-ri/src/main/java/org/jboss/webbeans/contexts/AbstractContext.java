package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentHashMap;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.util.MapWrapper;

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
   
   protected class BeanMap extends MapWrapper<Bean<? extends Object>, Object>
   {
      
      public BeanMap()
      {
         super(new ConcurrentHashMap<Bean<? extends Object>, Object>());
      }
      
      @SuppressWarnings("unchecked")
      public <T extends Object> T get(Bean<? extends T> key)
      {
         return (T) super.get(key);
      }

   }
   
   private Class<? extends Annotation> scopeType;
   protected boolean active;

   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      
   }

   public abstract <T> T get(Bean<T> bean, boolean create);

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public abstract void destroy(Manager manager);

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

}
