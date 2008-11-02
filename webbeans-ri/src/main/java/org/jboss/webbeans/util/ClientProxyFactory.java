package org.jboss.webbeans.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

public class ClientProxyFactory
{
   private ManagerImpl manager;
   
   public ClientProxyFactory(ManagerImpl manager) {
      this.manager = manager;
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
   
   public <T> T createClientProxy(Bean<T> bean, int beanIndex) throws InstantiationException, IllegalAccessException {
      ProxyFactory proxyFactory = new ProxyFactory();
      TypeInfo typeInfo = getTypeInfo(bean.getTypes());
      proxyFactory.setInterfaces(typeInfo.interfaces);
      proxyFactory.setSuperclass(typeInfo.superclass);
      T clientProxy = (T) proxyFactory.createClass().newInstance();
      ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(bean, beanIndex, manager);
      ((ProxyObject)clientProxy).setHandler(proxyMethodHandler);
      return clientProxy;
   }
        
}
