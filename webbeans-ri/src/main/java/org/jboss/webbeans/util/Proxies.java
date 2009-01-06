package org.jboss.webbeans.util;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;

public class Proxies
{
   
   public static class TypeInfo
   {
      
      private static final Class<?>[] EMPTY_INTERFACES_ARRAY = new Class<?>[0];
      
      private final Set<Class<?>> interfaces;
      private final Set<Class<?>> classes;
      
      private TypeInfo()
      {
         super();
         this.interfaces = new HashSet<Class<?>>();
         this.classes = new HashSet<Class<?>>();
      }
      
      public Class<?> getSuperClass()
      {
         if (classes.isEmpty())
         {
            throw new AssertionError("TypeInfo not properly initialized");
         }
         Iterator<Class<?>> it = classes.iterator();
         Class<?> superclass = it.next();
         while (it.hasNext())
         {
            Class<?> clazz = it.next();
            if (superclass.isAssignableFrom(clazz))
            {
               superclass = clazz;
            }
         }
         return superclass;
      }
      
      private Class<?>[] getInterfaces()
      {
         return interfaces.toArray(EMPTY_INTERFACES_ARRAY);
      }
      
      public ProxyFactory createProxyFactory()
      {
         ProxyFactory proxyFactory = new ProxyFactory();
         proxyFactory.setSuperclass(getSuperClass());
         proxyFactory.setInterfaces(getInterfaces());
         return proxyFactory;
      }
      
      private void add(Type type)
      {
         if (type instanceof Class)
         {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isInterface())
            {
               interfaces.add(clazz);
            }
            // TODO Check the class type much more carefully, many things need excluding!
            else
            {
               classes.add(clazz);
            }
         }
         // TODO what about non-Class types
      }
      
      public static TypeInfo ofTypes(Set<Type> types)
      {
         TypeInfo typeInfo = new TypeInfo();
         for (Type type : types)
         {
            typeInfo.add(type);
         }
         return typeInfo;
      }
      
      public static TypeInfo ofClasses(Set<Class<?>> classes)
      {
         TypeInfo typeInfo = new TypeInfo();
         for (Class<?> type : classes)
         {
            typeInfo.add(type);
         }
         return typeInfo;
      }
      
   }
   
   /**
    * Get the proxy factory for the given set of types
    *
    * @param types The types to create the proxy factory for
    * @param classes Additional interfaces the proxy should implement
    * @return the proxy factory
    */
   public static ProxyFactory getProxyFactory(Set<Type> types)
   {
      return TypeInfo.ofTypes(types).createProxyFactory();
   }
   
}
