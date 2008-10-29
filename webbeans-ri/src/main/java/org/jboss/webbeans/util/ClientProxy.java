package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.ProxyData;


public class ClientProxy
{
   
   public static boolean isProxyable(Class<?> rawType)
   {
      // TODO Add logging
      
      if (Reflections.getConstructor(rawType) == null)
      {
         return false;
      }
      else if (Reflections.isTypeOrAnyMethodFinal(rawType))
      {
         return false;
      }
      else if (Reflections.isPrimitive(rawType))
      {
         return false;
      }
      else if (Reflections.isArrayType(rawType))
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
   private static MethodHandler methodHandler = new MethodHandler()
   {
      public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable
      {
         ProxyData proxyData = (ProxyData)self;
         Manager manager = proxyData.getManager();
         Class<? extends Annotation> beanScope = proxyData.getBean().getScopeType();
         Context context = manager.getContext(beanScope);
         Object instance = context.get(proxyData.getBean(), true);
         return proceed.invoke(instance, args);
      }
   };

   public static Bean<?> createProxy(ProxyData proxyData) throws InstantiationException, IllegalAccessException
   {
      ProxyFactory proxyFactory = new ProxyFactory();
      proxyFactory.setSuperclass(proxyData.getClass());
      Class<?> proxyClass = proxyFactory.createClass();
      Bean<?> proxy = (Bean<?>) proxyClass.newInstance();
      ((ProxyObject)proxy).setHandler(methodHandler);      
      return proxy;
   }   
   
}
