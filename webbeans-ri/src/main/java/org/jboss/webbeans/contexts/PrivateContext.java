package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public abstract class PrivateContext extends AbstractContext
{
   private ThreadLocal<AtomicBoolean> active;
   private ThreadLocal<BeanMap> beans;
   
   public PrivateContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new ThreadLocal<BeanMap>();
      beans.set(new BeanMap());
      active = new ThreadLocal<AtomicBoolean>();
      active.set(new AtomicBoolean(true));
   }
   
   public void setBeans(BeanMap beans) {
      this.beans.set(beans);
   }
   
   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      T instance = beans.get().get(bean);
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
      beans.get().put(bean, instance);
      return instance;
   }
   
   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(beans.get().get(bean));
   }

   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : beans.get().keySet())
      {
         destroy(manager, bean);
      }
      beans.set(new BeanMap());
   }

   @Override
   public boolean isActive()
   {
      return active.get().get();
   }

   @Override
   public void setActive(boolean value)
   {
      active.get().set(value);
   }

}
