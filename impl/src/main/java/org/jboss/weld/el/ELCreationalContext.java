package org.jboss.weld.el;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.context.ForwardingWeldCreationalContext;
import org.jboss.weld.context.WeldCreationalContext;

abstract class ELCreationalContext<T> extends ForwardingWeldCreationalContext<T>
{
   
   public static <X> ELCreationalContext<X> of(final WeldCreationalContext<X> creationalContext)
   {
      return new ELCreationalContext<X>()
      {
         
         @Override
         protected WeldCreationalContext<X> delegate()
         {
            return creationalContext;
         }
         
      };
   }

   private final Map<String, Object> dependentInstances;
   
   public ELCreationalContext()
   {
      this.dependentInstances = new HashMap<String, Object>();
   }
   
   public Object putIfAbsent(Bean<?> bean, Callable<Object> value) throws Exception
   {
      if (bean.getScope().equals(Dependent.class))
      {
         if (dependentInstances.containsKey(bean.getName()))
         {
            return dependentInstances.get(bean.getName());
         }
         else
         {
            Object instance = value.call();
            dependentInstances.put(bean.getName(), instance);
            return instance;
         }
      }
      else
      {
         return value.call();
      }
   }
   
}