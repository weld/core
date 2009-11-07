package org.jboss.weld.bean.builtin.ee;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;

public abstract class AbstractEEBean<T> extends AbstractBuiltInBean<T>
{
   
   private final T proxy;
   private final Class<T> type;
   private final Set<Type> types;

   protected AbstractEEBean(Class<T> type, Callable<T> callable, BeanManagerImpl beanManager)
   {
      super(type.getSimpleName(), beanManager);
      this.type = type;
      this.types = new HashSet<Type>();
      this.types.add(Object.class);
      this.types.add(type);
      try
      {
         this.proxy = Proxies.<T>createProxy(new CallableMethodHandler(callable), TypeInfo.of(getTypes()).add(Serializable.class));
      }
      catch (InstantiationException e)
      {
         throw new DefinitionException("Could not instantiate client proxy for " + this, e);
      }
      catch (IllegalAccessException e)
      {
         throw new DefinitionException("Could not access bean correctly when creating client proxy for " + this, e);
      }
   }

   public T create(CreationalContext<T> creationalContext)
   {
      return proxy;
   }
   
   public void destroy(T instance, CreationalContext<T> creationalContext) 
   {
      // no-op
   }
   
   @Override
   public Class<T> getType()
   {
      return type;
   }
   
   public Set<Type> getTypes()
   {
      return types;
   }

}
