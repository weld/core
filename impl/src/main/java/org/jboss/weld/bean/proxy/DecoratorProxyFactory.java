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

import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import javax.decorator.Delegate;

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
   private final WeldInjectionPoint<?, ?> delegateInjectionPoint;
   private final CtClass                  delegateClass;
   private final Field                    delegateField;

   public DecoratorProxyFactory(Class<T> proxyType, WeldInjectionPoint<?, ?> delegateInjectionPoint)
   {
      super(proxyType);
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

   @Override
   protected void addConstructors(CtClass proxyClassType)
   {
      try
      {
         CtClass baseType = getClassPool().get(getBeanType().getName());
         for (CtConstructor constructor : baseType.getConstructors())
         {
            int delegateInjectionPosition = getDelegateInjectionPosition(constructor);
            if (delegateInjectionPosition >= 0)
            {
               proxyClassType.addConstructor(CtNewConstructor.make(constructor.getParameterTypes(), constructor.getExceptionTypes(), createDelegateInitializerCode(null, delegateInjectionPosition), proxyClassType));
            }
            else
            {
               proxyClassType.addConstructor(CtNewConstructor.copy(constructor, proxyClassType, null));
            }
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
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
         for (CtMethod method : proxyClassType.getMethods())
         {
            if (!method.getDeclaringClass().getName().equals("java.lang.Object") || method.getName().equals("toString"))
            {
               log.trace("Adding method " + method.getLongName());
               String methodBody = null;
               if ((delegateParameterPosition >= 0) && (initializerMethod.equals(method.getName())))
               {
                  methodBody = createDelegateInitializerCode(initializerMethod, delegateParameterPosition);
               }
               else if (Modifier.isAbstract(method.getModifiers()))
               {
                  methodBody = createAbstractMethodCode(method);
               }

               if (methodBody != null)
               {
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
         // Use the associated bean instance to invoke the method
         bodyString.append("beanInstance.invoke(");
         if (Modifier.isPublic(delegateMethod.getModifiers()))
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
      }

      return bodyString.toString();
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
      buffer.append("beanInstance = new ");
      buffer.append(TargetBeanInstance.class.getName());
      buffer.append("($");
      buffer.append(delegateParameterPosition + 1);
      buffer.append("); }");
      return buffer.toString();
   }

   private int getDelegateInjectionPosition(CtConstructor constructor)
   {
      int position = -1;
      Object[][] parameterAnnotations = constructor.getAvailableParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++)
      {
         for (int j = 0; j < parameterAnnotations[i].length; j++)
         {
            if (parameterAnnotations[i][j] instanceof Delegate)
            {
               position = i;
               break;
            }
         }
      }
      return position;
   }

}
