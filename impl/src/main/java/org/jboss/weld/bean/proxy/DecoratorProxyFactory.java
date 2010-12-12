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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Opcode;
import javassist.util.proxy.MethodHandler;

import javax.enterprise.inject.spi.Bean;

import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.MethodUtils;

/**
 * This special proxy factory is mostly used for abstract decorators. When a
 * delegate field is injected, the abstract methods directly invoke the
 * corresponding method on the delegate. All other cases forward the calls to
 * the {@link BeanInstance} for further processing.
 * 
 * @author David Allen
 * @author Stuart Douglas
 */
public class DecoratorProxyFactory<T> extends ProxyFactory<T>
{
   public static final String PROXY_SUFFIX = "DecoratorProxy";
   private final WeldInjectionPoint<?, ?> delegateInjectionPoint;
   private final Field delegateField;

   public DecoratorProxyFactory(Class<T> proxyType, WeldInjectionPoint<?, ?> delegateInjectionPoint, Bean<?> bean)
   {
      super(proxyType, Collections.<Type>emptySet(), bean);
      this.delegateInjectionPoint = delegateInjectionPoint;
      if (delegateInjectionPoint instanceof FieldInjectionPoint<?, ?>)
      {
         delegateField = ((FieldInjectionPoint<?, ?>) delegateInjectionPoint).getJavaMember();
      }
      else
      {
         delegateField = null;
      }
   }

   private void addHandlerInitializerMethod(ClassFile proxyClassType) throws Exception
   {
      proxyClassType.addMethod(MethodUtils.makeMethod(Modifier.PRIVATE, void.class, "_initMH", new Class[] { Object.class }, new Class[] {}, createMethodHandlerInitializerBody(proxyClassType), proxyClassType.getConstPool()));
   }

   @Override
   protected void addAdditionalInterfaces(Set<Class<?>> interfaces)
   {
      interfaces.add(DecoratorProxy.class);
   }

