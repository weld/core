package org.jboss.webbeans.contexts;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

/**
 * Base for the Context implementations
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 *
 */
public abstract class AbstractContext implements Context
{
   
   private Class<? extends Annotation> scopeType;

   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
   }

   /**
    * Get the bean if it exists in the contexts.
    * 
    * @param create If true, a new instance of the bean will be created if none
    * exists
    * 
    * @throws ContextNotActiveException if the context is not active
    *  
    */
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

   /**
    * Get the scope the context is for
    */
   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   /**
    * Return true if the context is active
    */
   public boolean isActive()
   {
      return getActive().get();
   }
   
   /** 
    * Set the context active, internal API for WBRI
    */
   public void setActive(boolean active) {
      getActive().set(active);
   }
   
   // TODO Do we need this
   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(getBeanMap().get(bean));
   }

   // TODO Do we need this
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
