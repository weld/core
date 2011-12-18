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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.weld.Container;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.LifecycleMixin;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.bytecode.Boxing;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.ClassFileUtils;
import org.jboss.weld.util.bytecode.ConstructorUtils;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.JumpMarker;
import org.jboss.weld.util.bytecode.JumpUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.MethodUtils;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;
import org.jboss.weld.util.reflection.instantiation.InstantiatorFactory;
import org.slf4j.cal10n.LocLogger;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * Main factory to produce proxy classes and instances for Weld beans. This
 * implementation creates proxies which forward non-static method invocations to
 * a {@link BeanInstance}. All proxies implement the {@link Proxy} interface.
 *
 * @author David Allen
 * @author Stuart Douglas
 * @author Marius Bogoevici
 * @author Ales Justin
 */
public class ProxyFactory<T> {
    // The log provider
    protected static final LocLogger log = loggerFactory().getLogger(BEAN);
    // Default proxy class name suffix
    public static final String PROXY_SUFFIX = "$Proxy$";
    public static final String DEFAULT_PROXY_PACKAGE = "org.jboss.weld.proxies";

    private final Class<?> beanType;
    private final Set<Class<?>> additionalInterfaces = new LinkedHashSet<Class<?>>();
    private final ClassLoader classLoader;
    private final String baseProxyName;
    private final Bean<?> bean;
    private final String contextId;
    private final Class<?> proxiedBeanType;

    public static final String CONSTRUCTED_FLAG_NAME = "constructed";

    protected static final BytecodeMethodResolver DEFAULT_METHOD_RESOLVER = new DefaultBytecodeMethodResolver();

