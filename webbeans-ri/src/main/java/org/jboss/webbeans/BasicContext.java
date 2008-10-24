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
 *
 */
public class BasicContext implements Context
{
   private Map<Bean<? extends Object>, Object> values;
   private Class<? extends Annotation> scopeType;
   private boolean active;
   
   public BasicContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      values = new HashMap<Bean<?>,Object>();
      active = true;
   }
   
   public <T> T get(Bean<T> component, boolean create) 
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }
      T instance = (T) values.get(component);
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
      
      values.put(component, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType() 
   {
      return scopeType;
   }

   public <T> void remove(Manager container, Bean<T> bean) 
   {
      T instance = (T) values.get(bean);
      
      if (instance != null)
      {
         values.remove(bean);
         bean.destroy(instance);
      }
      else
      {
         // TODO is this the correct exception to throw? See section 9.1 of spec
         throw new RuntimeException("Component " + bean.getName() + " cannot be removed as it " + 
               "does not exist in [" + scopeType + "] context.");
      }
   }
   
   public void destroy(Manager container)
   {
      for (Bean c : values.keySet())
      {
         c.destroy(values.get(c));
      }
      values.clear();
   }
   
   public boolean isActive() {
      return active;
   }
   
   public void setActive(boolean active) {
      this.active = active;
   }

}
