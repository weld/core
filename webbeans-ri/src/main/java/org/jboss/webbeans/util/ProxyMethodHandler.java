package org.jboss.webbeans.util;

import java.io.Serializable;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.ManagerImpl;

public class ProxyMethodHandler implements MethodHandler, Serializable
{
   private static final long serialVersionUID = -5391564935097267888L;

   private transient Bean<?> bean;
   private int beanIndex;
   private static ManagerImpl manager;

   public ProxyMethodHandler(Bean<?> bean, int beanIndex, ManagerImpl manager)
   {
      this.bean = bean;
      this.beanIndex = beanIndex;
      ProxyMethodHandler.manager = manager;
   }

   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      if (bean == null)
      {
         bean = manager.getBeans().get(beanIndex);
      }
      Context context = manager.getContext(bean.getScopeType());
      Object proxiedInstance = context.get(bean, true);
      Method proxiedMethod = getMethod(method, proxiedInstance);
      return proxiedMethod.invoke(proxiedInstance, args);
   }

   private static Method getMethod(Method method, Object instance)
   {
      for (Class<? extends Object> c = instance.getClass(); c!=Object.class; c=c.getSuperclass())
      {
         try
         {
            Method m = c.getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (!m.isAccessible()) m.setAccessible(true);
            return m;
         }
         catch (NoSuchMethodException nsme) {}
      }
      throw new IllegalArgumentException("method not implemented by instance");
   }

}