   /**
    * calls _initMH on the method handler and then stores the result in the
    * methodHandler field as then new methodHandler
    * 
    */
   private Bytecode createMethodHandlerInitializerBody(ClassFile proxyClassType)
   {
      Bytecode b = new Bytecode(proxyClassType.getConstPool(), 1, 2);
      b.add(Opcode.ALOAD_0);
      invokeMethodHandler(proxyClassType, b, proxyClassType.getName(), "_initMH", new String[] { "Ljava/lang/Object;" }, "V", false, DEFAULT_METHOD_RESOLVER);
      b.addCheckcast("javassist/util/proxy/MethodHandler");
      b.addPutfield(proxyClassType.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
      b.add(Opcode.RETURN);
      log.trace("Created MH initializer body for decorator proxy:  " + getBeanType());
      return b;
   }

   @Override
   protected void addMethodsFromClass(ClassFile proxyClassType)
   {
      Method initializerMethod = null;
      int delegateParameterPosition = -1;
      if (delegateInjectionPoint instanceof ParameterInjectionPoint<?, ?>)
      {
         ParameterInjectionPoint<?, ?> parameterIP = (ParameterInjectionPoint<?, ?>) delegateInjectionPoint;
         if (parameterIP.getMember() instanceof Method)
         {
            initializerMethod = ((Method) parameterIP.getMember());
            delegateParameterPosition = parameterIP.getPosition();
         }
      }
      try
      {
         if (delegateParameterPosition >= 0)
         {
            addHandlerInitializerMethod(proxyClassType);
         }
         Class<?> cls = getBeanType();
         while (cls != null)
         {
            for (Method method : cls.getDeclaredMethods())
            {
               if (!method.getDeclaringClass().getName().equals("java.lang.Object") || method.getName().equals("toString"))
               {
                  Bytecode methodBody = null;
                  if ((delegateParameterPosition >= 0) && (initializerMethod.equals(method)))
                  {
                     methodBody = createDelegateInitializerCode(proxyClassType, method, delegateParameterPosition);
                  }
                  if (Modifier.isAbstract(method.getModifiers()))
                  {
                     methodBody = createAbstractMethodCode(proxyClassType, method);
                  }

                  if (methodBody != null)
                  {
                     log.trace("Adding method " + method);
                     proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), methodBody, proxyClassType.getConstPool()));
                  }
               }
            }
            cls = cls.getSuperclass();
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

   private Bytecode createAbstractMethodCode(ClassFile file, Method method) throws NotFoundException
   {
      if ((delegateField != null) && (!Modifier.isPrivate(delegateField.getModifiers())))
      {
         // Call the corresponding method directly on the delegate
         Bytecode b = new Bytecode(file.getConstPool());
         int localVariables = MethodUtils.calculateMaxLocals(method);
         b.setMaxLocals(localVariables);
         // load the delegate field
         b.addAload(0);
         b.addGetfield(file.getName(), delegateField.getName(), DescriptorUtils.classToStringRepresentation(delegateField.getType()));
         String methodDescriptor = DescriptorUtils.getMethodDescriptor(method);
         // load the parameters
         BytecodeUtils.loadParameters(b, methodDescriptor);
         // invoke the delegate method
         b.addInvokeinterface(delegateField.getType().getName(), method.getName(), methodDescriptor, localVariables);
         // return the value if applicable
         BytecodeUtils.addReturnInstruction(b, method.getReturnType());
         return b;
      }
      else
      {
         if (!Modifier.isPrivate(method.getModifiers()))
         {
            // if it is a parameter injection point we need to initalize the
            // injection point then handle the method with the method handler
            return createAbstractMethodHandler(file, method);
         }
         else
         {
            // if the delegate is private we need to use the method handler
            return createInterceptorBody(file, method);
         }
      }
   }

   private Bytecode createAbstractMethodHandler(ClassFile file, Method method)
   {
      // this is slightly different to a normal method handler call, as we pass
      // in a TargetInstanceBytecodeMethodResolver. This resolver uses the
      // method handler to call getTargetClass to get the correct class type to
      // resolve the method with, and then resolves this method
      Bytecode b = new Bytecode(file.getConstPool());
      String[] ptypes = new String[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; ++i)
      {
         ptypes[i] = DescriptorUtils.classToStringRepresentation(method.getParameterTypes()[i]);
      }
      invokeMethodHandler(file, b, method.getDeclaringClass().getName(), method.getName(), ptypes, DescriptorUtils.classToStringRepresentation(method.getReturnType()), true, new TargetInstanceBytecodeMethodResolver());

      return b;

   }

   /**
    * When creates the delegate initializer code when the delegate is injected
    * into a method.
    * 
    * super initializer method is called first, and then _initMH is called
    * 
    * @param file
    * @param initializerName
    * @param delegateParameterPosition
    * @return
    */
   private Bytecode createDelegateInitializerCode(ClassFile file, Method intializerMethod, int delegateParameterPosition)
   {
      Bytecode b = new Bytecode(file.getConstPool());
      // we need to push all the pareters on the stack to call the corresponding
      // superclass arguments
      b.addAload(0); // load this
      int localVariables = 1;
      int actualDelegateParamterPosition = 0;
      String methodDescriptor = DescriptorUtils.getMethodDescriptor(intializerMethod);
      for (int i = 0; i < intializerMethod.getParameterTypes().length; ++i)
      {
         if (i == delegateParameterPosition)
         {
            // figure out the actual position of the delegate in the local
            // variables
            actualDelegateParamterPosition = localVariables;
         }
         Class<?> type = intializerMethod.getParameterTypes()[i];
         BytecodeUtils.addLoadInstruction(b, DescriptorUtils.classToStringRepresentation(type), localVariables);
         if (type == long.class || type == double.class)
         {
            localVariables = localVariables + 2;
         }
         else
         {
            localVariables++;
         }
      }
      b.addInvokespecial(file.getSuperclass(), intializerMethod.getName(), methodDescriptor);
      // if this method returns a value it is now sitting on top of the stack
      // we will leave it there are return it later

      // now we need to call _initMH
      b.addAload(0); // load this
      b.addAload(actualDelegateParamterPosition); // load the delegate
      b.addInvokevirtual(file.getName(), "_initMH", "(Ljava/lang/Object;)V");
      // return the object from the top of the stack that we got from calling
      // the superclass method earlier
      BytecodeUtils.addReturnInstruction(b, intializerMethod.getReturnType());
      b.setMaxLocals(localVariables);
      return b;

   }

   protected static class TargetInstanceBytecodeMethodResolver implements BytecodeMethodResolver
   {
      public void getDeclaredMethod(ClassFile file, Bytecode code, String declaringClass, String methodName, String[] parameterTypes)
      {
         // get the correct class type to use to resolve the method
         invokeMethodHandler(file, code, TargetInstanceProxy.class.getName(), "getTargetClass", parameterTypes, "Ljava/lang/Class;", false, DEFAULT_METHOD_RESOLVER);
         code.addCheckcast("java/lang/Class");
         // now we have the class on the stack
         code.addLdc(methodName);
         // now we need to load the parameter types into an array
         code.addIconst(parameterTypes.length);
         code.addAnewarray("java.lang.Class");
         for (int i = 0; i < parameterTypes.length; ++i)
         {
            code.add(Opcode.DUP); // duplicate the array reference
            code.addIconst(i);
            // now load the class object
            String type = parameterTypes[i];
            BytecodeUtils.pushClassType(code, type);
            // and store it in the array
            code.add(Opcode.AASTORE);
         }
         code.addInvokevirtual("java.lang.Class", "getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
      }
   }

}
