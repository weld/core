/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.interceptor.proxy.LifecycleMixin;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.util.bytecode.Boxing;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.MethodUtils;

/**
 * Factory for producing subclasses that are used by the combined interceptors and decorators stack.
 *
 * @author Marius Bogoevici
 */
public class InterceptedSubclassFactory<T> extends ProxyFactory<T>
{
   // Default proxy class name suffix
   public static final String PROXY_SUFFIX = "Subclass";

   private static final String SUPER_DELEGATE_SUFFIX = "$$super";

   private final Set<MethodSignature> enhancedMethodSignatures;

   public InterceptedSubclassFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean, Set<MethodSignature> enhancedMethodSignatures)
   {
      this(proxiedBeanType, typeClosure, getProxyName(proxiedBeanType, typeClosure, bean), enhancedMethodSignatures);
   }

   /**
    * Creates a new proxy factory when the name of the proxy class is already
    * known, such as during de-serialization
    *
    * @param proxiedBeanType the super-class for this proxy class
    * @param typeClosure the bean types of the bean
    * @param enhancedMethodSignatures a restricted set of methods that need to be intercepted
    *
    */

   public InterceptedSubclassFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName, Set<MethodSignature> enhancedMethodSignatures)
   {
      super(proxiedBeanType, typeClosure, proxyName);
      this.enhancedMethodSignatures = enhancedMethodSignatures;
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

   protected void addMethods(ClassFile proxyClassType)
   {
      // Add all class methods for interception
      addMethodsFromClass(proxyClassType);

      // Add special proxy methods
      addSpecialMethods(proxyClassType);
   }

   /**
    * Customize the overriding of the superclass methods
    */

   @Override
   protected void overrideSuperclassMethod(ClassFile proxyClassType, Method method) throws DuplicateMemberException, NotFoundException
   {
      proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName() + SUPER_DELEGATE_SUFFIX, method.getParameterTypes(), method.getExceptionTypes(), createDelegateToSuper(proxyClassType, method), proxyClassType.getConstPool()));
      log.trace("Adding method " + method);
      proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), addConstructedGuardToMethodBody(proxyClassType, createForwardingMethodBody(proxyClassType, method), method), proxyClassType.getConstPool()));
      log.trace("Adding method " + method + " and a corresponding delegate that invokes super()");
   }

   @Override
   protected MethodInfo generateHashCodeMethod(ClassFile proxyClassType)
   {
      // this method must return null, regardless what happens in the superclass
      return null;
   }

   @Override
   protected MethodInfo generateEqualsMethod(ClassFile proxyClassType)
   {
      // this method must return null, regardless what happens in the superclass
      return null;
   }

   protected Bytecode createForwardingMethodBody(ClassFile proxyClassType, Method method) throws NotFoundException
   {
      return createInterceptorBody(proxyClassType, method, true);
   }

   /**
    * Creates the given method on the proxy class where the implementation
    * forwards the call directly to the method handler.
    *
    * the generated bytecode is equivalent to:
    *
    * return (RetType) methodHandler.invoke(this,param1,param2);
    *
    *
    * @param file the class file
    * @param method any JLR method
    * @param delegateToSuper
    * @return the method byte code
    */
   protected Bytecode createInterceptorBody(ClassFile file, Method method, boolean delegateToSuper) throws NotFoundException
   {
      Bytecode b = new Bytecode(file.getConstPool());
      String[] ptypes = new String[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; ++i)
      {
         ptypes[i] = DescriptorUtils.classToStringRepresentation(method.getParameterTypes()[i]);
      }
      invokeMethodHandler(file, b, method.getDeclaringClass().getName(), method.getName(), ptypes, DescriptorUtils.classToStringRepresentation(method.getReturnType()), true, null, delegateToSuper);
      return b;
   }

   private Bytecode createDelegateToSuper(ClassFile file, Method method) throws NotFoundException
   {
      Bytecode b = new Bytecode(file.getConstPool());
      String methodDescriptor = DescriptorUtils.getMethodDescriptor(method);
      // first generate the invokespecial call to the super class method
      b.add(Opcode.ALOAD_0);
      int localVarCount = BytecodeUtils.loadParameters(b, methodDescriptor);
      b.addInvokespecial(file.getSuperclass(), method.getName(), methodDescriptor);
      b.setMaxLocals(localVarCount);
      BytecodeUtils.addReturnInstruction(b, method.getReturnType());
      return b;
   }

   /**
    * calls methodHandler.invoke for a given method
    *
    * @param file the current class file
    * @param b the bytecode to add the methodHandler.invoke call to
    * @param declaringClass declaring class of the method
    * @param methodName the name of the method to invoke
    * @param methodParameters method paramters in internal JVM format
    * @param returnType return type in internal format
    * @param addReturnInstruction set to true you want to return the result of
    * @param addProceed
    */
   protected static void invokeMethodHandler(ClassFile file, Bytecode b, String declaringClass, String methodName, String[] methodParameters, String returnType, boolean addReturnInstruction, BytecodeMethodResolver bytecodeMethodResolver, boolean addProceed)
   {
      // now we need to build the bytecode. The order we do this in is as
      // follows:
      // load methodHandler
      // load this
      // load the method object
      // load null
      // create a new array the same size as the number of parameters
      // push our parameter values into the array
      // invokeinterface the invoke method
      // add checkcast to cast the result to the return type, or unbox if
      // primitive
      // add an appropriate return instruction
      b.add(Opcode.ALOAD_0);
      b.addGetfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
      b.add(Opcode.ALOAD_0);
      if (bytecodeMethodResolver == null)
      {
         getDeclaredMethod(b, declaringClass, methodName, methodParameters);
      }
      else
      {
         bytecodeMethodResolver.getDeclaredMethod(file, b, declaringClass, methodName, methodParameters);
      }
      if (addProceed)
      {
         if (bytecodeMethodResolver == null)
         {
            getDeclaredMethod(b, file.getName(), methodName + SUPER_DELEGATE_SUFFIX, methodParameters);
         }
         else
         {
            bytecodeMethodResolver.getDeclaredMethod(file, b, file.getName(), methodName + SUPER_DELEGATE_SUFFIX, methodParameters);
         }
      }
      else
      {
         b.add(Opcode.ACONST_NULL);
      }

      b.addIconst(methodParameters.length);
      b.addAnewarray("java.lang.Object");

      int localVariableCount = 1;

      for (int i = 0; i < methodParameters.length; ++i)
      {
         String typeString = methodParameters[i];
         b.add(Opcode.DUP); // duplicate the array reference
         b.addIconst(i);
         // load the parameter value
         BytecodeUtils.addLoadInstruction(b, typeString, localVariableCount);
         // box the parameter if nessesary
         Boxing.boxIfNessesary(b, typeString);
         // and store it in the array
         b.add(Opcode.AASTORE);
         if (DescriptorUtils.isWide(typeString))
         {
            localVariableCount = localVariableCount + 2;
         }
         else
         {
            localVariableCount++;
         }
      }
      // now we have all our arguments on the stack
      // lets invoke the method
      b.addInvokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);
      if (addReturnInstruction)
      {
         // now we need to return the appropriate type
         if (returnType.equals("V"))
         {
            b.add(Opcode.RETURN);
         }
         else if (DescriptorUtils.isPrimitive(returnType))
         {
            Boxing.unbox(b, returnType);
            if (returnType.equals("D"))
            {
               b.add(Opcode.DRETURN);
            }
            else if (returnType.equals("F"))
            {
               b.add(Opcode.FRETURN);
            }
            else if (returnType.equals("J"))
            {
               b.add(Opcode.LRETURN);
            }
            else
            {
               b.add(Opcode.IRETURN);
            }
         }
         else
         {
            String castType = returnType;
            if (!returnType.startsWith("["))
            {
               castType = returnType.substring(1).substring(0, returnType.length() - 2);
            }
            b.addCheckcast(castType);
            b.add(Opcode.ARETURN);
         }
         if (b.getMaxLocals() < localVariableCount)
         {
            b.setMaxLocals(localVariableCount);
         }
      }
   }

   /**
    * Adds methods requiring special implementations rather than just
    * delegation.
    * 
    * @param proxyClassType the Javassist class description for the proxy type
    */
   protected void addSpecialMethods(ClassFile proxyClassType)
   {
      try
      {
         // Add special methods for interceptors
         for (Method method : LifecycleMixin.class.getDeclaredMethods())
         {
            log.trace("Adding method " + method);
            proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createInterceptorBody(proxyClassType, method, false), proxyClassType.getConstPool()));
         }
         Method getInstanceMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetInstance");
         Method getInstanceClassMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetClass");
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, getInstanceMethod.getReturnType(), getInstanceMethod.getName(), getInstanceMethod.getParameterTypes(), getInstanceMethod.getExceptionTypes(), generateGetTargetInstanceBody(proxyClassType), proxyClassType.getConstPool()));
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, getInstanceClassMethod.getReturnType(), getInstanceClassMethod.getName(), getInstanceClassMethod.getParameterTypes(), getInstanceClassMethod.getExceptionTypes(), generateGetTargetClassBody(proxyClassType), proxyClassType.getConstPool()));
         Method setMethodHandlerMethod = ProxyObject.class.getDeclaredMethod("setHandler", MethodHandler.class);
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, setMethodHandlerMethod.getReturnType(), setMethodHandlerMethod.getName(), setMethodHandlerMethod.getParameterTypes(), setMethodHandlerMethod.getExceptionTypes(), generateSetMethodHandlerBody(proxyClassType), proxyClassType.getConstPool()));
         Method getMethodHandlerMethod = ProxyObject.class.getDeclaredMethod("getHandler");
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, getMethodHandlerMethod.getReturnType(), getMethodHandlerMethod.getName(), getMethodHandlerMethod.getParameterTypes(), getMethodHandlerMethod.getExceptionTypes(), generateGetMethodHandlerBody(proxyClassType), proxyClassType.getConstPool()));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   private static Bytecode generateGetMethodHandlerBody(ClassFile file)
   {
      Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
      b.add(Opcode.ALOAD_0);
      b.addGetfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
      b.add(Opcode.ARETURN);
      return b;
   }

   private static Bytecode generateGetTargetInstanceBody(ClassFile file)
   {
      Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
      b.add(Opcode.ALOAD_0);
      b.add(Opcode.ARETURN);
      return b;
   }

   private static Bytecode generateGetTargetClassBody(ClassFile file)
   {
      Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
      BytecodeUtils.pushClassType(b, file.getSuperclass());
      b.add(Opcode.ARETURN);
      return b;
   }

   private static Bytecode generateSetMethodHandlerBody(ClassFile file)
   {
      Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
      b.add(Opcode.ALOAD_0);
      b.add(Opcode.ALOAD_1);
      b.addPutfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
      b.add(Opcode.RETURN);
      return b;
   }

}
