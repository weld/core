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

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.DuplicateMemberException;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
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
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
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
    private final Class<?> proxiedBeanType;

    public static final String CONSTRUCTED_FLAG_NAME = "constructed";

    protected static final BytecodeMethodResolver DEFAULT_METHOD_RESOLVER = new DefaultBytecodeMethodResolver();

    /**
     * created a new proxy factory from a bean instance. The proxy name is
     * generated from the bean id
     */
    public ProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        this(proxiedBeanType, typeClosure, getProxyName(proxiedBeanType, typeClosure, bean), bean);
    }

    /**
     * Creates a new proxy factory when the name of the proxy class is already
     * known, such as during de-serialization
     *
     * @param proxiedBeanType the super-class for this proxy class
     * @param typeClosure     the bean types of the bean
     * @param proxyName       the name of the proxy class
     */
    public ProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName, Bean<?> bean) {
        this.bean = bean;
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
        if (bean != null) {
            /*
             * this may happen when creating an InjectionTarget for a decorator using BeanManager#createInjectionTarget()
             * which does not allow the bean to be specified
             */
            this.classLoader = resolveClassLoaderForBeanProxy(bean.getBeanClass(), typeInfo);
        } else {
            this.classLoader = resolveClassLoaderForBeanProxy(proxiedBeanType, typeInfo);
        }
        // hierarchy order
        List<Class<?>> list = new ArrayList<Class<?>>(additionalInterfaces);
        Collections.sort(list, ClassHierarchyComparator.INSTANCE);
        additionalInterfaces.clear();
        additionalInterfaces.addAll(list);
    }

    static String getProxyName(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
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
            className = createCompoundProxyName(bean, typeInfo, name) + PROXY_SUFFIX;
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
                className = createCompoundProxyName(bean, typeInfo, name) + PROXY_SUFFIX;
            } else {
                className = typeInfo.getSuperClass().getSimpleName() + PROXY_SUFFIX;
            }
        }


        return proxyPackage + '.' + className;
    }

    private static String createCompoundProxyName(Bean<?> bean, TypeInfo typeInfo, StringBuilder name) {
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
        if (bean != null) {
            final String id = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
            name.append(id.hashCode());
        }
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
        ((ProxyObject) proxy).setHandler(new ProxyMethodHandler(beanInstance, bean));
        return proxy;
    }

    /**
     * Produces or returns the existing proxy class. The operation is thread-safe.
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

    protected Class<T> getCachedProxyClass(String proxyClassName) {
        try {
            // Check to see if we already have this proxy class
            return cast(classLoader.loadClass(proxyClassName));
        } catch (ClassNotFoundException e) {
            return null;
        }
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
    public static <T> void setBeanInstance(T proxy, BeanInstance beanInstance, Bean<?> bean) {
        if (proxy instanceof ProxyObject) {
            ProxyObject proxyView = (ProxyObject) proxy;
            proxyView.setHandler(new ProxyMethodHandler(beanInstance, bean));
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
            proxyClassType = new ClassFile(proxyClassName, Object.class.getName());
            proxyClassType.addInterface(beanType.getName());
        } else {
            proxyClassType = new ClassFile(proxyClassName, beanType.getName());
        }
        // Add interfaces which require method generation
        for (Class<?> clazz : additionalInterfaces) {
            proxyClassType.addInterface(clazz.getName());
        }
        List<DeferredBytecode> initialValueBytecode = new ArrayList<DeferredBytecode>();

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
        if (proxiedBeanType.getPackage() == null || proxiedBeanType.equals(Object.class)) {
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
    protected void addConstructors(ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode) {
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
                        ConstructorUtils.addConstructor("V", DescriptorUtils.getParameterTypes(constructor.getParameterTypes()), exceptions, proxyClassType, initialValueBytecode);
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

    protected void addFields(ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode) {
        // The field representing the underlying instance or special method
        // handling
        proxyClassType.addField(AccessFlag.PRIVATE, "methodHandler", MethodHandler.class);
        // field used to indicate that super() has been called
        proxyClassType.addField(AccessFlag.PRIVATE, CONSTRUCTED_FLAG_NAME, "Z");

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
            generateEqualsMethod(proxyClassType);

            generateHashCodeMethod(proxyClassType);

            while (cls != null) {
                for (Method method : cls.getDeclaredMethods()) {
                    if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers()) && (method.getDeclaringClass() != Object.class || method.getName().equals("toString"))) {
                        try {
                            MethodInformation methodInfo = new RuntimeMethodInformation(method);
                            ClassMethod classMethod = proxyClassType.addMethod(method);
                            addConstructedGuardToMethodBody(classMethod);
                            createForwardingMethodBody(classMethod, methodInfo);
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
                        ClassMethod classMethod = proxyClassType.addMethod(method);
                        createSpecialMethodBody(classMethod, methodInfo);
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
    protected void generateHashCodeMethod(ClassFile proxyClassType) {
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
    protected void generateEqualsMethod(ClassFile proxyClassType) {

    }

    protected void createSpecialMethodBody(ClassMethod proxyClassType, MethodInformation method) {
        createInterceptorBody(proxyClassType, method);
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
    protected void addConstructedGuardToMethodBody(final ClassMethod classMethod) {
        // now create the conditional
        final CodeAttribute cond = classMethod.getCodeAttribute();
        cond.aload(0);
        cond.getfield(classMethod.getClassFile().getName(), CONSTRUCTED_FLAG_NAME, "Z");

        // jump if the proxy constructor has finished
        BranchEnd jumpMarker = cond.ifne();
        // generate the invokespecial call to the super class method
        // this is run when the proxy is being constructed
        cond.aload(0);
        cond.loadMethodParameters();
        cond.invokespecial(classMethod.getClassFile().getSuperclass(), classMethod.getName(), classMethod.getDescriptor());
        cond.returnInstruction();
        cond.branchEnd(jumpMarker);
    }

    protected void createForwardingMethodBody(ClassMethod classMethod, MethodInformation method) {
        createInterceptorBody(classMethod, method);
    }

    /**
     * Creates the given method on the proxy class where the implementation
     * forwards the call directly to the method handler.
     * <p/>
     * the generated bytecode is equivalent to:
     * <p/>
     * return (RetType) methodHandler.invoke(this,param1,param2);
     *
     * @param classMethod the class method
     * @param method      any JLR method
     * @return the method byte code
     */
    protected void createInterceptorBody(ClassMethod classMethod, MethodInformation method) {
        invokeMethodHandler(classMethod, method, true, DEFAULT_METHOD_RESOLVER);
    }

    /**
     * calls methodHandler.invoke for a given method
     *
     * @param method                 The method information
     * @param addReturnInstruction   set to true you want to return the result of
     *                               the method invocation
     * @param bytecodeMethodResolver The resolver that returns the method to invoke
     */
    protected static void invokeMethodHandler(ClassMethod classMethod, MethodInformation method, boolean addReturnInstruction, BytecodeMethodResolver bytecodeMethodResolver) {
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
        final CodeAttribute b = classMethod.getCodeAttribute();
        b.aload(0);
        b.getfield(classMethod.getClassFile().getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
        b.aload(0);
        bytecodeMethodResolver.getDeclaredMethod(classMethod, method.getDeclaringClass(), method.getName(), method.getParameterTypes());
        b.aconstNull();

        b.iconst(method.getParameterTypes().length);
        b.anewarray("java.lang.Object");

        int localVariableCount = 1;

        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            String typeString = method.getParameterTypes()[i];
            b.dup(); // duplicate the array reference
            b.iconst(i);
            // load the parameter value
            BytecodeUtils.addLoadInstruction(b, typeString, localVariableCount);
            // box the parameter if nessesary
            Boxing.boxIfNessesary(b, typeString);
            // and store it in the array
            b.aastore();
            if (DescriptorUtils.isWide(typeString)) {
                localVariableCount = localVariableCount + 2;
            } else {
                localVariableCount++;
            }
        }
        // now we have all our arguments on the stack
        // lets invoke the method
        b.invokeinterface(MethodHandler.class.getName(), "invoke", "Ljava/lang/Object;", new String[]{"Ljava/lang/Object;", "Ljava/lang/reflect/Method;", "Ljava/lang/reflect/Method;", "[Ljava/lang/Object;"});
        if (addReturnInstruction) {
            // now we need to return the appropriate type
            if (method.getReturnType().equals("V") ) {
                b.returnInstruction();
            } else if(DescriptorUtils.isPrimitive(method.getReturnType())) {
                Boxing.unbox(b, method.getReturnType());
                b.returnInstruction();
            } else {
                String castType = method.getReturnType();
                if (!method.getReturnType().startsWith("[")) {
                    castType = method.getReturnType().substring(1).substring(0, method.getReturnType().length() - 2);
                }
                b.checkcast(castType);
                b.returnInstruction();
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
                final ClassMethod classMethod = proxyClassType.addMethod(method);
                createInterceptorBody(classMethod, methodInfo);
            }
            Method getInstanceMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetInstance");
            Method getInstanceClassMethod = TargetInstanceProxy.class.getDeclaredMethod("getTargetClass");

            MethodInformation getInstanceMethodInfo = new RuntimeMethodInformation(getInstanceMethod);
            createInterceptorBody(proxyClassType.addMethod(getInstanceMethod), getInstanceMethodInfo);


            MethodInformation getInstanceClassMethodInfo = new RuntimeMethodInformation(getInstanceClassMethod);
            createInterceptorBody(proxyClassType.addMethod(getInstanceClassMethod), getInstanceClassMethodInfo);

            Method setMethodHandlerMethod = ProxyObject.class.getDeclaredMethod("setHandler", MethodHandler.class);
            generateSetMethodHandlerBody(proxyClassType.addMethod(setMethodHandlerMethod));
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    private static void generateSetMethodHandlerBody(ClassMethod method) {
        final CodeAttribute b = method.getCodeAttribute();
        b.aload(0);
        b.aload(1);
        b.putfield(method.getClassFile().getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
        b.returnInstruction();
    }


    /**
     * Adds two constructors to the class that call each other in order to bypass
     * the JVM class file verifier.
     * <p/>
     * This would result in a stack overflow if they were actually called,
     * however the proxy is directly created without calling the constructor
     */
    private void addConstructorsForBeanWithPrivateConstructors(ClassFile proxyClassType) {
        ClassMethod ctor = proxyClassType.addMethod(AccessFlag.PUBLIC, "<init>", "V", "Ljava/lang/Byte;");
        CodeAttribute b = ctor.getCodeAttribute();
        b.aload(0);
        b.aconstNull();
        b.aconstNull();
        b.invokespecial(proxyClassType.getName(), "<init>", "(Ljava/lang/Byte;Ljava/lang/Byte;)V");
        b.returnInstruction();

        ctor = proxyClassType.addMethod(AccessFlag.PUBLIC, "<init>", "V", "Ljava/lang/Byte;", "Ljava/lang/Byte;");
        b = ctor.getCodeAttribute();
        b.aload(0);
        b.aconstNull();
        b.invokespecial(proxyClassType.getName(), "<init>", "(Ljava/lang/Byte;)V");
        b.returnInstruction();
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

    /**
     * Figures out the correct class loader to use for a proxy for a given bean
     */
    public static ClassLoader resolveClassLoaderForBeanProxy(Class<?> proxiedType, TypeInfo typeInfo) {
        Class<?> superClass = typeInfo.getSuperClass();
        if (superClass.getName().startsWith("java")) {
            ClassLoader cl = Container.instance().services().get(ProxyServices.class).getClassLoader(proxiedType);
            if (cl == null) {
                cl = Thread.currentThread().getContextClassLoader();
            }
            return cl;
        }
        return Container.instance().services().get(ProxyServices.class).getClassLoader(superClass);
    }

//    public static ClassLoader resolveClassLoaderForBeanProxy(Bean<?> bean) {
//        return resolveClassLoaderForBeanProxy(bean, TypeInfo.of(bean.getTypes()));
//    }

}
