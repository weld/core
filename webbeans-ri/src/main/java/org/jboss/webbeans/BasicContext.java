package org.jboss.webbeans;

import java.lang.annotation.Annotation;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.util.BeanMap;

/**
 * Basic implementation of javax.webbeans.Context, backed by a HashMap
 * 
 * @author Shane Bryzak
 * @author Nicklas Karlsson (nickarls@gmail.com)
 */
public class BasicContext implements Context
{
   private BeanMap<Object> beans;
   private Class<? extends Annotation> scopeType;
   private boolean active;

   public BasicContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      beans = new BeanMap<Object>();
      active = true;
   }

   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }
      T instance = beans.get(bean);
      if (instance != null)
      {
         return instance;
      }

      if (!create)
      {
         return null;
      }

      // TODO should component creation be synchronized?

      instance = bean.create();

      beans.put(bean, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public <T> void remove(Manager manager, Bean<T> bean)
   {
      T instance = beans.get(bean);

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

   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : beans.keySet())
      {
         remove(manager, bean);
      }
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
