package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

public abstract class AbstractContext implements Context
{
   private Class<? extends Annotation> scopeType;

   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
   }

   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      T instance = getBeanMap().get(bean);
      if (instance != null)
      {
         return instance;
      }
      if (!create)
      {
         return null;
      }

      // TODO should bean creation be synchronized?
      instance = bean.create();
      getBeanMap().put(bean, instance);
      return instance;
   }

   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   public boolean isActive()
   {
      return getActive().get();
   }
   
   public void setActive(boolean active) {
      getActive().set(active);
   }
   
   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(getBeanMap().get(bean));
   }

   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : getBeanMap().keySet())
      {
         destroy(manager, bean);
      }
      getBeanMap().clear();
   }   
   
   protected abstract BeanMap getBeanMap();
   protected abstract AtomicBoolean getActive();

}
