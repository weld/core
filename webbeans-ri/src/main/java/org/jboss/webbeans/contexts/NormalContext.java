package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;

public abstract class NormalContext extends AbstractContext
{

   public NormalContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
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

}
