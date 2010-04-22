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

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.jboss.interceptor.proxy.LifecycleMixin;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.bean.proxy.util.ClassloaderClassPath;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
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
   protected static final LocLogger    log                  = loggerFactory().getLogger(BEAN);
   // Default proxy class name suffix
   private static final String         PROXY_SUFFIX         = "Proxy";

   protected final Class<?>            beanType;
   protected final ArrayList<Class<?>> additionalInterfaces = new ArrayList<Class<?>>();
   protected final ClassLoader               classLoader;
   protected final ProtectionDomain          protectionDomain;
   protected final ClassPool                 classPool;

   /**
    * Creates a new proxy factory from any type of BeanInstance. This bean
    * instance is only used for initialization information and is not associated
    * with this factory once created.
    * 
    * @param instance a bean instance that will be used with the proxy
    */
   public ProxyFactory(BeanInstance beanInstance)
   {
      this(beanInstance.getInstanceType());
   }

   /**
    * Creates a new proxy factory with only the type of proxy specified.
    * 
    * @param proxiedBeanType the super-class for this proxy class
    */
   public ProxyFactory(Class<?> proxiedBeanType)
   {
      this.beanType = proxiedBeanType;
      if (beanType.getName().startsWith("java"))
      {
         this.classLoader = this.getClass().getClassLoader();
         this.protectionDomain = this.getClass().getProtectionDomain();
      }
      else
      {
         this.classLoader = beanType.getClassLoader();
         this.protectionDomain = beanType.getProtectionDomain();
      }
      this.classPool = new ClassPool();
      this.classPool.appendClassPath(new ClassloaderClassPath(classLoader));
      addDefaultAdditionalInterfaces();
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
         proxy = proxyClass.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
      ((Proxy) proxy).proxy_setInstance(beanInstance);
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
      String proxyClassName = beanType.getName() + "_$$_Weld" + getProxyNameSuffix();
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
    * Convenience method to set the underlying bean instance for a proxy.
    * 
    * @param proxy the proxy instance
    * @param beanInstance the instance of the bean
    */
   public static <T> void setBeanInstance(T proxy, BeanInstance beanInstance)
   {
      if (proxy instanceof Proxy)
      {
         Proxy proxyView = (Proxy) proxy;
         proxyView.proxy_setInstance(beanInstance);
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
      proxyClassType.addInterface(classPool.get(Proxy.class.getName()));
      proxyClassType.addInterface(classPool.get(LifecycleMixin.class.getName()));
      proxyClassType.addInterface(classPool.get(TargetInstanceProxy.class.getName()));

      Class<T> proxyClass = proxyClassType.toClass(classLoader, protectionDomain);
      proxyClassType.detach();
      log.trace("Created Proxy class of type " + proxyClass + " supporting interfaces " + Arrays.toString(proxyClass.getInterfaces()));
      return proxyClass;
   }

   /**
    * Adds a public default constructor to the proxy class for ease of
    * instantiating it.
    * 
    * @param proxyClassType the Javassist class for the proxy
    */
   protected void addConstructors(CtClass proxyClassType)
   {
      try
      {
         proxyClassType.addConstructor(CtNewConstructor.defaultConstructor(proxyClassType));
      }
      catch (CannotCompileException e)
      {
         throw new WeldException(e);
      }
   }

   private void addFields(CtClass proxyClassType)
   {
      // The field for the instance locator
      try
      {
         proxyClassType.addField(new CtField(classPool.get("org.jboss.weld.bean.proxy.BeanInstance"), "beanInstance", proxyClassType));
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
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   /**
    * Creates the given method on the proxy class where the implementation
    * forwards the call directly to the bean instance.
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

      bodyString.append("beanInstance.invoke(");
      if (Modifier.isPublic(method.getModifiers()))
      {
         bodyString.append("beanInstance.getInstanceType().getMethod(\"");
         log.trace("Using getMethod in proxy for method " + method.getLongName());
      }
      else
      {
         bodyString.append(method.getDeclaringClass().getName());
         bodyString.append(".class.getDeclaredMethod(\"");
         log.trace("Using getDeclaredMethod in proxy for method " + method.getLongName());
      }
      bodyString.append(method.getName());
      bodyString.append("\", ");
      bodyString.append(getSignatureClasses(method));
      bodyString.append("), $args); }");

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

   private void addSpecialMethods(CtClass proxyClassType)
   {
      try
      {
         // Add public getter/setter pair for the instance locator
         proxyClassType.addMethod(CtNewMethod.make("public org.jboss.weld.bean.proxy.BeanInstance proxy_getInstance() { return beanInstance; }", proxyClassType));
         proxyClassType.addMethod(CtNewMethod.make("public void proxy_setInstance(org.jboss.weld.bean.proxy.BeanInstance beanInstance) { this.beanInstance = beanInstance; }", proxyClassType));

         // Add special methods for interceptors
         CtClass lifecycleMixinClass = classPool.get(LifecycleMixin.class.getName());
         for (CtMethod method : lifecycleMixinClass.getDeclaredMethods())
         {
            log.trace("Adding method " + method.getLongName());
            proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createLifecycleBody(method), proxyClassType));
         }
         CtClass targetInstanceProxyClass = classPool.get(TargetInstanceProxy.class.getName());
         CtMethod getInstanceMethod = targetInstanceProxyClass.getDeclaredMethod("getTargetInstance");
         CtMethod getInstanceClassMethod = targetInstanceProxyClass.getDeclaredMethod("getTargetClass");
         proxyClassType.addMethod(CtNewMethod.make(getInstanceMethod.getReturnType(), getInstanceMethod.getName(), getInstanceMethod.getParameterTypes(), getInstanceMethod.getExceptionTypes(), "{ return beanInstance.getInstance(); }", proxyClassType));
         proxyClassType.addMethod(CtNewMethod.make(getInstanceClassMethod.getReturnType(), getInstanceClassMethod.getName(), getInstanceClassMethod.getParameterTypes(), getInstanceClassMethod.getExceptionTypes(), "{ return beanInstance.getInstanceType(); }", proxyClassType));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   /**
    * Creates the method body code for lifecycle methods which forward the calls
    * directly to the bean instance.
    * 
    * @param method a lifecycle method
    * @return code for the body of the method to be compiled
    * @throws NotFoundException if any of the parameter types are not found
    */
   private String createLifecycleBody(CtMethod method) throws NotFoundException
   {
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{ beanInstance.invoke(");
      bodyString.append(LifecycleMixin.class.getName());
      bodyString.append(".class.getDeclaredMethod(\"");
      bodyString.append(method.getName());
      bodyString.append("\", ");
      bodyString.append(getSignatureClasses(method));
      bodyString.append("), $args); }");

      return bodyString.toString();
   }

}
