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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;

/**
 * This special proxy factory is mostly used for abstract decorators. When a
 * delegate field is injected, the abstract methods directly invoke the
 * corresponding method on the delegate. All other cases forward the calls to
 * the {@link BeanInstance} for further processing.
 * 
 * @author David Allen
 */
public class DecoratorProxyFactory<T> extends ProxyFactory<T>
{
   public static final String             PROXY_SUFFIX = "DecoratorProxy";
   private final WeldInjectionPoint<?, ?, ?> delegateInjectionPoint;
   private final CtClass                  delegateClass;
   private final Field                    delegateField;

   public DecoratorProxyFactory(Class<T> proxyType, WeldInjectionPoint<?, ?, ?> delegateInjectionPoint)
   {
      super(proxyType, Collections.EMPTY_SET);
      this.delegateInjectionPoint = delegateInjectionPoint;
      try
      {
         delegateClass = getClassPool().get(((Class<?>) delegateInjectionPoint.getBaseType()).getName());
      }
      catch (NotFoundException e)
      {
         throw new WeldException(e);
      }
      if (delegateInjectionPoint instanceof FieldInjectionPoint<?, ?>)
      {
         delegateField = ((FieldInjectionPoint<?, ?>) delegateInjectionPoint).getJavaMember();
      }
      else
      {
         delegateField = null;
      }
   }

   private void addHandlerInitializerMethod(CtClass proxyClassType) throws Exception
   {
      CtClass objectClass = getClassPool().get(Object.class.getName());
      proxyClassType.addMethod(CtNewMethod.make(Modifier.PRIVATE, CtClass.voidType, "_initMH", new CtClass[] { objectClass }, null, createMethodHandlerInitializerBody(proxyClassType), proxyClassType));
   }

   private String createMethodHandlerInitializerBody(CtClass proxyClassType)
   {
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{ methodHandler = (javassist.util.proxy.MethodHandler) methodHandler.invoke($0, ");
      bodyString.append(proxyClassType.getName());
      bodyString.append(".class.getDeclaredMethod(\"");
      bodyString.append("_initMH");
      bodyString.append("\", new Class[]{Object.class}");
      bodyString.append("), null, $args); }");
      log.trace("Created MH initializer body for proxy:  " + bodyString.toString());
      return bodyString.toString();
   }

   @Override
   protected void addMethodsFromClass(CtClass proxyClassType)
   {
      String initializerMethod = null;
      int delegateParameterPosition = -1;
      if (delegateInjectionPoint instanceof ParameterInjectionPoint<?, ?>)
      {
         ParameterInjectionPoint<?, ?> parameterIP = (ParameterInjectionPoint<?, ?>) delegateInjectionPoint;
         if (parameterIP.getMember() instanceof Method)
         {
            initializerMethod = ((Method) parameterIP.getMember()).getName();
            delegateParameterPosition = parameterIP.getPosition();
         }
      }
      try
      {
         if (delegateParameterPosition >= 0)
         {
            addHandlerInitializerMethod(proxyClassType);
         }
         for (CtMethod method : proxyClassType.getMethods())
         {
            if (!method.getDeclaringClass().getName().equals("java.lang.Object") || method.getName().equals("toString"))
            {
               String methodBody = null;
               if ((delegateParameterPosition >= 0) && (initializerMethod.equals(method.getName())))
               {
                  methodBody = createDelegateInitializerCode(initializerMethod, delegateParameterPosition);
               }
               if (Modifier.isAbstract(method.getModifiers()))
               {
                  methodBody = createAbstractMethodCode(method);
               }

               if (methodBody != null)
               {
                  log.trace("Adding method " + method.getLongName() + " " + methodBody);
                  proxyClassType.addMethod(CtNewMethod.make(method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), methodBody, proxyClassType));
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   @Override
   protected String getProxyNameSuffix()
   {
      return PROXY_SUFFIX;
   }

   private String createAbstractMethodCode(CtMethod method) throws NotFoundException
   {
      CtMethod delegateMethod = null;
      StringBuilder bodyString = new StringBuilder();
      bodyString.append("{ ");
      try
      {
         delegateMethod = delegateClass.getMethod(method.getName(), method.getSignature());
         if (method.getReturnType() != null)
         {
            bodyString.append("return ($r)");
         }
      }
      catch (NotFoundException e)
      {
         throw new WeldException(e);
      }

      if ((delegateField != null) && (!Modifier.isPrivate(delegateField.getModifiers())))
      {
         // Call the corresponding method directly on the delegate
         bodyString.append(delegateField.getName());
         bodyString.append('.');
         bodyString.append(method.getName());
         bodyString.append("($$); }");
         log.trace("Delegating call directly to delegate for method " + method.getLongName());
      }
      else
      {
         // Use the associated method handler to invoke the method
         bodyString.append("methodHandler.invoke($0,");
         if (Modifier.isPublic(delegateMethod.getModifiers()))
         {
            bodyString.append(getTargetClass());
            bodyString.append(".getMethod(\"");
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
         bodyString.append("), null, $args); }");
      }

      return bodyString.toString();
   }

   private String getTargetClass()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("((Class)methodHandler.invoke($0,");
      buffer.append(TargetInstanceProxy.class.getName());
      buffer.append(".class.getMethod(\"getTargetClass\", null), null, null))");
      return buffer.toString();
   }

   private String createDelegateInitializerCode(String initializerName, int delegateParameterPosition)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("{ super");
      if (initializerName != null)
      {
         buffer.append('.');
         buffer.append(initializerName);
      }
      buffer.append("($$);\n");
      buffer.append("_initMH");
      buffer.append("($");
      buffer.append(delegateParameterPosition + 1);
      buffer.append("); }");
      return buffer.toString();
   }

}
