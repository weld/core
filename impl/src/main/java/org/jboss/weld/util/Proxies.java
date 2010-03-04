/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util;

import static org.jboss.weld.logging.messages.UtilMessage.CANNOT_PROXY_NON_CLASS_TYPE;
import static org.jboss.weld.logging.messages.UtilMessage.INSTANCE_NOT_A_PROXY;
import static org.jboss.weld.util.reflection.Reflections.EMPTY_CLASSES;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.jboss.weld.exceptions.ForbiddenArgumentException;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Utilties for working with Javassist proxies
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public class Proxies
{
   
   private static class IgnoreFinalizeMethodFilter implements MethodFilter
   {

      public boolean isHandled(Method m)
      {
         return m.getName().equals("finalize");
      }
      
   }
   
   
   public static class TypeInfo
   {

      private final Set<Class<?>> interfaces;
      private final Set<Class<?>> classes;

      private TypeInfo()
      {
         super();
         this.interfaces = new LinkedHashSet<Class<?>>();
         this.classes = new LinkedHashSet<Class<?>>();
      }

      public Class<?> getSuperClass()
      {
         if (classes.isEmpty())
         {
            return null;
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
         return interfaces.toArray(EMPTY_CLASSES);
      }

      /**
       * @return
       */
      public ProxyFactory createProxyFactory()
      {
         ProxyFactory proxyFactory = new ProxyFactory();
         ProxyFactory.useCache = false;
         proxyFactory.setFilter(new IgnoreFinalizeMethodFilter());
         Class<?> superClass = getSuperClass();
         if(superClass != null && superClass != Object.class)
         {
            proxyFactory.setSuperclass(superClass);
         }
         proxyFactory.setInterfaces(getInterfaces());
         return proxyFactory;
      }

      public TypeInfo add(Type type)
      {
         if (type instanceof Class<?>)
         {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isInterface())
            {
               interfaces.add(clazz);
            }
            else
            {
               classes.add(clazz);
            }
         }
         else if (type instanceof ParameterizedType)
         {
            add(((ParameterizedType)type).getRawType());
         }
         else
         {
            throw new ForbiddenArgumentException(CANNOT_PROXY_NON_CLASS_TYPE, type);
         }
         return this;
      }

      public static TypeInfo of(Set<? extends Type> types)
      {
         TypeInfo typeInfo = create();
         for (Type type : types)
         {
            typeInfo.add(type);
         }
         return typeInfo;
      }
      
      public static TypeInfo create()
      {
         return new TypeInfo();
      }

   }
   
   private static final String DEFAULT_INTERCEPTOR = "default_interceptor";
   
   /**
    * Create a proxy with a handler, registering the proxy for cleanup
    * 
    * @param <T>
    * @param methodHandler
    * @param typeInfo
    * @return
    * @throws IllegalAccessException
    * @throws InstantiationException
    */
   public static <T> T createProxy(MethodHandler methodHandler, TypeInfo typeInfo) throws IllegalAccessException, InstantiationException
   {
      return SecureReflections.newInstance(Proxies.<T>createProxyClass(methodHandler, typeInfo));
   }
   
   /**
    * Create a proxy class
    * 
    * You will need to manually register the proxy instances for cleanup
    * 
    * @param <T>
    * @param typeInfo
    * @return
    */
   public static <T> Class<T> createProxyClass(TypeInfo typeInfo)
   {
      return createProxyClass(null, typeInfo);
   }
   
   /**
    * Create a proxy class
    * 
    * You will need to manually register the proxy instances for cleanup
    * 
    * @param <T>
    * @param methodHandler
    * @param typeInfo
    * @return
    */
   public static <T> Class<T> createProxyClass(MethodHandler methodHandler, TypeInfo typeInfo)
   {
      ProxyFactory proxyFactory = typeInfo.createProxyFactory();
      attachMethodHandler(proxyFactory, methodHandler);
      
      @SuppressWarnings("unchecked")
      Class<T> clazz = proxyFactory.createClass();
      return clazz;
   }

   /**
    * Indicates if a class is proxyable
    * 
    * @param type The class to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isTypeProxyable(Type type)
   {
      if (type instanceof Class<?>)
      {
         return isClassProxyable((Class<?>) type);
      }
      else if (type instanceof ParameterizedType)
      {
         Type rawType = ((ParameterizedType) type).getRawType();
         if (rawType instanceof Class<?>)
         {
            return isClassProxyable((Class<?>) rawType);
         }
      }
      return false;
   }
   

   /**
    * Indicates if a set of types are all proxyable
    * 
    * @param types The types to test
    * @return True if proxyable, false otherwise
    */
   public static boolean isTypesProxyable(Iterable<? extends Type> types)
   {
      for (Type apiType : types)
      {
         if (Object.class.equals(apiType))
         {
            continue;
         }
         if (!isTypeProxyable(apiType))
         {
            return false;
         }
      }
      return true;
   }
      
   private static boolean isClassProxyable(Class<?> clazz)
   {
      if (clazz.isInterface())
      {
         return true;
      }
      else
      {
         Constructor<?> constructor = null;
         try
         {
            constructor = SecureReflections.getDeclaredConstructor(clazz);
         }
         catch (NoSuchMethodException e)
         {
            return false;
         }
         if (constructor == null)
         {
            return false;
         }
         else if (Modifier.isPrivate(constructor.getModifiers()))
         {
            return false;
         }
         else if (Reflections.isTypeOrAnyMethodFinal(clazz))
         {
            return false;
         }
         else if (clazz.isPrimitive())
         {
            return false;
         }
         else if (Reflections.isArrayType(clazz))
         {
            return false;
         }
         else
         {
            return true;
         }
      }
   }
   
   public static ProxyFactory attachMethodHandler(ProxyFactory proxyFactory, MethodHandler methodHandler)
   {
      if (methodHandler != null)
      {
         proxyFactory.setHandler(new CleanableMethodHandler(methodHandler));
      }
      return proxyFactory;
   }
   
   public static <T> T attachMethodHandler(T instance, MethodHandler methodHandler)
   {
      if (instance instanceof ProxyObject)
      {
         if (methodHandler != null)
         {
            ((ProxyObject) instance).setHandler(new CleanableMethodHandler(methodHandler));
         }
         return instance;
      }
      else
      {
         throw new ForbiddenArgumentException(INSTANCE_NOT_A_PROXY, instance);
      }
      
   }


}
