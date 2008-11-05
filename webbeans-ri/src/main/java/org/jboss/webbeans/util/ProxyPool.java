package org.jboss.webbeans.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.webbeans.UnproxyableDependencyException;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

import com.google.common.collect.ForwardingMap;

public class ProxyPool
{
   private class Pool extends ForwardingMap<Bean<?>, Object>
   {
      
      Map<Bean<?>, Object> delegate;
      
      public Pool()
      {
         delegate = new ConcurrentHashMap<Bean<?>, Object>();
      }

      public <T> T get(Bean<T> key)
      {
         return (T) super.get(key);
      }
      
      @Override
      protected Map<Bean<?>, Object> delegate()
      {
         return delegate;
      }
      
   }
   
   private ManagerImpl manager;
   private Pool pool;
   
   public ProxyPool(ManagerImpl manager) {
      this.manager = manager;
      this.pool = new Pool();
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
   
   private <T> T createClientProxy(Bean<T> bean, int beanIndex) throws InstantiationException, IllegalAccessException {
      ProxyFactory proxyFactory = new ProxyFactory();
      TypeInfo typeInfo = getTypeInfo(bean.getTypes());
      proxyFactory.setInterfaces(typeInfo.interfaces);
      proxyFactory.setSuperclass(typeInfo.superclass);
      T clientProxy = (T) proxyFactory.createClass().newInstance();
      ProxyMethodHandler proxyMethodHandler = new ProxyMethodHandler(bean, beanIndex, manager);
      ((ProxyObject)clientProxy).setHandler(proxyMethodHandler);
      return clientProxy;
   }

   public Object getClientProxy(Bean<?> bean)
   {
      Object clientProxy = pool.get(bean);
      if (clientProxy == null)
      {
         try
         {
            int beanIndex = manager.getBeans().indexOf(bean);
            // Implicit add required since it is looked up on activation with then index
            if (beanIndex < 0) {
               manager.addBean(bean);
               beanIndex = manager.getBeans().size() - 1;
            }
            clientProxy = createClientProxy(bean, beanIndex);
         }
         catch (Exception e)
         {
            // TODO: What to *really* do here?
            throw new UnproxyableDependencyException("Could not create client proxy for " + bean.getName(), e);
         }
         pool.put(bean, clientProxy);
      }
      return clientProxy;
   }
   
   
}