    /**
     * created a new proxy factory from a bean instance. The proxy name is
     * generated from the bean id
     */
    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        this(contextId, proxiedBeanType, typeClosure, getProxyName(contextId, proxiedBeanType, typeClosure, bean), bean);
    }

    /**
     * Creates a new proxy factory when the name of the proxy class is already
     * known, such as during de-serialization
     *
     * @param proxiedBeanType the super-class for this proxy class
     * @param typeClosure     the bean types of the bean
     * @param proxyName       the name of the proxy class
     */
    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName, Bean<?> bean) {
        this.bean = bean;
        this.contextId = contextId;
        this.proxiedBeanType = proxiedBeanType;
        for (Type type : typeClosure) {
            Class<?> c = Reflections.getRawType(type);
            // Ignore no-interface views, they are dealt with proxiedBeanType
            // (pending redesign)
            if (c.isInterface()) {
                addInterface(c);
            }
        }
        TypeInfo typeInfo = TypeInfo.of(typeClosure);
        Class<?> superClass = typeInfo.getSuperClass();
        superClass = superClass == null ? Object.class : superClass;
        if (superClass.equals(Object.class) && additionalInterfaces.isEmpty()) {
            // No interface beans must use the bean impl as superclass
            superClass = proxiedBeanType;
        }
        this.beanType = superClass;
        addDefaultAdditionalInterfaces();
        baseProxyName = proxyName;
        this.classLoader = resolveClassLoaderForBeanProxy(contextId, bean, typeInfo);
        // hierarchy order
        List<Class<?>> list = new ArrayList<Class<?>>(additionalInterfaces);
        Collections.sort(list, ClassHierarchyComparator.INSTANCE);
        additionalInterfaces.clear();
        additionalInterfaces.addAll(list);
    }

    static String getProxyName(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        TypeInfo typeInfo = TypeInfo.of(typeClosure);
        String proxyPackage;
        if (proxiedBeanType.equals(Object.class)) {
            Class<?> superInterface = typeInfo.getSuperInterface();
            if (superInterface == null) {
                throw new IllegalArgumentException("Proxied bean type cannot be java.lang.Object without an interface");
            } else {
                proxyPackage = DEFAULT_PROXY_PACKAGE;
            }
        } else {
            if (proxiedBeanType.getPackage() == null) {
                proxyPackage = DEFAULT_PROXY_PACKAGE;
            } else {
                proxyPackage = proxiedBeanType.getPackage().getName();
            }
        }
        final String className;

        if (typeInfo.getSuperClass() == Object.class) {
            final StringBuilder name = new StringBuilder();
            //interface only bean.
            className = createCompoundProxyName(contextId, bean, typeInfo, name) + PROXY_SUFFIX;
        } else {
            boolean typeModified = false;
            for (Class<?> iface : typeInfo.getInterfaces()) {
                if (!iface.isAssignableFrom(typeInfo.getSuperClass())) {
                    typeModified = true;
                    break;
                }
            }
            if (typeModified) {
                //this bean has interfaces that the base type is not assignable to
                //which can happen with some creative use of the SPI
                //interface only bean.
                StringBuilder name = new StringBuilder(typeInfo.getSuperClass().getSimpleName() + "$");
                className = createCompoundProxyName(contextId, bean, typeInfo, name) + PROXY_SUFFIX;
            } else {
                className = typeInfo.getSuperClass().getSimpleName() + PROXY_SUFFIX;
            }
        }


        return proxyPackage + '.' + className;
    }

    private static String createCompoundProxyName(String contextId, Bean<?> bean, TypeInfo typeInfo, StringBuilder name) {
        String className;
        final List<String> interfaces = new ArrayList<String>();
        for (Class<?> type : typeInfo.getInterfaces()) {
            interfaces.add(type.getSimpleName());
        }
        Collections.sort(interfaces);
        for (final String iface : interfaces) {
            name.append(iface);
            name.append('$');
        }
        //there is a remote chance that this could generate the same
        //proxy name for two interfaces with the same simple name.
        //append the hash code of the bean id to be sure
        final String id = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
        name.append(id.hashCode());
        className = name.toString();
        return className;
    }

    /**
     * Adds an additional interface that the proxy should implement. The default
     * implementation will be to forward invocations to the bean instance.
     *
     * @param newInterface an interface
     */
    public void addInterface(Class<?> newInterface) {
        if (!newInterface.isInterface()) {
            throw new IllegalArgumentException(newInterface + " is not an interface");
        }
        additionalInterfaces.add(newInterface);
    }

    /**
     * Method to create a new proxy that wraps the bean instance.
     *
     * @param beanInstance the bean instance
     * @return a new proxy object
     */
    public T create(BeanInstance beanInstance) {
        T proxy;
        Class<T> proxyClass = getProxyClass();
        try {
            InstantiatorFactory factory = Container.instance().services().get(InstantiatorFactory.class);
            if (factory != null && factory.useInstantiators()) {
                proxy = SecureReflections.newUnsafeInstance(proxyClass);
            } else {
                proxy = SecureReflections.newInstance(proxyClass);
            }
        } catch (InstantiationException e) {
            throw new DefinitionException(PROXY_INSTANTIATION_FAILED, e, this);
        } catch (IllegalAccessException e) {
            throw new DefinitionException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
        }
        ((ProxyObject) proxy).setHandler(new ProxyMethodHandler(contextId, beanInstance, bean));
        return proxy;
    }

    /**
     * Produces or returns the existing proxy class.
     *
     * @return always the class of the proxy
     */
    public Class<T> getProxyClass() {
        String suffix = "_$$_Weld" + getProxyNameSuffix();
        String proxyClassName = getBaseProxyName();
        if (!proxyClassName.endsWith(suffix)) {
            proxyClassName = proxyClassName + suffix;
        }
        if (proxyClassName.startsWith("java")) {
            proxyClassName = proxyClassName.replaceFirst("java", "org.jboss.weld");
        }
        Class<T> proxyClass = null;
        log.trace("Retrieving/generating proxy class " + proxyClassName);
        try {
            // First check to see if we already have this proxy class
            proxyClass = cast(classLoader.loadClass(proxyClassName));
        } catch (ClassNotFoundException e) {
            // Create the proxy class for this instance
            try {
                proxyClass = createProxyClass(proxyClassName);
            } catch (Throwable e1) {
                //attempt to load the class again, just in case another thread
                //defined it between the check and the create method
                try {
                    proxyClass = cast(classLoader.loadClass(proxyClassName));
                } catch (ClassNotFoundException e2) {
                    throw new WeldException(e1);
                }
            }
        }
        return proxyClass;
    }

    /**
     * Returns the package and base name for the proxy class.
     *
     * @return base name without suffixes
     */
    protected String getBaseProxyName() {
        return baseProxyName;
    }

    /**
     * Convenience method to determine if an object is a proxy generated by this
     * factory or any derived factory.
     *
     * @param proxySuspect the object suspected of being a proxy
     * @return true only if it is a proxy object
     */
    public static boolean isProxy(Object proxySuspect) {
        return proxySuspect instanceof ProxyObject;
    }

    /**
     * Convenience method to set the underlying bean instance for a proxy.
     *
     * @param proxy        the proxy instance
     * @param beanInstance the instance of the bean
     */
    public static <T> void setBeanInstance(String contextId, T proxy, BeanInstance beanInstance, Bean<?> bean) {
        if (proxy instanceof ProxyObject) {
            ProxyObject proxyView = (ProxyObject) proxy;
            proxyView.setHandler(new ProxyMethodHandler(contextId, beanInstance, bean));
        }
    }

    /**
     * Returns a suffix to append to the name of the proxy class. The name
     * already consists of <class-name>_$$_Weld, to which the suffix is added.
     * This allows the creation of different types of proxies for the same class.
     *
     * @return a name suffix
     */
    protected String getProxyNameSuffix() {
        return PROXY_SUFFIX;
    }

    private void addDefaultAdditionalInterfaces() {
        additionalInterfaces.add(Serializable.class);
    }

    /**
     * Sub classes may override to specify additional interfaces the proxy should
     * implement
     */
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
    }

    private Class<T> createProxyClass(String proxyClassName) throws Exception {
        ArraySet<Class<?>> specialInterfaces = new ArraySet<Class<?>>(3);
        specialInterfaces.add(LifecycleMixin.class);
        specialInterfaces.add(TargetInstanceProxy.class);
        specialInterfaces.add(ProxyObject.class);
        addAdditionalInterfaces(specialInterfaces);
        // Remove special interfaces from main set (deserialization scenario)
        additionalInterfaces.removeAll(specialInterfaces);

        ClassFile proxyClassType = null;
        if (beanType.isInterface()) {
            proxyClassType = new ClassFile(false, proxyClassName, Object.class.getName());
            proxyClassType.addInterface(beanType.getName());
        } else {
            proxyClassType = new ClassFile(false, proxyClassName, beanType.getName());
        }
        proxyClassType.setVersionToJava5();
        proxyClassType.setAccessFlags(AccessFlag.PUBLIC);
        // Add interfaces which require method generation
        for (Class<?> clazz : additionalInterfaces) {
            proxyClassType.addInterface(clazz.getName());
        }
        Bytecode initialValueBytecode = new Bytecode(proxyClassType.getConstPool());

        addFields(proxyClassType, initialValueBytecode);
        addConstructors(proxyClassType, initialValueBytecode);
        addMethods(proxyClassType);

        // Additional interfaces whose methods require special handling
        for (Class<?> specialInterface : specialInterfaces) {
            proxyClassType.addInterface(specialInterface.getName());
        }
        // TODO: change the ProxyServices SPI to allow the container to figure out
        // which PD to use

        ProtectionDomain domain = proxiedBeanType.getProtectionDomain();
        if(proxiedBeanType.getPackage() == null || proxiedBeanType.equals(Object.class)) {
            domain = ProxyFactory.class.getProtectionDomain();
        }
        Class<T> proxyClass = cast(ClassFileUtils.toClass(proxyClassType, classLoader, domain));
        log.trace("Created Proxy class of type " + proxyClass + " supporting interfaces " + Arrays.toString(proxyClass.getInterfaces()));
        return proxyClass;
    }

    /**
     * Adds a constructor for the proxy for each constructor declared by the base
     * bean type.
     *
     * @param proxyClassType       the Javassist class for the proxy
     * @param initialValueBytecode
     */
    protected void addConstructors(ClassFile proxyClassType, Bytecode initialValueBytecode) {
        try {
            if (beanType.isInterface()) {
                ConstructorUtils.addDefaultConstructor(proxyClassType, initialValueBytecode);
            } else {
                boolean constructorFound = false;
                for (Constructor<?> constructor : beanType.getDeclaredConstructors()) {
                    if ((constructor.getModifiers() & Modifier.PRIVATE) == 0) {
                        constructorFound = true;
                        String[] exceptions = new String[constructor.getExceptionTypes().length];
                        for (int i = 0; i < exceptions.length; ++i) {
                            exceptions[i] = constructor.getExceptionTypes()[i].getName();
                        }
                        ConstructorUtils.addConstructor(DescriptorUtils.getConstructorDescriptor(constructor), exceptions, proxyClassType, initialValueBytecode);
                    }
                }
                if (!constructorFound) {
                    // the bean only has private constructors, we need to generate
                    // two fake constructors that call each other
                    addConstructorsForBeanWithPrivateConstructors(proxyClassType);
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    protected void addFields(ClassFile proxyClassType, Bytecode initialValueBytecode) {
        try {
            // The field representing the underlying instance or special method
            // handling
            proxyClassType.addField(new FieldInfo(proxyClassType.getConstPool(), "methodHandler", "Ljavassist/util/proxy/MethodHandler;"));
            // field used to indicate that super() has been called
            FieldInfo constfield = new FieldInfo(proxyClassType.getConstPool(), CONSTRUCTED_FLAG_NAME, "Z");
            constfield.setAccessFlags(AccessFlag.PRIVATE);
            proxyClassType.addField(constfield);
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    protected void addMethods(ClassFile proxyClassType) {
        // Add all class methods for interception
        addMethodsFromClass(proxyClassType);

        // Add special proxy methods
        addSpecialMethods(proxyClassType);

        // Add serialization support methods
        addSerializationSupport(proxyClassType);
    }

    /**
     * Adds special serialization code. By default this is a nop
     *
     * @param proxyClassType the Javassist class for the proxy class
     */
    protected void addSerializationSupport(ClassFile proxyClassType) {
        //noop
    }

    protected void addMethodsFromClass(ClassFile proxyClassType) {
        try {
            // Add all methods from the class heirachy
            Class<?> cls = beanType;
            // first add equals/hashCode methods if required
            MethodInfo equalsMethod = generateEqualsMethod(proxyClassType);
            if (equalsMethod != null) {
                proxyClassType.addMethod(equalsMethod);
            }
            MethodInfo hashCodeMethod = generateHashCodeMethod(proxyClassType);
            if (hashCodeMethod != null) {
                proxyClassType.addMethod(hashCodeMethod);
            }

            while (cls != null) {
                for (Method method : cls.getDeclaredMethods()) {
                    if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers()) && (method.getDeclaringClass() != Object.class || method.getName().equals("toString"))) {
                        try {
                            MethodInformation methodInfo = new RuntimeMethodInformation(method);
                            proxyClassType.addMethod(MethodUtils.makeMethod(methodInfo, method.getExceptionTypes(), addConstructedGuardToMethodBody(proxyClassType, createForwardingMethodBody(proxyClassType, methodInfo), methodInfo), proxyClassType.getConstPool()));
                            log.trace("Adding method " + method);
                        } catch (DuplicateMemberException e) {
                            // do nothing. This will happen if superclass methods
                            // have been overridden
                        }
                    }
                }
                cls = cls.getSuperclass();
            }
            for (Class<?> c : additionalInterfaces) {
                for (Method method : c.getMethods()) {
                    try {
                        MethodInformation methodInfo = new RuntimeMethodInformation(method);
                        proxyClassType.addMethod(MethodUtils.makeMethod(methodInfo, method.getExceptionTypes(), createSpecialMethodBody(proxyClassType, methodInfo), proxyClassType.getConstPool()));
                        log.trace("Adding method " + method);
                    } catch (DuplicateMemberException e) {
                    }
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Generate the body of the proxies hashCode method.
     * <p/>
     * If this method returns null, the method will not be added, and the
     * hashCode on the superclass will be used as per normal virtual method
     * resolution rules
     */
    protected MethodInfo generateHashCodeMethod(ClassFile proxyClassType) {
        return null;
    }

    /**
     * Generate the body of the proxies equals method.
     * <p/>
     * If this method returns null, the method will not be added, and the
     * hashCode on the superclass will be used as per normal virtual method
     * resolution rules
     *
     * @param proxyClassType The class file
     */
    protected MethodInfo generateEqualsMethod(ClassFile proxyClassType) {
        return null;
    }

    protected Bytecode createSpecialMethodBody(ClassFile proxyClassType, MethodInformation method) throws NotFoundException {
        return createInterceptorBody(proxyClassType, method);
    }

    /**
     * Adds the following code to a delegating method:
     * <p/>
     * <code>
     * if(!this.constructed) return super.thisMethod()
     * </code>
     * <p/>
     * This means that the proxy will not start to delegate to the underlying
     * bean instance until after the constructor has finished.
     */
    protected Bytecode addConstructedGuardToMethodBody(ClassFile proxyClassType, Bytecode existingMethod, MethodInformation method) {
        String methodDescriptor = method.getDescriptor();

        // now create the conditional
        Bytecode cond = new Bytecode(proxyClassType.getConstPool());
        cond.add(Opcode.ALOAD_0);
        cond.addGetfield(proxyClassType.getName(), CONSTRUCTED_FLAG_NAME, "Z");

        // jump if the proxy constructor has finished
        cond.add(Opcode.IFNE);
        JumpMarker invokeSpecial = JumpUtils.addJumpInstruction(cond);
        // generate the invokespecial call to the super class method
        // this is run when the proxy is being constructed
        cond.add(Opcode.ALOAD_0);
        BytecodeUtils.loadParameters(cond, methodDescriptor);
        cond.addInvokespecial(proxyClassType.getSuperclass(), method.getName(), methodDescriptor);
        BytecodeUtils.addReturnInstruction(cond, method.getReturnType());
        invokeSpecial.mark();

        // store the offset for copying the exception table
        int offset = cond.currentPc();
        // copy the byecode of the original method
        byte[] methodBodyBytes = existingMethod.get();
        for (byte methodBodyByte : methodBodyBytes) {
            cond.add(methodBodyByte);
        }
        // copy the exception table of the original method, if any (shift the table values to account for the guard)
        ExceptionTable originalExceptionTable = existingMethod.getExceptionTable();
        if (originalExceptionTable.size() > 0) {
            for (int i = 0; i < originalExceptionTable.size(); i++) {
                cond.addExceptionHandler(originalExceptionTable.startPc(i) + offset,
                        originalExceptionTable.endPc(i) + offset,
                        originalExceptionTable.handlerPc(i) + offset,
                        originalExceptionTable.catchType(i));
            }
        }
        cond.setMaxLocals(existingMethod.getMaxLocals());
        cond.setMaxStack(existingMethod.getMaxStack());
        return cond;
    }

    protected Bytecode createForwardingMethodBody(ClassFile proxyClassType, MethodInformation method) throws NotFoundException {
        return createInterceptorBody(proxyClassType, method);
    }

    /**
     * Creates the given method on the proxy class where the implementation
     * forwards the call directly to the method handler.
     * <p/>
     * the generated bytecode is equivalent to:
     * <p/>
     * return (RetType) methodHandler.invoke(this,param1,param2);
     *
     * @param file   the class file
     * @param method any JLR method
     * @return the method byte code
     */
    protected Bytecode createInterceptorBody(ClassFile file, MethodInformation method) throws NotFoundException {
        Bytecode b = new Bytecode(file.getConstPool());
        invokeMethodHandler(file, b, method, true, DEFAULT_METHOD_RESOLVER);
        return b;
    }

    /**
     * calls methodHandler.invoke for a given method
     *
     * @param file                   the current class file
     * @param b                      the bytecode to add the methodHandler.invoke call to
     * @param method                 The method information
     * @param addReturnInstruction   set to true you want to return the result of
     *                               the method invocation
     * @param bytecodeMethodResolver The resolver that returns the method to invoke
     */
    protected static void invokeMethodHandler(ClassFile file, Bytecode b, MethodInformation method, boolean addReturnInstruction, BytecodeMethodResolver bytecodeMethodResolver) {
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
        bytecodeMethodResolver.getDeclaredMethod(file, b, method.getDeclaringClass(), method.getName(), method.getParameterTypes());
        b.add(Opcode.ACONST_NULL);

        b.addIconst(method.getParameterTypes().length);
        b.addAnewarray("java.lang.Object");

        int localVariableCount = 1;

        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            String typeString = method.getParameterTypes()[i];
            b.add(Opcode.DUP); // duplicate the array reference
            b.addIconst(i);
            // load the parameter value
            BytecodeUtils.addLoadInstruction(b, typeString, localVariableCount);
            // box the parameter if nessesary
            Boxing.boxIfNessesary(b, typeString);
            // and store it in the array
            b.add(Opcode.AASTORE);
            if (DescriptorUtils.isWide(typeString)) {
                localVariableCount = localVariableCount + 2;
            } else {
                localVariableCount++;
            }
        }
        // now we have all our arguments on the stack
        // lets invoke the method
        b.addInvokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);
        if (addReturnInstruction) {
            // now we need to return the appropriate type
            if (method.getReturnType().equals("V")) {
                b.add(Opcode.RETURN);
            } else if (DescriptorUtils.isPrimitive(method.getReturnType())) {
                Boxing.unbox(b, method.getReturnType());
                if (method.getReturnType().equals("D")) {
                    b.add(Opcode.DRETURN);
                } else if (method.getReturnType().equals("F")) {
                    b.add(Opcode.FRETURN);
                } else if (method.getReturnType().equals("J")) {
                    b.add(Opcode.LRETURN);
                } else {
                    b.add(Opcode.IRETURN);
                }
            } else {
                String castType = method.getReturnType();
                if (!method.getReturnType().startsWith("[")) {
                    castType = method.getReturnType().substring(1).substring(0, method.getReturnType().length() - 2);
                }
                b.addCheckcast(castType);
                b.add(Opcode.ARETURN);
            }
            if (b.getMaxLocals() < localVariableCount) {
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
    protected void addSpecialMethods(ClassFile proxyClassType) {
        try {
            // Add special methods for interceptors
            for (Method method : LifecycleMixin.class.getDeclaredMethods()) {
                log.trace("Adding method " + method);
                MethodInformation methodInfo = new RuntimeMethodInformation(method);
                proxyClassType.addMethod(MethodUtils.makeMethod(methodInfo, method.getExceptionTypes(), createInterceptorBody(proxyClassType, methodInfo), proxyClassType.getConstPool()));
            }
            Method getInstanceMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetInstance");
            Method getInstanceClassMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetClass");

            MethodInformation getInstanceMethodInfo = new RuntimeMethodInformation(getInstanceMethod);
            proxyClassType.addMethod(MethodUtils.makeMethod(getInstanceMethodInfo, getInstanceMethod.getExceptionTypes(), createInterceptorBody(proxyClassType, getInstanceMethodInfo), proxyClassType.getConstPool()));

            MethodInformation getInstanceClassMethodInfo = new RuntimeMethodInformation(getInstanceClassMethod);
            proxyClassType.addMethod(MethodUtils.makeMethod(getInstanceClassMethodInfo, getInstanceClassMethod.getExceptionTypes(), createInterceptorBody(proxyClassType, getInstanceClassMethodInfo), proxyClassType.getConstPool()));

            Method setMethodHandlerMethod = ProxyObject.class.getDeclaredMethod("setHandler", MethodHandler.class);
            MethodInformation setMethodHandlerMethodInfo = new RuntimeMethodInformation(setMethodHandlerMethod);
            proxyClassType.addMethod(MethodUtils.makeMethod(setMethodHandlerMethodInfo, setMethodHandlerMethod.getExceptionTypes(), generateSetMethodHandlerBody(proxyClassType), proxyClassType.getConstPool()));
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    private static Bytecode generateSetMethodHandlerBody(ClassFile file) {
        Bytecode b = new Bytecode(file.getConstPool(), 3, 2);
        b.add(Opcode.ALOAD_0);
        b.add(Opcode.ALOAD_1);
        b.addPutfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
        b.add(Opcode.RETURN);
        return b;
    }


    /**
     * Adds two constructors to the class that call each other in order to bypass
     * the JVM class file verifier.
     * <p/>
     * This would result in a stack overflow if they were actually called,
     * however the proxy is directly created without calling the constructor
     */
    private void addConstructorsForBeanWithPrivateConstructors(ClassFile proxyClassType) {
        try {
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
        } catch (DuplicateMemberException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public Set<Class<?>> getAdditionalInterfaces() {
        return additionalInterfaces;
    }

    public Bean<?> getBean() {
        return bean;
    }

    public String getContextId() {
        return contextId;
    }

    /**
     * Figures out the correct class loader to use for a proxy for a given bean
     */
    public static ClassLoader resolveClassLoaderForBeanProxy(String contextId, Bean<?> bean, TypeInfo typeInfo) {
        Class<?> superClass = typeInfo.getSuperClass();
        if (superClass.getName().startsWith("java")) {
            ClassLoader cl = Container.instance(contextId).services().get(ProxyServices.class).getClassLoader(bean.getBeanClass());
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            return cl;
        }
        return Container.instance(contextId).services().get(ProxyServices.class).getClassLoader(superClass);
    }

    public static ClassLoader resolveClassLoaderForBeanProxy(String contextId, Bean<?> bean) {
        return resolveClassLoaderForBeanProxy(contextId, bean, TypeInfo.of(bean.getTypes()));
    }


}
