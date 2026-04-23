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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.LifecycleMixin;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.proxy.WeldConstruct;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.bytecode.ConstructorUtils;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.collections.Sets;
import org.jboss.weld.util.reflection.Reflections;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

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

    // Default proxy class name suffix
    public static final String PROXY_SUFFIX = "$Proxy$";
    // choose different package from what we have in tests to distinguish it clearly
    public static final String WELD_PROXY_PREFIX = "org.jboss.weld.generated.proxies";
    public static final String DEFAULT_PROXY_PACKAGE = WELD_PROXY_PREFIX + ".default";
    public static final String CONSTRUCTED_FLAG_NAME = "constructed";
    protected static final String INVOKE_METHOD_NAME = "invoke";
    protected static final String METHOD_HANDLER_FIELD_NAME = "methodHandler";
    static final String JAVA = "java";
    static final String JAKARTA = "jakarta";
    static final String NO_PACKAGE = "the class package is null or empty";
    static final String SIGNED = "the class is signed";
    private static final Set<ProxiedMethodFilter> METHOD_FILTERS;

    static {
        Set<ProxiedMethodFilter> filters = new HashSet<>();
        filters.add(CommonProxiedMethodFilters.NON_STATIC);
        filters.add(CommonProxiedMethodFilters.NON_FINAL);
        filters.add(CommonProxiedMethodFilters.OBJECT_TO_STRING);
        filters.add(CommonProxiedMethodFilters.NON_JDK_PACKAGE_PRIVATE);
        GroovyMethodFilter groovy = new GroovyMethodFilter();
        if (groovy.isEnabled()) {
            filters.add(groovy);
        }
        METHOD_FILTERS = ImmutableSet.copyOf(filters);
    }

    private final Class<?> beanType;
    private final Set<Class<?>> additionalInterfaces = new LinkedHashSet<Class<?>>();
    private final String baseProxyName;
    private final Bean<?> bean;
    private final Class<?> proxiedBeanType;
    private final String contextId;
    private final ProxyServices proxyServices;
    private final WeldConfiguration configuration;
    private final ProxyInstantiator proxyInstantiator;

    /**
     * created a new proxy factory from a bean instance. The proxy name is
     * generated from the bean id
     */
    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        this(contextId, proxiedBeanType, typeClosure, bean, false);
    }

    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean,
            boolean forceSuperClass) {
        this(contextId, proxiedBeanType, typeClosure, getProxyName(contextId, proxiedBeanType, typeClosure, bean), bean,
                forceSuperClass);
    }

    /**
     * Creates a new proxy factory when the name of the proxy class is already
     * known, such as during de-serialization
     *
     * @param proxiedBeanType the super-class for this proxy class
     * @param typeClosure the bean types of the bean
     * @param proxyName the name of the proxy class
     */
    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName,
            Bean<?> bean) {
        this(contextId, proxiedBeanType, typeClosure, proxyName, bean, false);
    }

    public ProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName,
            Bean<?> bean, boolean forceSuperClass) {
        this.bean = bean;
        this.contextId = contextId;
        this.proxiedBeanType = proxiedBeanType;
        this.configuration = Container.instance(contextId).deploymentManager().getServices().get(WeldConfiguration.class);
        addInterfacesFromTypeClosure(typeClosure, proxiedBeanType);
        TypeInfo typeInfo = TypeInfo.of(typeClosure);
        Class<?> superClass = typeInfo.getSuperClass();
        superClass = superClass == null ? Object.class : superClass;
        if (forceSuperClass || (superClass.equals(Object.class) && additionalInterfaces.isEmpty())) {
            // No interface beans, must use the bean impl as superclass
            superClass = proxiedBeanType;
        }
        this.beanType = superClass;

        addDefaultAdditionalInterfaces();
        baseProxyName = proxyName;
        proxyServices = Container.instance(contextId).services().get(ProxyServices.class);
        // hierarchy order
        if (additionalInterfaces.size() > 1) {
            LinkedHashSet<Class<?>> sorted = Proxies.sortInterfacesHierarchy(additionalInterfaces);
            additionalInterfaces.clear();
            additionalInterfaces.addAll(sorted);
        }

        this.proxyInstantiator = Container.instance(contextId).services().get(ProxyInstantiator.class);
    }

    static String getProxyName(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        TypeInfo typeInfo = TypeInfo.of(typeClosure);
        final String className;
        ProxyNameHolder holder;
        if (typeInfo.getSuperClass() == Object.class) {
            final StringBuilder name = new StringBuilder();
            //interface only bean.
            holder = createCompoundProxyName(contextId, bean, typeInfo, name);
        } else {
            boolean typeModified = false;
            for (Class<?> iface : typeInfo.getInterfaces()) {
                if (!iface.isAssignableFrom(typeInfo.getSuperClass())) {
                    typeModified = true;
                    break;
                }
            }
            if (typeModified) {
                // This bean has interfaces that the base type is not assignable to.
                // One example of this is an EJB bean using @Local and declaring an interface it doesn't implement.
                // Another case is a CDI bean with type added via ProcessBeanAttributes which isn't directly implemented.
                StringBuilder name = new StringBuilder(typeInfo.getSuperClass().getSimpleName() + "$");
                holder = createCompoundProxyName(contextId, bean, typeInfo, name, bean.getBeanClass().getPackage().getName());
            } else {
                holder = new ProxyNameHolder(null, typeInfo.getSuperClass().getSimpleName(), bean);
            }
        }
        className = holder.getClassName() + PROXY_SUFFIX;
        String proxyPackage = holder.getPackageName();
        if (proxiedBeanType.equals(Object.class)) {
            Class<?> superInterface = typeInfo.getSuperInterface();
            if (superInterface == null) {
                throw new IllegalArgumentException("Proxied bean type cannot be java.lang.Object without an interface");
            } else {
                String reason = getDefaultPackageReason(superInterface);
                if (reason != null) {
                    proxyPackage = DEFAULT_PROXY_PACKAGE;
                    BeanLogger.LOG.generatingProxyToDefaultPackage(superInterface, DEFAULT_PROXY_PACKAGE, reason);
                }
            }
        } else {
            String reason = getDefaultPackageReason(proxiedBeanType);
            if (reason != null && reason.equals(NO_PACKAGE)) {
                proxyPackage = DEFAULT_PROXY_PACKAGE;
                BeanLogger.LOG.generatingProxyToDefaultPackage(proxiedBeanType, DEFAULT_PROXY_PACKAGE, reason);
            } else {
                if (proxyPackage == null) {
                    proxyPackage = proxiedBeanType.getPackage().getName();
                }
            }
        }
        return proxyPackage + '.' + getEnclosingPrefix(proxiedBeanType) + className;
    }

    private static ProxyNameHolder createCompoundProxyName(String contextId, Bean<?> bean, TypeInfo typeInfo,
            StringBuilder name) {
        return createCompoundProxyName(contextId, bean, typeInfo, name, null);
    }

    private static ProxyNameHolder createCompoundProxyName(String contextId, Bean<?> bean, TypeInfo typeInfo,
            StringBuilder name, String knownProxyPackage) {
        String className;
        String proxyPackage = knownProxyPackage;
        // we need a sorted collection without repetition, hence LinkedHashSet
        final Set<String> interfaces = new LinkedHashSet<>();
        // for producers, try to determine the most specific class and make sure the proxy starts with the same package and class
        if (bean != null && bean instanceof AbstractProducerBean) {
            Class<?> mostSpecificClass = ((AbstractProducerBean) bean).getType();
            // for producers, always override the proxy package
            proxyPackage = mostSpecificClass.getPackage().getName();
            if (mostSpecificClass.getDeclaringClass() != null) {
                interfaces.add(mostSpecificClass.getDeclaringClass().getSimpleName());
            }
            interfaces.add(mostSpecificClass.getSimpleName());
        }
        // if the bean class is a non-public one (i.e. pack private), we prioritize placing proxy in the same package
        // we skip built-in beans are those are often for jakarta.* classes and end up in Weld's default package anyway
        if (proxyPackage == null && bean != null
                && !Modifier.isPublic(bean.getBeanClass().getModifiers())
                && !(bean instanceof AbstractBuiltInBean)) {
            proxyPackage = bean.getBeanClass().getPackage().getName();
        }
        final Set<String> declaringClasses = new HashSet<>();
        for (Class<?> type : typeInfo.getInterfaces()) {
            Class<?> declaringClass = type.getDeclaringClass();
            if (declaringClass != null && declaringClasses.add(declaringClass.getSimpleName())) {
                interfaces.add(declaringClass.getSimpleName());
            }
            interfaces.add(type.getSimpleName());
            if (proxyPackage == null) {
                proxyPackage = typeInfo.getPackageNameForClass(type);
            }
        }
        // no need to sort the set, because we copied already sorted set
        Iterator<String> iterator = interfaces.iterator();
        while (iterator.hasNext()) {
            name.append(iterator.next());
            if (iterator.hasNext()) {
                name.append("$");
            }

        }
        //there is a remote chance that this could generate the same
        //proxy name for two interfaces with the same simple name.
        //append the hash code of the bean id to be sure
        // However, it is safe to share a proxy class for built-in beans of the same type (e.g. Event)
        if (bean != null && !(bean instanceof AbstractBuiltInBean)) {
            final BeanIdentifier id = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
            int idHash = id.hashCode();
            // add a separator so that WeldDefaultProxyServices can determine the correct full class name by first occurrence of "$"
            name.append("$");
            name.append(Math.abs(idHash == Integer.MIN_VALUE ? 0 : idHash));
        }
        className = name.toString();
        return new ProxyNameHolder(proxyPackage, className, bean);
    }

    private static String getEnclosingPrefix(Class<?> clazz) {
        Class<?> encl = clazz.getEnclosingClass();
        return encl == null ? "" : getEnclosingPrefix(encl) + encl.getSimpleName() + '$';
    }

    /**
     * Convenience method to set the underlying bean instance for a proxy.
     *
     * @param proxy the proxy instance
     * @param beanInstance the instance of the bean
     */
    public static <T> void setBeanInstance(String contextId, T proxy, BeanInstance beanInstance, Bean<?> bean) {
        if (proxy instanceof ProxyObject) {
            ProxyObject proxyView = (ProxyObject) proxy;
            proxyView.weld_setHandler(new ProxyMethodHandler(contextId, beanInstance, bean));
        }
    }

    private static String getDefaultPackageReason(Class<?> clazz) {
        if (clazz.getPackage() == null || clazz.getPackage().getName().isEmpty()) {
            return NO_PACKAGE;
        }
        if (clazz.getSigners() != null) {
            return SIGNED;
        }
        return null;
    }

    public void addInterfacesFromTypeClosure(Set<? extends Type> typeClosure, Class<?> proxiedBeanType) {
        for (Type type : typeClosure) {
            Class<?> c = Reflections.getRawType(type);
            // Ignore no-interface views, they are dealt with proxiedBeanType
            // (pending redesign)
            if (c.isInterface()) {
                addInterface(c);
            }
        }
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
        final T proxy = instantiateProxy();
        ((ProxyObject) proxy).weld_setHandler(new ProxyMethodHandler(contextId, beanInstance, bean));
        return proxy;
    }

    public T instantiateProxy() {
        try {
            Class<T> proxyClass = getProxyClass();
            boolean hasConstrutedField = Reflections.hasDeclaredField(proxyClass, CONSTRUCTED_FLAG_NAME);
            if (hasConstrutedField != useConstructedFlag()) {
                // The proxy class was created with a different type of ProxyInstantiator
                ProxyInstantiator newInstantiator = ProxyInstantiator.Factory.create(!hasConstrutedField);
                BeanLogger.LOG.creatingProxyInstanceUsingDifferentInstantiator(proxyClass, newInstantiator, proxyInstantiator);
                return newInstantiator.newInstance(proxyClass);
            }
            return proxyInstantiator.newInstance(proxyClass);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new DefinitionException(BeanLogger.LOG.proxyInstantiationFailed(this), e.getCause());
        } catch (IllegalAccessException e) {
            throw new DefinitionException(BeanLogger.LOG.proxyInstantiationBeanAccessFailed(this), e.getCause());
        }
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
        if (proxyClassName.startsWith(JAVA)) {
            proxyClassName = proxyClassName.replaceFirst(JAVA, WELD_PROXY_PREFIX);
        } else if (proxyClassName.startsWith(JAKARTA)) {
            proxyClassName = proxyClassName.replaceFirst(JAKARTA, WELD_PROXY_PREFIX);
        }
        Class<T> proxyClass = null;
        Class<?> originalClass = bean != null ? bean.getBeanClass() : proxiedBeanType;
        BeanLogger.LOG.generatingProxyClass(proxyClassName);
        try {
            // First check to see if we already have this proxy class
            proxyClass = cast(proxyServices.loadClass(originalClass, proxyClassName));
        } catch (ClassNotFoundException e) {
            // Create the proxy class for this instance
            try {
                proxyClass = createProxyClass(originalClass, proxyClassName);
            } catch (Throwable e1) {
                //attempt to load the class again, just in case another thread
                //defined it between the check and the create method
                try {
                    proxyClass = cast(proxyServices.loadClass(originalClass, proxyClassName));
                } catch (ClassNotFoundException e2) {
                    BeanLogger.LOG.catchingDebug(e1);
                    throw BeanLogger.LOG.unableToLoadProxyClass(bean, proxiedBeanType, e1);
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
        // add a marker interface denoting that the resulting class uses weld internal contructs
        additionalInterfaces.add(WeldConstruct.class);
    }

    /**
     * Sub classes may override to specify additional interfaces the proxy should
     * implement
     */
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {

    }

    private Class<T> createProxyClass(Class<?> originalClass, String proxyClassName) throws Exception {
        Set<Class<?>> specialInterfaces = Sets.newHashSet(LifecycleMixin.class, TargetInstanceProxy.class, ProxyObject.class);
        addAdditionalInterfaces(specialInterfaces);
        // Remove special interfaces from main set (deserialization scenario)
        additionalInterfaces.removeAll(specialInterfaces);

        List<DeferredBytecode> initialValueBytecode = new ArrayList<DeferredBytecode>();

        // Create ByteArrayClassOutput to capture generated bytecode
        ByteArrayClassOutput classOutput = new ByteArrayClassOutput();
        Gizmo gizmo = Gizmo.create(classOutput);

        // Generate proxy class using Gizmo 2
        gizmo.class_(proxyClassName, cc -> {
            // Set modifiers (public, synthetic)
            cc.public_();
            cc.synthetic();

            // Set superclass
            if (getBeanType().isInterface()) {
                cc.extends_(Object.class);
                cc.implements_(getBeanType());
            } else {
                cc.extends_(getBeanType());
            }

            // Add interfaces which require method generation
            for (Class<?> clazz : additionalInterfaces) {
                cc.implements_(clazz);
            }

            // Additional interfaces whose methods require special handling
            for (Class<?> specialInterface : specialInterfaces) {
                cc.implements_(specialInterface);
            }

            // Add fields
            addFields(cc, initialValueBytecode);

            // Add constructors
            addConstructors(cc, initialValueBytecode);

            // Add methods (includes static initializer)
            addMethods(cc);

            // Add serialization support
            addSerializationSupport(cc);
        });

        // Get generated bytecode
        byte[] bytecode = classOutput.getBytes();

        if (bytecode == null) {
            throw new WeldException("Failed to generate proxy class: " + proxyClassName);
        }

        // Dump proxy type bytecode if necessary
        dumpToFile(proxyClassName, bytecode);

        ProtectionDomain domain = proxiedBeanType.getProtectionDomain();

        if (proxiedBeanType.getPackage() == null || proxiedBeanType.getPackage().getName().isEmpty()
                || proxiedBeanType.equals(Object.class)) {
            domain = ProxyFactory.class.getProtectionDomain();
        }

        Class<T> proxyClass = cast(toClass(bytecode, proxyClassName, originalClass, proxyServices, domain));
        BeanLogger.LOG.createdProxyClass(proxyClass, Arrays.toString(proxyClass.getInterfaces()));
        return proxyClass;
    }

    private void dumpToFile(String fileName, byte[] data) {
        File proxyDumpFilePath = configuration.getProxyDumpFilePath();
        if (proxyDumpFilePath == null) {
            return;
        }
        File dumpFile = new File(proxyDumpFilePath, fileName + ".class");
        try {
            Files.write(dumpFile.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            BeanLogger.LOG.beanCannotBeDumped(fileName, e);
        }
    }

    /**
     * Adds constructors to the proxy class using Gizmo 2 API.
     *
     * @param cc the class creator
     * @param initialValueBytecode deferred bytecode for field initialization
     */
    protected void addConstructors(ClassCreator cc, List<DeferredBytecode> initialValueBytecode) {
        try {
            if (getBeanType().isInterface()) {
                // Interface-based proxy: add default constructor calling Object()
                ConstructorUtils.addDefaultConstructor(cc, Object.class, initialValueBytecode, !useConstructedFlag());
            } else {
                // Class-based proxy: mirror all non-private constructors from the bean type
                boolean constructorFound = false;
                for (Constructor<?> constructor : getBeanType().getDeclaredConstructors()) {
                    if ((constructor.getModifiers() & Modifier.PRIVATE) == 0) {
                        constructorFound = true;
                        Class<?>[] paramTypes = constructor.getParameterTypes();
                        Class<?>[] exceptionTypes = constructor.getExceptionTypes();

                        ConstructorUtils.addConstructor(cc, getBeanType(), paramTypes, exceptionTypes,
                                initialValueBytecode, !useConstructedFlag());
                    }
                }
                if (!constructorFound) {
                    // the bean only has private constructors, we need to generate
                    // two fake constructors that call each other
                    addConstructorsForBeanWithPrivateConstructors(cc);
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Adds fields to the proxy class using Gizmo 2 API.
     *
     * @param cc the class creator
     * @param initialValueBytecode deferred bytecode for field initialization
     */
    protected void addFields(ClassCreator cc, List<DeferredBytecode> initialValueBytecode) {
        // The field representing the underlying instance or special method handling
        cc.field(METHOD_HANDLER_FIELD_NAME, f -> {
            f.setType(getMethodHandlerType());
            f.private_();
        });

        if (useConstructedFlag()) {
            // field used to indicate that super() has been called
            cc.field(CONSTRUCTED_FLAG_NAME, f -> {
                f.setType(boolean.class);
                f.private_();
            });
        }
    }

    protected Class<? extends MethodHandler> getMethodHandlerType() {
        return MethodHandler.class;
    }

    /**
     * Adds special serialization code using Gizmo 2 API. By default this is a nop
     *
     * @param cc the class creator
     */
    protected void addSerializationSupport(ClassCreator cc) {
        //noop
    }

    /**
     * Adds all methods to the proxy class using Gizmo 2 API.
     *
     * @param cc the class creator
     */
    protected void addMethods(ClassCreator cc) {
        try {
            // Collect all methods that need to be proxied
            List<MethodInfo> methodsToProxy = collectMethodsToProxy();

            // Add static fields for Method reflection objects
            Map<MethodInfo, String> methodFieldNames = new HashMap<>();
            for (MethodInfo methodInfo : methodsToProxy) {
                String fieldName = "weld$$$method$$$" + methodFieldNames.size();
                methodFieldNames.put(methodInfo, fieldName);

                cc.staticField(fieldName, f -> {
                    f.setType(Method.class);
                    f.private_();
                });
            }

            // Add static initializer to populate Method fields
            if (!methodsToProxy.isEmpty()) {
                addStaticInitializer(cc, methodsToProxy, methodFieldNames);
            }

            // Add methods from class hierarchy
            addMethodsFromClass(cc, methodsToProxy, methodFieldNames);

            // Add special proxy methods
            addSpecialMethods(cc);

            // Note: Serialization support is added separately via addSerializationSupport(cc)
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Simple holder for method information during proxy generation.
     */
    protected static class MethodInfo {
        final Method method;
        final boolean isDefault;

        MethodInfo(Method method, boolean isDefault) {
            this.method = method;
            this.isDefault = isDefault;
        }
    }

    /**
     * Collects all methods that need to be proxied.
     */
    protected List<MethodInfo> collectMethodsToProxy() {
        List<MethodInfo> methods = new ArrayList<>();
        Class<?> cls = getBeanType();
        boolean isBeanClassAbstract = Modifier.isAbstract(cls.getModifiers());
        Set<MethodSignature> foundFinalMethods = new HashSet<>();
        Set<MethodSignature> addedMethods = new HashSet<>();

        // Add methods from the class hierarchy
        while (cls != null) {
            Method[] classDeclaredMethods = cls.getDeclaredMethods();
            for (Method method : classDeclaredMethods) {
                MethodSignature methodSignature = new MethodSignatureImpl(method);
                if (Modifier.isFinal(method.getModifiers())) {
                    foundFinalMethods.add(methodSignature);
                }
                // Skip bridge methods that have a concrete implementation in the same class
                if (method.isBridge() && hasConcreteImplementation(method, classDeclaredMethods)) {
                    continue;
                }
                if (isMethodAccepted(method, getProxySuperclass())
                        && !foundFinalMethods.contains(methodSignature)
                        && !addedMethods.contains(methodSignature)) {
                    methods.add(new MethodInfo(method, false));
                    addedMethods.add(methodSignature);
                }
            }
            if (isBeanClassAbstract && Modifier.isAbstract(cls.getModifiers())) {
                for (Class<?> implementedInterface : Reflections.getInterfaceClosure(cls)) {
                    if (!additionalInterfaces.contains(implementedInterface)) {
                        for (Method method : implementedInterface.getMethods()) {
                            MethodSignature methodSignature = new MethodSignatureImpl(method);
                            if (isMethodAccepted(method, getProxySuperclass()) && !addedMethods.contains(methodSignature)) {
                                methods.add(new MethodInfo(method, Reflections.isDefault(method)));
                                addedMethods.add(methodSignature);
                            }
                        }
                    }
                }
            }
            cls = cls.getSuperclass();
        }

        // Add methods from additional interfaces
        for (Class<?> iface : additionalInterfaces) {
            for (Method method : iface.getMethods()) {
                MethodSignature methodSignature = new MethodSignatureImpl(method);
                if (isMethodAccepted(method, getProxySuperclass()) && !addedMethods.contains(methodSignature)) {
                    methods.add(new MethodInfo(method, Reflections.isDefault(method)));
                    addedMethods.add(methodSignature);
                }
            }
        }

        return methods;
    }

    /**
     * Adds a static initializer that populates Method reflection fields.
     */
    protected void addStaticInitializer(ClassCreator cc, List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {
        cc.staticMethod("<clinit>", m -> {
            m.returning(void.class);

            m.body(b -> {
                for (MethodInfo methodInfo : methodsToProxy) {
                    String fieldName = methodFieldNames.get(methodInfo);
                    Method method = methodInfo.method;

                    // Get the declaring class: Class<?> declaringClass = <DeclaringClass>.class
                    Expr classExpr = Const.of(method.getDeclaringClass());

                    // Get the method name: String methodName = "methodName"
                    Expr methodNameExpr = Const.of(method.getName());

                    // Create parameter types array: Class<?>[] paramTypes = new Class<?>[] { ... }
                    Class<?>[] paramTypes = method.getParameterTypes();

                    Expr paramTypesArray;
                    if (paramTypes.length == 0) {
                        paramTypesArray = b.newEmptyArray(Class.class, 0);
                    } else {
                        // Create array, store in LocalVar immediately, then populate
                        Expr arrayExpr = b.newEmptyArray(Class.class, paramTypes.length);
                        var paramTypesVar = b.localVar("paramTypes_" + fieldName, arrayExpr);

                        for (int i = 0; i < paramTypes.length; i++) {
                            // Use Const.of(Class) to properly handle arrays and primitives
                            Expr paramClassExpr = Const.of(paramTypes[i]);
                            b.set(paramTypesVar.elem(i), paramClassExpr);
                        }
                        paramTypesArray = paramTypesVar;
                    }

                    // Call Class.getDeclaredMethod(methodName, paramTypes)
                    MethodDesc getDeclaredMethodDesc = MethodDesc.of(
                            Class.class,
                            "getDeclaredMethod",
                            Method.class,
                            String.class,
                            Class[].class);

                    Expr methodExpr = b.invokeVirtual(getDeclaredMethodDesc, classExpr,
                            methodNameExpr, paramTypesArray);

                    // Store in static field: <ProxyClass>.fieldName = method
                    FieldDesc fieldDesc = FieldDesc.of(
                            cc.type(),
                            fieldName,
                            Method.class);
                    b.setStaticField(fieldDesc, methodExpr);
                }

                b.return_();
            });
        });
    }

    /**
     * Adds methods from the class hierarchy to the proxy.
     */
    protected void addMethodsFromClass(ClassCreator cc, List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {
        // Always generate equals/hashCode methods
        // We override any bean implementations to ensure proxy identity semantics
        generateEqualsMethod(cc);
        generateHashCodeMethod(cc);

        for (MethodInfo methodInfo : methodsToProxy) {
            // Skip equals/hashCode - we always generate our own versions
            if (methodInfo.method.getName().equals("equals") && methodInfo.method.getParameterCount() == 1
                    && methodInfo.method.getParameterTypes()[0] == Object.class) {
                continue;
            }
            if (methodInfo.method.getName().equals("hashCode") && methodInfo.method.getParameterCount() == 0) {
                continue;
            }
            addProxyMethod(cc, methodInfo, methodFieldNames.get(methodInfo));
        }
    }

    /**
     * Generates equals() method that compares proxy classes.
     * Two proxies are equal if they have the same class.
     */
    protected void generateEqualsMethod(ClassCreator cc) {
        cc.method("equals", m -> {
            m.public_();
            m.returning(boolean.class);
            var otherParam = m.parameter("other", Object.class);

            m.body(b -> {
                // if (other == null) return false;
                Expr nullCheck = b.eq(otherParam, Const.ofNull(Object.class));
                b.if_(nullCheck, nullBlock -> {
                    nullBlock.return_(Const.of(false));
                });

                // return this.getClass().equals(other.getClass());
                Expr thisClass = b.invokeVirtual(
                        MethodDesc.of(Object.class, "getClass", Class.class),
                        m.this_());
                Expr otherClass = b.invokeVirtual(
                        MethodDesc.of(Object.class, "getClass", Class.class),
                        otherParam);
                Expr result = b.invokeVirtual(
                        MethodDesc.of(Object.class, "equals", boolean.class, Object.class),
                        thisClass, otherClass);
                b.return_(result);
            });
        });
    }

    /**
     * Generates hashCode() method that returns the proxy class hashCode.
     */
    protected void generateHashCodeMethod(ClassCreator cc) {
        cc.method("hashCode", m -> {
            m.public_();
            m.returning(int.class);

            m.body(b -> {
                // return this.getClass().hashCode();
                Expr thisClass = b.invokeVirtual(
                        MethodDesc.of(Object.class, "getClass", Class.class),
                        m.this_());
                Expr hashCode = b.invokeVirtual(
                        MethodDesc.of(Object.class, "hashCode", int.class),
                        thisClass);
                b.return_(hashCode);
            });
        });
    }

    /**
     * Adds a single proxy method that forwards to the method handler.
     */
    protected void addProxyMethod(ClassCreator cc, MethodInfo methodInfo, String methodFieldName) {
        Method method = methodInfo.method;

        // Create method descriptor from the reflection Method
        MethodDesc methodDesc = MethodDesc.of(method);

        cc.method(methodDesc, m -> {
            // Set method modifiers
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers)) {
                m.public_();
            } else if (Modifier.isProtected(modifiers)) {
                m.protected_();
            }

            // Set varargs flag if the method is varargs
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            ParamVar[] params = new ParamVar[method.getParameterCount()];
            for (int i = 0; i < method.getParameterCount(); i++) {
                params[i] = m.parameter("arg" + i, method.getParameterTypes()[i]);
            }

            // Add checked exceptions
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableClass = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableClass);
            }

            m.body(b -> {
                // Add constructed guard if needed (prevents delegation before constructor completes)
                if (useConstructedFlag()) {
                    addConstructedGuard(m, b, method, params);
                }

                // Forward to method handler: methodHandler.invoke(this, method, null, args)
                invokeMethodHandler(m, b, method, methodFieldName, params);
            });
        });

        BeanLogger.LOG.addingMethodToProxy(method);
    }

    /**
     * Adds a constructed guard that prevents method delegation before constructor completes.
     * Generates: if (!this.constructed) return super.method(args);
     */
    protected void addConstructedGuard(InstanceMethodCreator m,
            BlockCreator b, Method method, ParamVar[] params) {

        // Load the constructed flag: this.constructed
        FieldDesc constructedField = FieldDesc.of(
                m.owner(),
                CONSTRUCTED_FLAG_NAME,
                boolean.class);
        Expr constructedValue = b.get(m.this_().field(constructedField));

        // Create a local variable to store the value (needed for cross-scope usage)
        var constructedVar = b.localVar("constructed", constructedValue);

        // Create boolean expression: constructed == false
        Expr condition = b.eq(constructedVar, Const.of(false));

        // if (!constructed) { call super and return }
        b.if_(condition, falseBlock -> {
            // Inside the if block: call super.method(args) and return

            // Prepare parameter expressions
            Expr[] paramExprs = new Expr[params.length];
            for (int i = 0; i < params.length; i++) {
                paramExprs[i] = params[i];
            }

            // Get the method's declaring class
            Class<?> declaringClass = method.getDeclaringClass();

            // For interface methods (non-default), we can't use invokeSpecial
            // Return default values instead
            if (declaringClass.isInterface() || getBeanType().isInterface()) {
                // If the method is from an interface, there's no super implementation we can call
                // Just return default value
                if (method.getReturnType() == void.class) {
                    falseBlock.return_();
                } else if (method.getReturnType().isPrimitive()) {
                    falseBlock.return_(getDefaultPrimitiveValue(falseBlock, method.getReturnType()));
                } else {
                    falseBlock.return_(Const.ofNull(Object.class));
                }
                return;
            }

            // Call super.method(args) - only valid for concrete class methods
            MethodDesc superMethodDesc = MethodDesc.of(method);

            Expr result;
            if (paramExprs.length == 0) {
                result = falseBlock.invokeSpecial(superMethodDesc, m.this_());
            } else if (paramExprs.length == 1) {
                result = falseBlock.invokeSpecial(superMethodDesc, m.this_(), paramExprs[0]);
            } else if (paramExprs.length == 2) {
                result = falseBlock.invokeSpecial(superMethodDesc, m.this_(), paramExprs[0], paramExprs[1]);
            } else {
                result = falseBlock.invokeSpecial(superMethodDesc, m.this_(), paramExprs);
            }

            if (method.getReturnType() == void.class) {
                falseBlock.return_();
            } else {
                falseBlock.return_(result);
            }
        });
        // If constructed == true, execution continues to method handler invocation
    }

    /**
     * Returns the default value for a primitive type.
     */
    protected Expr getDefaultPrimitiveValue(BlockCreator b,
            Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return Const.of(false);
        } else if (primitiveType == byte.class) {
            return Const.of((byte) 0);
        } else if (primitiveType == short.class) {
            return Const.of((short) 0);
        } else if (primitiveType == int.class) {
            return Const.of(0);
        } else if (primitiveType == long.class) {
            return Const.of(0L);
        } else if (primitiveType == float.class) {
            return Const.of(0.0f);
        } else if (primitiveType == double.class) {
            return Const.of(0.0);
        } else if (primitiveType == char.class) {
            return Const.of((char) 0);
        } else {
            throw new IllegalArgumentException("Unknown primitive type: " + primitiveType);
        }
    }

    /**
     * Invokes the method handler: methodHandler.invoke(this, staticMethodField, null, args)
     */
    protected void invokeMethodHandler(InstanceMethodCreator m,
            BlockCreator b, Method method, String methodFieldName,
            ParamVar[] params) {

        // 1. Load this.methodHandler
        FieldDesc methodHandlerField = FieldDesc.of(
                m.owner(),
                METHOD_HANDLER_FIELD_NAME,
                getMethodHandlerType());
        Expr handler = b.get(m.this_().field(methodHandlerField));

        // 2. Load the static Method field
        FieldDesc methodField = FieldDesc.of(
                m.owner(),
                methodFieldName,
                Method.class);
        Expr methodObj = Expr.staticField(methodField);

        // 3. Create null for the second Method parameter (not used in ProxyFactory)
        Expr nullMethod = Const.ofNull(Method.class);

        // 4. Create and populate Object[] args array
        Class<?>[] paramTypes = method.getParameterTypes();

        // If no parameters, create empty array and store directly
        Expr argsArray;
        if (paramTypes.length == 0) {
            argsArray = b.newEmptyArray(Object.class, 0);
        } else {
            // For parameters, create array, store in LocalVar immediately, then populate
            Expr arrayExpr = b.newEmptyArray(Object.class, paramTypes.length);
            var argsVar = b.localVar("args", arrayExpr);

            for (int i = 0; i < paramTypes.length; i++) {
                Expr paramValue = params[i];
                // Box primitive types
                if (paramTypes[i].isPrimitive()) {
                    paramValue = boxPrimitive(b, paramValue, paramTypes[i]);
                }
                b.set(argsVar.elem(i), paramValue);
            }
            argsArray = argsVar;
        }

        // 5. Call methodHandler.invoke(this, method, null, args)
        // MethodHandler.invoke(Object self, Method thisMethod, Method proceed, Object[] args)
        MethodDesc invokeDesc = MethodDesc.of(
                MethodHandler.class,
                INVOKE_METHOD_NAME,
                Object.class,
                Object.class, Method.class, Method.class, Object[].class);

        Expr result = b.invokeInterface(invokeDesc, handler,
                m.this_(), methodObj, nullMethod, argsArray);

        // 6. Handle return value
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            // Void method - just return
            b.return_();
        } else if (returnType.isPrimitive()) {
            // Primitive return - unbox
            Expr unboxed = unboxPrimitive(b, result, returnType);
            b.return_(unboxed);
        } else {
            // Object return - cast
            Expr casted = b.cast(result, returnType);
            b.return_(casted);
        }
    }

    /**
     * Boxes a primitive value into its wrapper type.
     * Gizmo 2's box() automatically determines the wrapper type from the expression type.
     */
    protected Expr boxPrimitive(BlockCreator b,
            Expr value, Class<?> primitiveType) {
        return b.box(value);
    }

    /**
     * Unboxes a wrapper object into its primitive value.
     * Gizmo 2's unbox() automatically determines the primitive type from the expression type.
     */
    protected Expr unboxPrimitive(BlockCreator b,
            Expr value, Class<?> primitiveType) {
        // First cast to the wrapper type, then unbox
        Class<?> wrapperType = getWrapperType(primitiveType);
        Expr casted = b.cast(value, wrapperType);
        return b.unbox(casted);
    }

    /**
     * Gets the wrapper type for a primitive type.
     */
    protected Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return Boolean.class;
        } else if (primitiveType == byte.class) {
            return Byte.class;
        } else if (primitiveType == short.class) {
            return Short.class;
        } else if (primitiveType == int.class) {
            return Integer.class;
        } else if (primitiveType == long.class) {
            return Long.class;
        } else if (primitiveType == float.class) {
            return Float.class;
        } else if (primitiveType == double.class) {
            return Double.class;
        } else if (primitiveType == char.class) {
            return Character.class;
        } else {
            throw new IllegalArgumentException("Unknown primitive type: " + primitiveType);
        }
    }

    /**
     * Adds constructors for beans with only private constructors using Gizmo 2 API.
     *
     * @param cc the class creator
     */
    /**
     * Adds two constructors to the class that call each other in order to bypass
     * the JVM class file verifier.
     * <p>
     * This would result in a stack overflow if they were actually called,
     * however the proxy is directly created without calling the constructor
     * (using Unsafe.allocateInstance or similar mechanisms).
     */
    protected void addConstructorsForBeanWithPrivateConstructors(ClassCreator cc) {
        // Add first constructor: public <init>(Byte b)
        // This calls the second constructor: this(null, null)
        cc.constructor(c -> {
            c.public_();
            c.parameter("b", Byte.class);

            c.body(b -> {
                // Call this(null, null) - invoke the second constructor
                ConstructorDesc secondCtor = ConstructorDesc.of(
                        cc.type(),
                        ClassDesc.of(Byte.class.getName()),
                        ClassDesc.of(Byte.class.getName()));
                Expr thisRef = c.this_();
                Expr nullByte1 = Const.ofNull(Byte.class);
                Expr nullByte2 = Const.ofNull(Byte.class);
                b.invokeSpecial(secondCtor, thisRef, nullByte1, nullByte2);
                b.return_();
            });
        });

        // Add second constructor: public <init>(Byte b1, Byte b2)
        // This calls the first constructor: this(null)
        cc.constructor(c -> {
            c.public_();
            c.parameter("b1", Byte.class);
            c.parameter("b2", Byte.class);

            c.body(b -> {
                // Call this(null) - invoke the first constructor
                ConstructorDesc firstCtor = ConstructorDesc.of(
                        cc.type(),
                        ClassDesc.of(Byte.class.getName()));
                Expr thisRef = c.this_();
                Expr nullByte = Const.ofNull(Byte.class);
                b.invokeSpecial(firstCtor, thisRef, nullByte);
                b.return_();
            });
        });
    }

    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        for (ProxiedMethodFilter filter : METHOD_FILTERS) {
            if (!filter.accept(method, proxySuperclass)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a bridge method has a concrete (non-bridge) implementation in the same class.
     * If it does, we skip the bridge method and only proxy the concrete implementation.
     *
     * @param bridgeMethod the bridge method to check
     * @param classMethods all declared methods in the class
     * @return true if there's a concrete implementation, false otherwise
     */
    protected boolean hasConcreteImplementation(Method bridgeMethod, Method[] classMethods) {
        if (!bridgeMethod.isBridge()) {
            return false;
        }

        String bridgeName = bridgeMethod.getName();
        Class<?>[] bridgeParams = bridgeMethod.getParameterTypes();

        for (Method candidate : classMethods) {
            // Skip if it's also a bridge method or has different name
            if (candidate.isBridge() || !candidate.getName().equals(bridgeName)) {
                continue;
            }

            // Check if parameter count matches
            Class<?>[] candidateParams = candidate.getParameterTypes();
            if (candidateParams.length != bridgeParams.length) {
                continue;
            }

            // Check if this is a more specific version of the bridge method
            // Bridge methods typically have Object or other generic types as parameters
            // while concrete implementations have specific types
            boolean isMoreSpecific = false;
            for (int i = 0; i < bridgeParams.length; i++) {
                if (bridgeParams[i] != candidateParams[i]) {
                    // Parameters differ - check if candidate is more specific
                    if (bridgeParams[i].isAssignableFrom(candidateParams[i])) {
                        isMoreSpecific = true;
                    } else {
                        // Parameters are incompatible, not a match
                        isMoreSpecific = false;
                        break;
                    }
                }
            }

            if (isMoreSpecific || (bridgeParams.length == candidateParams.length &&
                    java.util.Arrays.equals(bridgeParams, candidateParams))) {
                // Found a concrete implementation (either more specific or exact match that's not a bridge)
                return true;
            }
        }

        return false;
    }

    /**
     * Adds methods requiring special implementations using Gizmo 2 API.
     *
     * @param cc the class creator
     */
    protected void addSpecialMethods(ClassCreator cc) {
        try {
            // Add special methods for interceptors (LifecycleMixin interface)
            for (Method method : LifecycleMixin.class.getMethods()) {
                BeanLogger.LOG.addingMethodToProxy(method);
                // Implement lifecycle methods - they just delegate to method handler
                generateLifecycleMixinMethod(cc, method);
            }

            // Add TargetInstanceProxy methods
            // TODO: Method getInstanceMethod = TargetInstanceProxy.class.getMethod("weld_getTargetInstance");
            // TODO: Method getInstanceClassMethod = TargetInstanceProxy.class.getMethod("weld_getTargetClass");

            // Add ProxyObject methods (getMethodHandler, setMethodHandler)
            Method setMethodHandlerMethod = ProxyObject.class.getMethod("weld_setHandler", MethodHandler.class);
            generateSetMethodHandlerBody(cc, setMethodHandlerMethod);

            Method getMethodHandlerMethod = ProxyObject.class.getMethod("weld_getHandler");
            generateGetMethodHandlerBody(cc, getMethodHandlerMethod);
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Generates a LifecycleMixin method (postConstruct/preDestroy) that delegates to the method handler.
     */
    protected void generateLifecycleMixinMethod(ClassCreator cc, Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(void.class);

            m.body(b -> {
                // Get the method handler field
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());
                Expr handler = b.get(m.this_().field(methodHandlerField));

                // Get the Method object for this lifecycle method
                // LifecycleMixin.class.getMethod(methodName)
                Expr lifecycleMixinClass = Const.of(LifecycleMixin.class);
                Expr methodName = Const.of(method.getName());
                Expr emptyClassArray = b.newEmptyArray(Class.class, 0);

                MethodDesc getMethodDesc = MethodDesc.of(
                        Class.class, "getMethod", Method.class, String.class, Class[].class);
                Expr methodObj = b.invokeVirtual(getMethodDesc, lifecycleMixinClass,
                        methodName, emptyClassArray);

                // Create null proceed Method parameter
                Expr nullMethod = Const.ofNull(Method.class);

                // Create empty args array
                Expr emptyArgs = b.newEmptyArray(Object.class, 0);

                // Call methodHandler.invoke(this, methodObj, null, emptyArgs)
                MethodDesc invokeDesc = MethodDesc.of(
                        MethodHandler.class,
                        INVOKE_METHOD_NAME,
                        Object.class,
                        Object.class, Method.class, Method.class, Object[].class);

                b.invokeInterface(invokeDesc, handler, m.this_(), methodObj, nullMethod, emptyArgs);

                // Return (void method)
                b.return_();
            });
        });
    }

    /**
     * Generates the setMethodHandler method using Gizmo 2 API.
     *
     * @param cc the class creator
     * @param method the method to implement (weld_setHandler)
     */
    protected void generateSetMethodHandlerBody(ClassCreator cc, Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(void.class);
            var handlerParam = m.parameter("handler", MethodHandler.class);

            m.body(b -> {
                // this.methodHandler = (MethodHandlerType) handler;
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());

                // Cast the parameter to the specific MethodHandler type and set the field
                var castedHandler = b.cast(handlerParam, getMethodHandlerType());
                b.set(m.this_().field(methodHandlerField), castedHandler);
                b.return_();
            });
        });
    }

    /**
     * Generates the getMethodHandler method using Gizmo 2 API.
     *
     * @param cc the class creator
     * @param method the method to implement (weld_getHandler)
     */
    protected void generateGetMethodHandlerBody(ClassCreator cc, Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(MethodHandler.class);

            m.body(b -> {
                // return this.methodHandler;
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());

                var fieldValue = b.get(m.this_().field(methodHandlerField));
                b.return_(fieldValue);
            });
        });
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

    protected Class<?> getProxiedBeanType() {
        return proxiedBeanType;
    }

    protected Class<?> getProxySuperclass() {
        return getBeanType().isInterface() ? Object.class : getBeanType();
    }

    /**
     * @return {@code true} if {@link ProxyInstantiator} is used to instantiate proxy instances
     */
    protected boolean isUsingProxyInstantiator() {
        return true;
    }

    /**
     * @return {@code true} if {@link #CONSTRUCTED_FLAG_NAME} should be used
     */
    private boolean useConstructedFlag() {
        return !isUsingProxyInstantiator() || proxyInstantiator.isUsingConstructor();
    }

    /**
     * Converts a Class to a ClassDesc, properly handling arrays and primitives.
     * For regular classes: "com.example.Foo" -> ClassDesc
     * For arrays: "[Ljava.lang.String;" -> ClassDesc
     * For primitives: "int" -> ClassDesc
     */
    private static java.lang.constant.ClassDesc classDescriptorOf(Class<?> clazz) {
        if (clazz.isPrimitive() || clazz.isArray()) {
            // For primitives and arrays, use descriptor format
            // Primitives: "I", "J", "Z", etc.
            // Arrays: "[Ljava/lang/Object;", "[[I", etc.
            return java.lang.constant.ClassDesc.ofDescriptor(clazz.descriptorString());
        } else {
            // For regular classes, use binary name
            return java.lang.constant.ClassDesc.of(clazz.getName());
        }
    }

    /**
     * Delegates proxy creation via {@link ProxyServices} to the integrator or to our own implementation.
     * Uses bytecode and className generated by Gizmo 2.
     */
    protected Class<?> toClass(byte[] bytecode, String className, Class<?> originalClass, ProxyServices proxyServices,
            ProtectionDomain domain) {
        try {
            Class<?> result;
            if (domain == null) {
                result = proxyServices.defineClass(originalClass, className, bytecode, 0, bytecode.length);
            } else {
                result = proxyServices.defineClass(originalClass, className, bytecode, 0, bytecode.length, domain);
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * When creating a proxy class name we can sometimes determine it's package as well.
     */
    private static class ProxyNameHolder {
        private String packageName;
        private String className;

        private ProxyNameHolder(String packageName, String className, Bean<?> bean) {
            this.packageName = packageName;
            if (className == null) {
                throw BeanLogger.LOG.tryingToCreateProxyNameHolderWithoutClassName(bean.getBeanClass());
            }
            this.className = className;
        }

        /**
         * Class name, never null
         *
         * @return class name, never null
         */
        public String getClassName() {
            return className;
        }

        /**
         * Package name, can be null
         *
         * @return package name or null
         */
        public String getPackageName() {
            return packageName;
        }
    }
}
