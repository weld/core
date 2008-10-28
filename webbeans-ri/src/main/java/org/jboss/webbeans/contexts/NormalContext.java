package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Manager;

public abstract class NormalContext extends AbstractContext
{

   private BeanMap beans;

   public NormalContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      beans = new BeanMap();
      active = true;
   }
   
   public <T> T get(Bean<T> bean, boolean create)
   {
      if (!active)
      {
         throw new ContextNotActiveException();
      }
      
      if (beans == null)
      {
         // Context has been destroyed
         return null;
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

      // TODO should bean creation be synchronized?

      instance = bean.create();

      beans.put(bean, instance);
      return instance;
   }
   
   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(beans.get(bean));
   }

   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : beans.keySet())
      {
         destroy(manager, bean);
      }
      beans = new BeanMap();
   }

}
