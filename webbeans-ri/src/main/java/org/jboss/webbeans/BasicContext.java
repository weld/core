package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

/**
 * Basic implementation of javax.webbeans.Context, backed by a HashMap
 * 
 * @author Shane Bryzak
 * @author Nicklas Karlsson (nickarls@gmail.com)
 */
public class BasicContext implements Context
{
   private Map<Bean<? extends Object>, Object> beans;
   private Class<? extends Annotation> scopeType;
   private boolean active;

   public BasicContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      beans = new HashMap<Bean<?>, Object>();
      active = true;
   }

   public void add(Bean<?> component, Object instance)
   {
      beans.put(component, instance);
   }

   public <T> T get(Bean<T> component, boolean create)
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }
      T instance = (T) beans.get(component);
      if (instance != null)
      {
         return instance;
      }

      if (!create)
      {
         return null;
      }

      // TODO should component creation be synchronized?

      instance = component.create();

      beans.put(component, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public <T> void remove(Manager container, Bean<T> bean)
   {
      T instance = (T) beans.get(bean);

      if (instance != null)
      {
         beans.remove(bean);
         bean.destroy(instance);
      }
      else
      {
         // TODO is this the correct exception to throw? See section 9.1 of spec
         throw new RuntimeException("Component " + bean.getName() + " cannot be removed as it " + "does not exist in [" + scopeType + "] context.");
      }
   }

   public void destroy(Manager container)
   {
      for (Bean c : beans.keySet())
      {
         c.destroy(beans.get(c));
      }
      beans.clear();
      active = false;
   }

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

}
