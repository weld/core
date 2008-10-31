package org.jboss.webbeans.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
   
   public class ProxyMethodHandler implements MethodHandler, Serializable {
      private static final long serialVersionUID = -5391564935097267888L;

      private ManagerImpl manager;
      private Bean<?> bean;
      
      public ProxyMethodHandler(Bean<?> bean, ManagerImpl manager) {
         this.bean = bean;
         this.manager = manager;
      }
      
      public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
      {
         Context context = manager.getContext(bean.getScopeType());
         Object proxiedInstance = context.get(bean, true);
         Method proxiedMethod = proxiedInstance.getClass().getMethod(method.getName(), method.getParameterTypes());
         return proxiedMethod.invoke(proxiedInstance, args);
      }
            
   }
  
   private Class<?>[] addSerializableInterface(Class<?> clazz) {
      // TODO Doesn't compile Class<?>[] interfaces = Arrays.copyOf(clazz.getInterfaces(), clazz.getInterfaces().length + 1);
      Class[] interfaces = new Class[0];
      interfaces[interfaces.length] = Serializable.class;
     return interfaces;
   }
   
   private class TypeInfo {
      Class<?>[] interfaces;
      Class<?> superclass;
   }
   
   private TypeInfo getTypeInfo(Set<Class<?>> types) {
      TypeInfo typeInfo = new TypeInfo();
      List<Class<?>> interfaces = new ArrayList<Class<?>>();
      Class<?> superclass = null;
      for (Class<?> type : types) {
         if (type.isInterface()) {
            interfaces.add(type);
         } else if (superclass == null || (type != Object.class && superclass.isAssignableFrom(type))) {
            superclass = type;
         }
      }
      interfaces.add(Serializable.class);
      typeInfo.interfaces = interfaces.toArray(new Class<?>[0]);
      typeInfo.superclass = superclass;
      return typeInfo;
   }
   
   public <T> T createClientProxy(Bean<T> bean) throws InstantiationException, IllegalAccessException {
      ProxyFactory proxyFactory = new ProxyFactory();
      TypeInfo typeInfo = getTypeInfo(bean.getTypes());
      proxyFactory.setInterfaces(typeInfo.interfaces);
      proxyFactory.setSuperclass(typeInfo.superclass);
      T clientProxy = (T) proxyFactory.createClass().newInstance();
      ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(bean, manager);
      ((ProxyObject)clientProxy).setHandler(proxyMethodHandler);
      return clientProxy;
   }
        
}
