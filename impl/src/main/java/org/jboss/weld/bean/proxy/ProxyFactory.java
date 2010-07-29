/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.jboss.weld.bean.proxy;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.util.proxy.ProxyObject;

import org.jboss.interceptor.proxy.LifecycleMixin;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.util.ClassloaderClassPath;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;
import org.jboss.weld.util.reflection.instantiation.InstantiatorFactory;
import org.slf4j.cal10n.LocLogger;

/**
 * Main factory to produce proxy classes and instances for Weld beans. This
 * implementation creates proxies which forward non-static method invocations to
 * a {@link BeanInstance}. All proxies implement the {@link Proxy} interface.
 * 
 * @author David Allen
 */
public class ProxyFactory<T>
{
   // The log provider
   protected static final LocLogger log                  = loggerFactory().getLogger(BEAN);
   // Default proxy class name suffix
   public static final String       PROXY_SUFFIX         = "Proxy";

   private final Class<?>           beanType;
   private final Set<Class<?>>      additionalInterfaces = new HashSet<Class<?>>();
   private final ClassLoader        classLoader;
   private final ProtectionDomain   protectionDomain;
   private final ClassPool          classPool;
   private final String             baseProxyName;

   /**
    * Creates a new proxy factory with only the type of proxy specified.
    * 
    * @param proxiedBeanType the super-class for this proxy class
    */
   public ProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> businessInterfaces)
   {
      for (Type type : businessInterfaces)
      {
         Class<?> c = Reflections.getRawType(type);
         // Ignore no-interface views, they are dealt with proxiedBeanType
         // (pending redesign)
         if (c.isInterface())
         {
            addInterface(c);
         }
      }
      TypeInfo typeInfo = TypeInfo.of(businessInterfaces);
      Class<?> superClass = typeInfo.getSuperClass();
      superClass = superClass == null ? Object.class : superClass;
      if (superClass.equals(Object.class))
      {
         if (additionalInterfaces.isEmpty())
         {
            // No interface beans must use the bean impl as superclass
            superClass = proxiedBeanType;
         }
         this.classLoader = Container.instance().services().get(ProxyServices.class).getClassLoader(proxiedBeanType);         
      }
      else
      {
         this.classLoader = Container.instance().services().get(ProxyServices.class).getClassLoader(superClass);
      }
      this.beanType = superClass;
      this.protectionDomain = Container.instance().services().get(ProxyServices.class).getProtectionDomain(beanType);
      this.classPool = new ClassPool();
      this.classPool.appendClassPath(new ClassloaderClassPath(classLoader));
      addDefaultAdditionalInterfaces();
      if (proxiedBeanType.equals(Object.class))
      {
         Class<?> superInterface = typeInfo.getSuperInterface();
         if (superInterface == null)
         {
            throw new IllegalArgumentException("Proxied bean type cannot be java.lang.Object without an interface");
         }
         else
         {
            baseProxyName = superInterface.getName();
         }
      }
      else
      {
         baseProxyName = proxiedBeanType.getName();
      }
   }

   /**
    * Adds an additional interface that the proxy should implement. The default
    * implementation will be to forward invocations to the bean instance.
    * 
    * @param newInterface an interface
    */
   public void addInterface(Class<?> newInterface)
   {
      if (!newInterface.isInterface())
      {
         throw new IllegalArgumentException(newInterface + " is not an interface");
      }
      additionalInterfaces.add(newInterface);
   }

   /**
    * Method to create a new proxy that wraps the bean instance.
    * 
    * @return a new proxy object
    */
   public T create(BeanInstance beanInstance)
   {
      T proxy = null;
      Class<T> proxyClass = getProxyClass();
      try
      {
         if (InstantiatorFactory.useInstantiators())
         {
            proxy = SecureReflections.newUnsafeInstance(proxyClass);
         }
         else
         {
            proxy = SecureReflections.newInstance(proxyClass);
         }
      }
      catch (InstantiationException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
      ((ProxyObject) proxy).setHandler(new ProxyMethodHandler(beanInstance));
      return proxy;
   }

   /**
    * Produces or returns the existing proxy class.
    * 
    * @return always the class of the proxy
    */
   @SuppressWarnings("unchecked")
   public Class<T> getProxyClass()
   {
      String proxyClassName = getBaseProxyName() + "_$$_Weld" + getProxyNameSuffix();
      if (proxyClassName.startsWith("java"))
      {
         proxyClassName = proxyClassName.replaceFirst("java", "org.jboss.weld");
      }
      Class<T> proxyClass = null;
      log.trace("Retrieving/generating proxy class " + proxyClassName);
      try
      {
         // First check to see if we already have this proxy class
         proxyClass = (Class<T>) classLoader.loadClass(proxyClassName);
      }
      catch (ClassNotFoundException e)
      {
         // Create the proxy class for this instance
         try
         {
            proxyClass = createProxyClass(proxyClassName);
         }
         catch (Exception e1)
         {
            throw new WeldException(e1);
         }
      }
      return proxyClass;
   }

   /**
    * Returns the package and base name for the proxy class.
    * 
    * @return base name without suffixes
    */
   protected String getBaseProxyName()
   {
      return baseProxyName;
   }

   /**
    * Convenience method to determine if an object is a proxy generated by this
    * factory or any derived factory.
    * 
    * @param proxySuspect the object suspected of being a proxy
    * @return true only if it is a proxy object
    */
   public static boolean isProxy(Object proxySuspect)
   {
      return proxySuspect instanceof ProxyObject;
   }

   /**
    * Convenience method to set the underlying bean instance for a proxy.
    * 
    * @param proxy the proxy instance
    * @param beanInstance the instance of the bean
    */
   public static <T> void setBeanInstance(T proxy, BeanInstance beanInstance)
   {
      if (proxy instanceof ProxyObject)
      {
         ProxyObject proxyView = (ProxyObject) proxy;
         proxyView.setHandler(new ProxyMethodHandler(beanInstance));
      }
   }

   /**
    * Returns a suffix to append to the name of the proxy class. The name
    * already consists of <class-name>_$$_Weld, to which the suffix is added.
    * This allows the creation of different types of proxies for the same class.
    * 
    * @return a name suffix
    */
   protected String getProxyNameSuffix()
   {
      return PROXY_SUFFIX;
   }

   private void addDefaultAdditionalInterfaces()
   {
      additionalInterfaces.add(Serializable.class);
   }

   @SuppressWarnings("unchecked")
   private Class<T> createProxyClass(String proxyClassName) throws Exception
   {
      ArraySet<Class<?>> specialInterfaces = new ArraySet<Class<?>>(3);
      specialInterfaces.add(LifecycleMixin.class);
      specialInterfaces.add(TargetInstanceProxy.class);
      specialInterfaces.add(ProxyObject.class);
      // Remove special interfaces from main set (deserialization scenario)
      additionalInterfaces.removeAll(specialInterfaces);

      CtClass instanceType = classPool.get(beanType.getName());
      CtClass proxyClassType = null;
      if (instanceType.isInterface())
      {
         proxyClassType = classPool.makeClass(proxyClassName);
         proxyClassType.addInterface(instanceType);
      }
      else
      {
         proxyClassType = classPool.makeClass(proxyClassName, instanceType);
      }

      // Add interfaces which require method generation
      for (Class<?> clazz : additionalInterfaces)
      {
         proxyClassType.addInterface(classPool.get(clazz.getName()));
      }
      addFields(proxyClassType);
      addConstructors(proxyClassType);
      addMethods(proxyClassType);

      // Additional interfaces whose methods require special handling
      for (Class<?> specialInterface : specialInterfaces)
      {
         proxyClassType.addInterface(classPool.get(specialInterface.getName()));
      }

      Class<T> proxyClass = proxyClassType.toClass(classLoader, protectionDomain);
      proxyClassType.detach();
      log.trace("Created Proxy class of type " + proxyClass + " supporting interfaces " + Arrays.toString(proxyClass.getInterfaces()));
      return proxyClass;
   }

   /**
    * Adds a constructor for the proxy for each constructor declared by the base
    * bean type.
    * 
    * @param proxyClassType the Javassist class for the proxy
    */
   protected void addConstructors(CtClass proxyClassType)
   {
      try
      {
         CtClass baseType = classPool.get(beanType.getName());
         if (baseType.isInterface())
         {
            proxyClassType.addConstructor(CtNewConstructor.defaultConstructor(proxyClassType));
         }
         else
         {
            for (CtConstructor constructor : baseType.getConstructors())
            {
               proxyClassType.addConstructor(CtNewConstructor.make(constructor.getParameterTypes(), constructor.getExceptionTypes(), proxyClassType));
            }
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   protected void addFields(CtClass proxyClassType)
   {
      try
      {
         // The field representing the underlying instance or special method
         // handling
         proxyClassType.addField(new CtField(classPool.get("javassist.util.proxy.MethodHandler"), "methodHandler", proxyClassType));
         // Special field used during serialization of a proxy
         proxyClassType.addField(new CtField(CtClass.booleanType, "firstSerializationPhaseComplete", proxyClassType), "false");
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   private void addMethods(CtClass proxyClassType)
   {
      // Add all class methods for interception
      addMethodsFromClass(proxyClassType);

      // Add special proxy methods
      addSpecialMethods(proxyClassType);

      // Add serialization support methods
      addSerializationSupport(proxyClassType);
   }

   /**
    * Adds special serialization code by providing a writeReplace() method on
    * the proxy. This method when first called will substitute the proxy object
    * with an instance of {@link org.jboss.weld.proxy.util.SerializableProxy}.
    * The next call will receive the proxy object itself permitting the
    * substitute object to serialize the proxy.
    * 
    * @param proxyClassType the Javassist class for the proxy class
    */
   protected void addSerializationSupport(CtClass proxyClassType)
   {
      try
      {
         // Create a two phase writeReplace where the first call uses a
         // replacement object and the subsequent call get the proxy object.
         CtClass exception = classPool.get(ObjectStreamException.class.getName());
         CtClass objectClass = classPool.get(Object.class.getName());
         String writeReplaceBody = createWriteReplaceBody(proxyClassType);
         proxyClassType.addMethod(CtNewMethod.make(objectClass, "writeReplace", null, new CtClass[] { exception }, writeReplaceBody, proxyClassType));

         // Also add a static method that can be used to deserialize a proxy
         // object.
         // This causes the OO input stream to use the class loader from this
         // class.
         CtClass objectInputStreamClass = classPool.get(ObjectInputStream.class.getName());
         CtClass cnfe = classPool.get(ClassNotFoundException.class.getName());
         CtClass ioe = classPool.get(IOException.class.getName());
         String deserializeProxyBody = "{ return $1.readObject(); }";
         proxyClassType.addMethod(CtNewMethod.make(Modifier.STATIC | Modifier.PUBLIC, objectClass, "deserializeProxy", new CtClass[] { objectInputStreamClass }, new CtClass[] { cnfe, ioe }, deserializeProxyBody, proxyClassType));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }

   }

   private String createWriteReplaceBody(CtClass proxyClassType)
   {
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{\n");
      bodyString.append(" if (firstSerializationPhaseComplete) {\n");
      bodyString.append("    firstSerializationPhaseComplete = false;\n");
      bodyString.append("    return $0;\n");
      bodyString.append(" } else {\n");
      bodyString.append("    firstSerializationPhaseComplete = true;\n");
      bodyString.append("    return methodHandler.invoke($0,");
      bodyString.append(proxyClassType.getName());
      bodyString.append(".class.getMethod(\"writeReplace\", null), null, $args);\n");
      bodyString.append(" }\n}");
      return bodyString.toString();
   }

   protected void addMethodsFromClass(CtClass proxyClassType)
   {
      try
      {
         for (CtMethod method : proxyClassType.getMethods())
         {
            if (!Modifier.isStatic(method.getModifiers()) && !method.getDeclaringClass().getName().equals("java.lang.Object") || method.getName().equals("toString"))
            {
               log.trace("Adding method " + method.getLongName());
               proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createInterceptorBody(method), proxyClassType));
            }
         }
         // Also add all private methods from the class hierarchy
         CtClass superClass = proxyClassType.getSuperclass();
         while (!superClass.getName().equals("java.lang.Object"))
         {
            for (CtMethod method : superClass.getDeclaredMethods())
            {
               if (Modifier.isPrivate(method.getModifiers()) && !method.getDeclaringClass().getName().equals("java.lang.Object"))
               {
                  log.trace("Adding method " + method.getLongName());
                  proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createInterceptorBody(method), proxyClassType));
               }
            }
            superClass = superClass.getSuperclass();
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   /**
    * Creates the given method on the proxy class where the implementation
    * forwards the call directly to the method handler.
    * 
    * @param method any Javassist method
    * @return a string containing the method body code to be compiled
    * @throws NotFoundException if any of the parameter types are not found
    */
   protected String createInterceptorBody(CtMethod method) throws NotFoundException
   {
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{ ");
      try
      {
         if (method.getReturnType() != null)
         {
            bodyString.append("return ($r)");
         }
      }
      catch (NotFoundException e)
      {
         // Assume this is a void method
      }

      bodyString.append("methodHandler.invoke($0, ");
      bodyString.append(method.getDeclaringClass().getName());
      if (Modifier.isPublic(method.getModifiers()))
      {
         bodyString.append(".class.getMethod(\"");
         log.trace("Using getMethod in proxy for method " + method.getLongName());
      }
      else
      {
         bodyString.append(".class.getDeclaredMethod(\"");
         log.trace("Using getDeclaredMethod in proxy for method " + method.getLongName());
      }
      bodyString.append(method.getName());
      bodyString.append("\", ");
      bodyString.append(getSignatureClasses(method));
      bodyString.append("), null, $args); }");

      return bodyString.toString();
   }

   /**
    * Produces the code for the list of argument types for the given method.
    * 
    * @param method the method for which to produce the parameter list
    * @return a string of comma-delimited class objects
    * @throws NotFoundException if any of the parameter types are not found by
    *            Javassist
    */
   protected String getSignatureClasses(CtMethod method) throws NotFoundException
   {
      if (method.getParameterTypes().length > 0)
      {
         StringBuilder signatureBuffer = new StringBuilder();
         signatureBuffer.append("new Class[]{");
         boolean firstClass = true;
         for (CtClass clazz : method.getParameterTypes())
         {
            if (firstClass)
            {
               firstClass = false;
            }
            else
            {
               signatureBuffer.append(", ");
            }
            signatureBuffer.append(clazz.getName());
            signatureBuffer.append(".class");
         }
         signatureBuffer.append('}');
         return signatureBuffer.toString();
      }
      else
      {
         return "null";
      }
   }

   /**
    * Adds methods requiring special implementations rather than just
    * delegation.
    * 
    * @param proxyClassType the Javassist class description for the proxy type
    */
   protected void addSpecialMethods(CtClass proxyClassType)
   {
      try
      {
         // Add special methods for interceptors
         CtClass lifecycleMixinClass = classPool.get(LifecycleMixin.class.getName());
         for (CtMethod method : lifecycleMixinClass.getDeclaredMethods())
         {
            log.trace("Adding method " + method.getLongName());
            proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createSpecialInterfaceBody(method, LifecycleMixin.class), proxyClassType));
         }
         CtClass targetInstanceProxyClass = classPool.get(TargetInstanceProxy.class.getName());
         CtMethod getInstanceMethod = targetInstanceProxyClass.getDeclaredMethod("getTargetInstance");
         CtMethod getInstanceClassMethod = targetInstanceProxyClass.getDeclaredMethod("getTargetClass");
         proxyClassType.addMethod(CtNewMethod.make(getInstanceMethod.getReturnType(), getInstanceMethod.getName(), getInstanceMethod.getParameterTypes(), getInstanceMethod.getExceptionTypes(), createSpecialInterfaceBody(getInstanceMethod, TargetInstanceProxy.class), proxyClassType));
         proxyClassType.addMethod(CtNewMethod.make(getInstanceClassMethod.getReturnType(), getInstanceClassMethod.getName(), getInstanceClassMethod.getParameterTypes(), getInstanceClassMethod.getExceptionTypes(), createSpecialInterfaceBody(getInstanceClassMethod, TargetInstanceProxy.class), proxyClassType));
         CtClass proxyObjectClass = classPool.get(ProxyObject.class.getName());
         CtMethod setMethodHandlerMethod = proxyObjectClass.getDeclaredMethod("setHandler");
         proxyClassType.addMethod(CtNewMethod.make(setMethodHandlerMethod.getReturnType(), setMethodHandlerMethod.getName(), setMethodHandlerMethod.getParameterTypes(), setMethodHandlerMethod.getExceptionTypes(), "{ methodHandler = $1; }", proxyClassType));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   /**
    * Creates the method body code for methods which forward the calls directly
    * to the bean instance. These methods are not considered to be implemented
    * by any superclass of the proxy.
    * 
    * @param method a method
    * @return code for the body of the method to be compiled
    * @throws NotFoundException if any of the parameter types are not found
    */
   protected String createSpecialInterfaceBody(CtMethod method, Class<?> interfaceClazz) throws NotFoundException
   {
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{\n");
      try
      {
         if (method.getReturnType() != null)
         {
            bodyString.append("return ($r)");
         }
      }
      catch (NotFoundException e)
      {
         // Assume this is a void method
      }

      bodyString.append("methodHandler.invoke($0, ");
      bodyString.append(interfaceClazz.getName());
      bodyString.append(".class.getDeclaredMethod(\"");
      bodyString.append(method.getName());
      bodyString.append("\", ");
      bodyString.append(getSignatureClasses(method));
      bodyString.append("), null, $args); }");

      return bodyString.toString();
   }

   public ClassPool getClassPool()
   {
      return classPool;
   }

   public Class<?> getBeanType()
   {
      return beanType;
   }

}
