package org.jboss.webbeans.util;

import java.lang.reflect.Method;
import java.util.Set;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.test.beans.Tuna;
import org.jboss.webbeans.test.util.Util;

public class ClientProxyFactory
{
   private ManagerImpl manager;
   
   public ClientProxyFactory(ManagerImpl manager) {
      this.manager = manager;
   }
   
   public class ProxyMethodHandler implements MethodHandler {
      private ManagerImpl manager;
      private Bean<?> bean;
      
      public ProxyMethodHandler(Bean<?> bean, ManagerImpl manager) {
         this.bean = bean;
         this.manager = manager;
      }
      
      @Override
      public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
      {
         Context context = manager.getContext(bean.getScopeType());
         Object proxiedInstance = context.get(bean, true);
         Method proxiedMethod = proxiedInstance.getClass().getMethod(method.getName(), method.getParameterTypes());
         return proxiedMethod.invoke(proxiedInstance, args);
      }
            
   }
   
   public <T> T createClientProxy(Bean<T> bean) throws InstantiationException, IllegalAccessException {
      ProxyFactory proxyFactory = new ProxyFactory();
      // TODO How to get the type T from a bean?
      Class<?>[] beanTypes = bean.getTypes().toArray(new Class<?>[0]);
      proxyFactory.setSuperclass(beanTypes[0]);
      T clientProxy = (T) proxyFactory.createClass().newInstance();
      ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(bean, manager);
      ((ProxyObject)clientProxy).setHandler(proxyMethodHandler);
      return clientProxy;
   }
   
   private void run() throws InstantiationException, IllegalAccessException {
      Bean<Tuna> tunaBean = Util.createSimpleWebBean(Tuna.class, manager);
      Tuna proxy = createClientProxy(tunaBean);
   }
   
   public static void main(String[] params) throws InstantiationException, IllegalAccessException {
      ClientProxyFactory f = new ClientProxyFactory(new ManagerImpl());
      f.run();
   }   
      
}
