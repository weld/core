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
import static org.jboss.weld.logging.messages.BeanMessage.FAILED_TO_SET_THREAD_LOCAL_ON_PROXY;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;

import javax.enterprise.inject.spi.Bean;

import org.jboss.interceptor.proxy.LifecycleMixin;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.bytecode.Boxing;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.ClassFileUtils;
import org.jboss.weld.util.bytecode.ConstructorUtils;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.MethodUtils;
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
 * @author Stuart Douglas
 */
public class ProxyFactory<T>
{
   // The log provider
   protected static final LocLogger log = loggerFactory().getLogger(BEAN);
   // Default proxy class name suffix
   public static final String PROXY_SUFFIX = "Proxy";
   public static final String DEFAULT_PROXY_PACKAGE = "org.jboss.weld.proxies";

   private final Class<?> beanType;
   private final Set<Class<?>> additionalInterfaces = new HashSet<Class<?>>();
   private final ClassLoader classLoader;
   private final String baseProxyName;

   private static final String FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME = "firstSerializationPhaseComplete";

   /**
    * created a new proxy factory from a bean instance. The proxy name is
    * generated from the bean id
    * 
    * @param proxiedBeanType
    * @param businessInterfaces
    * @param bean
    */
   public ProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean)
   {
      this(proxiedBeanType, typeClosure, getProxyName(proxiedBeanType, typeClosure, bean));
   }

   /**
    * Creates a new proxy factory when the name of the proxy class is already
    * known, such as during de-serialization
    * 
    * @param proxiedBeanType the super-class for this proxy class
    * @param typeClosure the bean types of the bean
    * @param the name of the proxy class
    * 
    */
   public ProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName)
   {
      for (Type type : typeClosure)
      {
         Class<?> c = Reflections.getRawType(type);
         // Ignore no-interface views, they are dealt with proxiedBeanType
         // (pending redesign)
         if (c.isInterface())
         {
            addInterface(c);
         }
      }
      TypeInfo typeInfo = TypeInfo.of(typeClosure);
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
      addDefaultAdditionalInterfaces();
      baseProxyName = proxyName;
   }

   private static String getProxyName(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean)
   {
      TypeInfo typeInfo = TypeInfo.of(typeClosure);
      String proxyPackage;
      if (proxiedBeanType.equals(Object.class))
      {
         Class<?> superInterface = typeInfo.getSuperInterface();
         if (superInterface == null)
         {
            throw new IllegalArgumentException("Proxied bean type cannot be java.lang.Object without an interface");
         }
         else
         {
            proxyPackage=DEFAULT_PROXY_PACKAGE;
         }
      }
      else
      {
         proxyPackage = proxiedBeanType.getPackage().getName();
      }
      String beanId = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
      String className = beanId.replace('.', '$').replace(' ', '_').replace('/', '$').replace(';', '$');
      return proxyPackage + '.' + className;
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
            //we need to inialize the ThreadLocal via reflection
            // TODO: there is probably a better way to to this
            try
            {
               Field sfield = proxyClass.getDeclaredField(FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME);
               sfield.setAccessible(true);
               
               @SuppressWarnings("rawtypes")
               ThreadLocal threadLocal = new ThreadLocal();
               
               sfield.set(proxy, threadLocal);
            }
            catch(Exception e)
            {
               throw new DefinitionException(FAILED_TO_SET_THREAD_LOCAL_ON_PROXY, e, this);
            }
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
   public Class<T> getProxyClass()
   {
      String suffix = "_$$_Weld" + getProxyNameSuffix();
      String proxyClassName = getBaseProxyName();
      if (!proxyClassName.endsWith(suffix))
      {
         proxyClassName = proxyClassName + suffix;
      }
      if (proxyClassName.startsWith("java"))
      {
         proxyClassName = proxyClassName.replaceFirst("java", "org.jboss.weld");
      }
      Class<T> proxyClass = null;
      log.trace("Retrieving/generating proxy class " + proxyClassName);
      try
      {
         // First check to see if we already have this proxy class
         proxyClass = cast(classLoader.loadClass(proxyClassName));
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

   /**
    * Sub classes may override to specify additional interfaces the proxy should
    * implement
    */
   protected void addAdditionalInterfaces(Set<Class<?>> interfaces)
   {

   }

   private Class<T> createProxyClass(String proxyClassName) throws Exception
   {
      ArraySet<Class<?>> specialInterfaces = new ArraySet<Class<?>>(3);
      specialInterfaces.add(LifecycleMixin.class);
      specialInterfaces.add(TargetInstanceProxy.class);
      specialInterfaces.add(ProxyObject.class);
      addAdditionalInterfaces(specialInterfaces);
      // Remove special interfaces from main set (deserialization scenario)
      additionalInterfaces.removeAll(specialInterfaces);

      ClassFile proxyClassType = null;
      if (beanType.isInterface())
      {
         proxyClassType = new ClassFile(false, proxyClassName, Object.class.getName());
         proxyClassType.addInterface(beanType.getName());
      }
      else
      {
         proxyClassType = new ClassFile(false, proxyClassName, beanType.getName());
      }
      proxyClassType.setVersionToJava5();
      proxyClassType.setAccessFlags(AccessFlag.PUBLIC);
      // Add interfaces which require method generation
      for (Class<?> clazz : additionalInterfaces)
      {
         proxyClassType.addInterface(clazz.getName());
      }
      Bytecode initialValueBytecode = new Bytecode(proxyClassType.getConstPool());

      addFields(proxyClassType, initialValueBytecode);
      addConstructors(proxyClassType, initialValueBytecode);
      addMethods(proxyClassType);

      // Additional interfaces whose methods require special handling
      for (Class<?> specialInterface : specialInterfaces)
      {
         proxyClassType.addInterface(specialInterface.getName());
      }
      Class<T> proxyClass = cast(ClassFileUtils.toClass(proxyClassType, classLoader, null));
      log.trace("Created Proxy class of type " + proxyClass + " supporting interfaces " + Arrays.toString(proxyClass.getInterfaces()));
      return proxyClass;
   }

   /**
    * Adds a constructor for the proxy for each constructor declared by the base
    * bean type.
    * 
    * @param proxyClassType the Javassist class for the proxy
    * @param initialValueBytecode
    */
   protected void addConstructors(ClassFile proxyClassType, Bytecode initialValueBytecode)
   {
      try
      {
         if (beanType.isInterface())
         {
            ConstructorUtils.addDefaultConstructor(proxyClassType, initialValueBytecode);
         }
         else
         {
            boolean constructorFound = false;
            for (Constructor<?> constructor : beanType.getDeclaredConstructors())
            {
               if ((constructor.getModifiers() & Modifier.PRIVATE) == 0)
               {
                  constructorFound = true;
                  String[] exceptions = new String[constructor.getExceptionTypes().length];
                  for (int i = 0; i < exceptions.length; ++i)
                  {
                     exceptions[i] = constructor.getExceptionTypes()[i].getName();
                  }
                  ConstructorUtils.addConstructor(DescriptorUtils.getConstructorDescriptor(constructor), exceptions, proxyClassType, initialValueBytecode);
               }
            }
            if (!constructorFound)
            {
               // the bean only has private constructors, we need to generate
               // two fake constructors that call each other
               addConstructorsForBeanWithPrivateConstructors(proxyClassType);
            }
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   protected void addFields(ClassFile proxyClassType, Bytecode initialValueBytecode)
   {
      try
      {
         // The field representing the underlying instance or special method
         // handling
         proxyClassType.addField(new FieldInfo(proxyClassType.getConstPool(), "methodHandler", "Ljavassist/util/proxy/MethodHandler;"));
         // Special field used during serialization of a proxy
         FieldInfo sfield = new FieldInfo(proxyClassType.getConstPool(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
         sfield.setAccessFlags(AccessFlag.TRANSIENT | AccessFlag.PRIVATE);
         proxyClassType.addField(sfield);
         // we need to initialize this to a new ThreadLocal
         initialValueBytecode.addAload(0);
         initialValueBytecode.addNew("java/lang/ThreadLocal");
         initialValueBytecode.add(Opcode.DUP);
         initialValueBytecode.addInvokespecial("java.lang.ThreadLocal", "<init>", "()V");
         initialValueBytecode.addPutfield(proxyClassType.getName(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   private void addMethods(ClassFile proxyClassType)
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
   protected void addSerializationSupport(ClassFile proxyClassType)
   {
      try
      {
         // Create a two phase writeReplace where the first call uses a
         // replacement object and the subsequent call get the proxy object.
         Class<?>[] exceptions = new Class[] { ObjectStreamException.class };
         Bytecode writeReplaceBody = createWriteReplaceBody(proxyClassType);
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, Object.class, "writeReplace", new Class[] {}, exceptions, writeReplaceBody, proxyClassType.getConstPool()));

         // Also add a static method that can be used to deserialize a proxy
         // object.
         // This causes the OO input stream to use the class loader from this
         // class.
         exceptions = new Class[] { ClassNotFoundException.class, IOException.class };
         Bytecode deserializeProxyBody = createDeserializeProxyBody(proxyClassType);
         proxyClassType.addMethod(MethodUtils.makeMethod(Modifier.STATIC | Modifier.PUBLIC, Object.class, "deserializeProxy", new Class[] { ObjectInputStream.class }, exceptions, deserializeProxyBody, proxyClassType.getConstPool()));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }

   }

   /**
    * creates a bytecode fragment that returns $1.readObject()
    * 
    */
   private Bytecode createDeserializeProxyBody(ClassFile file)
   {
      Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
      b.addAload(0);
      b.addInvokevirtual("java.io.ObjectInputStream", "readObject", "()Ljava/lang/Object;");
      // initialize the transient threadlocal
      b.add(Opcode.DUP);
      b.addCheckcast(file.getName());
      b.addNew("java/lang/ThreadLocal");
      b.add(Opcode.DUP);
      b.addInvokespecial("java.lang.ThreadLocal", "<init>", "()V");
      b.addPutfield(file.getName(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
      b.addOpcode(Opcode.ARETURN);
      return b;
   }

   /**
    * creates serialization code. In java this code looks like:
    * 
    * <pre>
    *  Boolean value = firstSerializationPhaseComplete.get();
    *  if (firstSerializationPhaseComplete!=null) {
    *   firstSerializationPhaseComplete.remove();\n");
    *   return $0;
    *  } else {
    *    firstSerializationPhaseComplete.set(Boolean.TRUE);
    *    return methodHandler.invoke($0,$proxyClassTypeName.class.getMethod("writeReplace", null), null, $args);
    *  }
    * }
    * </pre>
    * 
    * the use TRUE,null rather than TRUE,FALSE to avoid the need to subclass
    * ThreadLocal, which would be problematic
    */
   private Bytecode createWriteReplaceBody(ClassFile proxyClassType)
   {
      // we need to build up the conditional body first
      // this bytecode is run if firstSerializationPhaseComplete=true
      Bytecode runSecondPhase = new Bytecode(proxyClassType.getConstPool());
      // set firstSerializationPhaseComplete=false
      runSecondPhase.add(Opcode.ALOAD_0);
      runSecondPhase.addGetfield(proxyClassType.getName(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
      runSecondPhase.addInvokevirtual("java.lang.ThreadLocal", "remove", "()V");
      // return this
      runSecondPhase.add(Opcode.ALOAD_0);
      runSecondPhase.add(Opcode.ARETURN);
      byte[] runSecondBytes = runSecondPhase.get();
      Bytecode b = new Bytecode(proxyClassType.getConstPool());
      b.add(Opcode.ALOAD_0);
      b.addGetfield(proxyClassType.getName(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
      b.addInvokevirtual("java.lang.ThreadLocal", "get", "()Ljava/lang/Object;");
      b.add(Opcode.IFNULL);
      // +3 because the IFNULL sequence is 3 bytes long
      BytecodeUtils.add16bit(b, runSecondBytes.length + 3);
      for (int i = 0; i < runSecondBytes.length; ++i)
      {
         b.add(runSecondBytes[i]);
      }
      // now create the rest of the bytecode
      // set firstSerializationPhaseComplete=true
      b.add(Opcode.ALOAD_0);
      b.addGetfield(proxyClassType.getName(), FIRST_SERIALIZATION_PHASE_COMPLETE_FIELD_NAME, "Ljava/lang/ThreadLocal;");
      b.addGetstatic("java.lang.Boolean", "TRUE", "Ljava/lang/Boolean;");
      b.addInvokevirtual("java.lang.ThreadLocal", "set", "(Ljava/lang/Object;)V");

      b.add(Opcode.ALOAD_0);
      b.addGetfield(proxyClassType.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
      b.add(Opcode.ALOAD_0);
      getDeclaredMethod(b, proxyClassType.getName(), "writeReplace", new String[0]);
      b.add(Opcode.ACONST_NULL);

      b.addIconst(0);
      b.addAnewarray("java.lang.Object");
      // now we have all our arguments on the stack
      // lets invoke the method
      b.addInvokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);
      b.add(Opcode.ARETURN);
      b.setMaxLocals(1);
      return b;
   }

   protected void addMethodsFromClass(ClassFile proxyClassType)
   {
      try
      {
         // Add all methods from the class heirachy
         Class<?> cls = beanType;
         while (cls != null)
         {
            for (Method method : cls.getDeclaredMethods())
            {
               if (!Modifier.isStatic(method.getModifiers()) && (method.getDeclaringClass() != Object.class || method.getName().equals("toString")))
               {
                  try
                  {
                     proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(),
                           createForwardingMethodBody(proxyClassType, method), proxyClassType.getConstPool()));
                     log.trace("Adding method " + method);
                  }
                  catch (DuplicateMemberException e)
                  {
                     // do nothing. This will happen if superclass methods have
                     // been overridden
                  }
               }
            }
            cls = cls.getSuperclass();
         }
         for (Class<?> c : additionalInterfaces)
         {
            for (Method method : c.getMethods())
            {
               try
               {
                  proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(),
                        createSpecialMethodBody(proxyClassType, method), proxyClassType.getConstPool()));
                  log.trace("Adding method " + method);
               }
               catch (DuplicateMemberException e)
               {
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   protected Bytecode createSpecialMethodBody(ClassFile proxyClassType, Method method) throws NotFoundException
   {
      return createInterceptorBody(proxyClassType, method);
   }

   protected Bytecode createForwardingMethodBody(ClassFile proxyClassType, Method method) throws NotFoundException
   {
      return createInterceptorBody(proxyClassType, method);
   }

   /**
    * Creates the given method on the proxy class where the implementation
    * forwards the call directly to the method handler.
    * 
    * the generated bytecode is equivalent to:
    * 
    * return (RetType) methodHandler.invoke(this,param1,param2);
    * 
    * @param file the class file
    * @param method any JLR method
    * @return the method byte code
    */
   protected Bytecode createInterceptorBody(ClassFile file, Method method) throws NotFoundException
   {
      Bytecode b = new Bytecode(file.getConstPool());
      String[] ptypes = new String[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; ++i)
      {
         ptypes[i] = DescriptorUtils.classToStringRepresentation(method.getParameterTypes()[i]);
      }
      invokeMethodHandler(file, b, method.getDeclaringClass().getName(), method.getName(), ptypes, DescriptorUtils.classToStringRepresentation(method.getReturnType()), true, null);
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
    *           the method invocation
    */
   protected static void invokeMethodHandler(ClassFile file, Bytecode b, String declaringClass, String methodName, String[] methodParameters, String returnType, boolean addReturnInstruction, BytecodeMethodResolver bytecodeMethodResolver)
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
      b.add(Opcode.ACONST_NULL);

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
            proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, method.getReturnType(), method.getName(), method.getParameterTypes(), method.getExceptionTypes(), createInterceptorBody(proxyClassType, method), proxyClassType.getConstPool()));
         }
         Method getInstanceMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetInstance");
         Method getInstanceClassMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetClass");
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, getInstanceMethod.getReturnType(), getInstanceMethod.getName(), getInstanceMethod.getParameterTypes(), getInstanceMethod.getExceptionTypes(), createInterceptorBody(proxyClassType, getInstanceMethod), proxyClassType.getConstPool()));
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, getInstanceClassMethod.getReturnType(), getInstanceClassMethod.getName(), getInstanceClassMethod.getParameterTypes(), getInstanceClassMethod.getExceptionTypes(), createInterceptorBody(proxyClassType, getInstanceClassMethod), proxyClassType.getConstPool()));
         Method setMethodHandlerMethod = ProxyObject.class.getDeclaredMethod("setHandler", MethodHandler.class);
         proxyClassType.addMethod(MethodUtils.makeMethod(AccessFlag.PUBLIC, setMethodHandlerMethod.getReturnType(), setMethodHandlerMethod.getName(), setMethodHandlerMethod.getParameterTypes(), setMethodHandlerMethod.getExceptionTypes(), generateSetMethodHandlerBody(proxyClassType), proxyClassType.getConstPool()));
      }
      catch (Exception e)
      {
         throw new WeldException(e);
      }
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

   /**
    * generates some bytecode that will load the given method object into the
    * top of the stack
    */
   protected static void getDeclaredMethod(Bytecode code, Method method)
   {
      String[] ptypes = new String[method.getParameterTypes().length];
      for (int i = 0; i < method.getParameterTypes().length; ++i)
      {
         ptypes[i] = method.getParameterTypes()[i].getName();
      }
      getDeclaredMethod(code, method.getDeclaringClass().getName(), method.getName(), ptypes);
   }

   /**
    * generates some bytecode that will load the given method object into the
    * top of the stack
    */
   protected static void getDeclaredMethod(Bytecode code, String declaringClass, String methodName, String[] parameterTypes)
   {
      BytecodeUtils.pushClassType(code, declaringClass);
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

   /**
    * Adds two constructors to the class that call each other in order to bypass
    * the JVM class file verifier.
    * 
    * This would result in a stack overflow if they were actually called,
    * however the proxy is directly created without calling the constructor
    * 
    */
   private void addConstructorsForBeanWithPrivateConstructors(ClassFile proxyClassType)
   {
      try
      {
         MethodInfo ctor = new MethodInfo(proxyClassType.getConstPool(), "<init>", "(Ljava/lang/Byte;)V");
         Bytecode b = new Bytecode(proxyClassType.getConstPool(), 3, 3);
         b.add(Opcode.ALOAD_0);
         b.add(Opcode.ACONST_NULL);
         b.add(Opcode.ACONST_NULL);
         b.addInvokespecial(proxyClassType.getName(), "<init>", "(Ljava/lang/Byte;Ljava/lang/Byte;)V");
         b.add(Opcode.RETURN);
         ctor.setCodeAttribute(b.toCodeAttribute());
         ctor.setAccessFlags(AccessFlag.PUBLIC);
         proxyClassType.addMethod(ctor);

         ctor = new MethodInfo(proxyClassType.getConstPool(), "<init>", "(Ljava/lang/Byte;Ljava/lang/Byte;)V");
         b = new Bytecode(proxyClassType.getConstPool(), 3, 3);
         b.add(Opcode.ALOAD_0);
         b.add(Opcode.ACONST_NULL);
         b.addInvokespecial(proxyClassType.getName(), "<init>", "(Ljava/lang/Byte;)V");
         b.add(Opcode.RETURN);
         ctor.setCodeAttribute(b.toCodeAttribute());
         ctor.setAccessFlags(AccessFlag.PUBLIC);
         proxyClassType.addMethod(ctor);
      }
      catch (DuplicateMemberException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Class<?> getBeanType()
   {
      return beanType;
   }

   static protected interface BytecodeMethodResolver
   {
      abstract void getDeclaredMethod(ClassFile file, Bytecode code, String declaringClass, String methodName, String[] parameterTypes);
   }

}
