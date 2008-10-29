package org.jboss.webbeans.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.ManagerImpl;

public class ClientProxyFactory
{
   private ManagerImpl manager;
   
   public ClientProxyFactory(ManagerImpl manager) {
      this.manager = manager;
   }
   
   public class ProxyMethodHandler implements MethodHandler {
      private ManagerImpl manager;
      
      public ProxyMethodHandler(ManagerImpl manager) {
         this.manager = manager;
      }
      
      @Override
      public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
      {
         Bean<?> bean = (Bean<?>)self;
         Class<? extends Annotation> beanScope = bean.getScopeType();
         Context context = manager.getContext(beanScope);
         Object instance = context.get(bean, true);
         return proceed.invoke(instance, args);
      }
      
   }
   
   public Bean<?> createProxyClient(Bean<?> bean) throws InstantiationException, IllegalAccessException {
      ProxyFactory factory = new ProxyFactory();
      factory.setSuperclass(bean.getClass());
      Class<?> proxyClass = factory.createClass();
      Object proxy = proxyClass.newInstance();
      ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(manager);
      ((ProxyObject)proxy).setHandler(proxyMethodHandler);
      return (Bean<?>) proxy;
   }
   
}
