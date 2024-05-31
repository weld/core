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

import static org.jboss.classfilewriter.util.DescriptorUtils.isPrimitive;
import static org.jboss.classfilewriter.util.DescriptorUtils.isWide;
import static org.jboss.classfilewriter.util.DescriptorUtils.makeDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.DuplicateMemberException;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;
import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.LifecycleMixin;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Factory for producing subclasses that are used by the combined interceptors and decorators stack.
 *
 * @author Marius Bogoevici
 */
public class InterceptedSubclassFactory<T> extends ProxyFactory<T> {

    // Default proxy class name suffix
    public static final String PROXY_SUFFIX = "Subclass";

    private static final String SUPER_DELEGATE_SUFFIX = "$$super";

    static final String COMBINED_INTERCEPTOR_AND_DECORATOR_STACK_METHOD_HANDLER_CLASS_NAME = CombinedInterceptorAndDecoratorStackMethodHandler.class
            .getName();
    static final String[] INVOKE_METHOD_PARAMETERS = new String[] { makeDescriptor(Stack.class), LJAVA_LANG_OBJECT,
            LJAVA_LANG_REFLECT_METHOD, LJAVA_LANG_REFLECT_METHOD, "[" + LJAVA_LANG_OBJECT };

    protected static final String PRIVATE_METHOD_HANDLER_FIELD_NAME = "privateMethodHandler";

    private final Set<MethodSignature> enhancedMethodSignatures;
    private final Set<MethodSignature> interceptedMethodSignatures;
    private Set<Class<?>> interfacesToInspect;

    private final Class<?> proxiedBeanType;

    public InterceptedSubclassFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean,
            Set<MethodSignature> enhancedMethodSignatures, Set<MethodSignature> interceptedMethodSignatures) {
        this(contextId, proxiedBeanType, typeClosure, getProxyName(contextId, proxiedBeanType, typeClosure, bean), bean,
                enhancedMethodSignatures, interceptedMethodSignatures);
    }

    /**
     * Creates a new proxy factory when the name of the proxy class is already
     * known, such as during de-serialization
     *
     * @param proxiedBeanType the super-class for this proxy class
     * @param typeClosure the bean types of the bean
     * @param enhancedMethodSignatures a restricted set of methods that need to be intercepted
     */
    public InterceptedSubclassFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure,
            String proxyName, Bean<?> bean,
            Set<MethodSignature> enhancedMethodSignatures, Set<MethodSignature> interceptedMethodSignatures) {
        super(contextId, proxiedBeanType, typeClosure, proxyName, bean, true);
        this.enhancedMethodSignatures = enhancedMethodSignatures;
        this.interceptedMethodSignatures = interceptedMethodSignatures;
        this.proxiedBeanType = proxiedBeanType;
    }

    @Override
    public void addInterfacesFromTypeClosure(Set<? extends Type> typeClosure, Class<?> proxiedBeanType) {
        // these interfaces we want to scan for method and our proxies will implement them
        for (Class<?> c : proxiedBeanType.getInterfaces()) {
            addInterface(c);
        }
        // now we need to go deeper in hierarchy and scan those interfaces for additional interfaces with default impls
        for (Type type : typeClosure) {
            Class<?> c = Reflections.getRawType(type);
            if (c.isInterface()) {
                addInterfaceToInspect(c);
            }
        }
    }

    private void addInterfaceToInspect(Class<?> iface) {
        if (interfacesToInspect == null) {
            interfacesToInspect = new HashSet<>();
        }
        this.interfacesToInspect.add(iface);
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

    @Override
    protected void addMethods(ClassFile proxyClassType, ClassMethod staticConstructor) {
        // Add all class methods for interception
        addMethodsFromClass(proxyClassType, staticConstructor);

        // Add special proxy methods
        addSpecialMethods(proxyClassType, staticConstructor);

    }

    private boolean skipIfBridgeMethod(Method method, Collection<Method> classDeclaredMethods) {
        if (method.isBridge()) {
            // if it's a bridge method, we need to see if the class also contains an actual "impl" of that method
            // if it does, we can skip this method, if it doesn't we will need to intercept it
            for (Method declaredMethod : classDeclaredMethods) {
                // only check non-bridge declared methods
                if (declaredMethod.isBridge()) {
                    continue;
                }
                if (method.getName().equals(declaredMethod.getName())) {
                    Class<?>[] methodParams = method.getParameterTypes();
                    Class<?>[] declaredMethodParams = declaredMethod.getParameterTypes();
                    if (methodParams.length != declaredMethodParams.length) {
                        continue;
                    }
                    boolean paramsNotMatching = false;
                    for (int i = 0; i < methodParams.length; i++) {
                        String methodParamName = methodParams[i].getName();
                        String declaredMethodParamName = declaredMethodParams[i].getName();
                        if (methodParamName.equals(declaredMethodParamName)
                                || methodParamName.equals(Object.class.getName())) {
                            continue;
                        } else {
                            paramsNotMatching = true;
                            break;
                        }
                    }
                    if (paramsNotMatching) {
                        continue;
                    }
                    if (!Modifier.isInterface(declaredMethod.getDeclaringClass().getModifiers())) {
                        if (method.getReturnType().getName().equals(Object.class.getName())
                                || Modifier.isAbstract(declaredMethod.getModifiers())) {
                            // bridge method with matching signature has Object as return type
                            // or the method we compare against is abstract meaning the bridge overrides it
                            // both cases are a match
                            return true;
                        } else {
                            // as a last resort, we simply check equality of return Type
                            if (method.getReturnType().getName().equals(declaredMethod.getReturnType().getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void addMethodsFromClass(ClassFile proxyClassType, ClassMethod staticConstructor) {
        try {

            final Set<MethodSignature> finalMethods = new HashSet<MethodSignature>();
            final Set<BridgeMethod> processedBridgeMethods = new HashSet<BridgeMethod>();

            // Add all methods from the class hierarchy
            Class<?> cls = getBeanType();
            while (cls != null) {
                Set<BridgeMethod> declaredBridgeMethods = new HashSet<BridgeMethod>();
                Collection<Method> classDeclaredMethods = Arrays
                        .asList(cls.getDeclaredMethods().clone());
                for (Method method : classDeclaredMethods) {

                    final MethodSignatureImpl methodSignature = new MethodSignatureImpl(method);

                    if (!Modifier.isFinal(method.getModifiers()) && !skipIfBridgeMethod(method, classDeclaredMethods)
                            && enhancedMethodSignatures.contains(methodSignature)
                            && !finalMethods.contains(methodSignature)
                            && CommonProxiedMethodFilters.NON_PRIVATE_WITHOUT_PACK_PRIVATE_PARAMS.accept(method,
                                    getProxySuperclass())
                            && !bridgeMethodsContainsMethod(processedBridgeMethods, methodSignature,
                                    method.getGenericReturnType(), Modifier.isAbstract(method.getModifiers()))) {
                        try {
                            final MethodInformation methodInfo = new RuntimeMethodInformation(method);

                            if (interceptedMethodSignatures.contains(methodSignature)) {
                                // create delegate-to-super method
                                createDelegateMethod(proxyClassType, method, methodInfo);

                                // this method is intercepted
                                // override a subclass method to delegate to method handler
                                ClassMethod classMethod = proxyClassType.addMethod(method);
                                addConstructedGuardToMethodBody(classMethod);
                                createForwardingMethodBody(classMethod, methodInfo, staticConstructor);
                                BeanLogger.LOG.addingMethodToProxy(method);
                            } else {
                                // this method is not intercepted
                                // we still need to override and push InterceptionDecorationContext stack to prevent full interception
                                ClassMethod classMethod = proxyClassType.addMethod(method);
                                new RunWithinInterceptionDecorationContextGenerator(classMethod, this) {

                                    @Override
                                    void doWork(CodeAttribute b, ClassMethod classMethod) {
                                        if (Modifier.isPrivate(classMethod.getAccessFlags())) {
                                            // Weld cannot use invokespecial to invoke a private method from the superclass
                                            invokePrivateMethodHandler(b, classMethod, methodInfo, staticConstructor);
                                        } else {
                                            // build the bytecode that invokes the super class method directly
                                            b.aload(0);
                                            // create the method invocation
                                            b.loadMethodParameters();
                                            b.invokespecial(methodInfo.getDeclaringClass(), methodInfo.getName(),
                                                    methodInfo.getDescriptor());
                                        }
                                        // leave the result on top of the stack
                                    }

                                    @Override
                                    void doReturn(CodeAttribute b, ClassMethod method) {
                                        // assumes doWork() result is on top of the stack
                                        b.returnInstruction();
                                    }
                                }.runStartIfNotOnTop();
                            }

                        } catch (DuplicateMemberException e) {
                            // do nothing. This will happen if superclass methods have
                            // been overridden
                        }
                    } else {
                        if (Modifier.isFinal(method.getModifiers())) {
                            finalMethods.add(methodSignature);
                        }
                        if (method.isBridge()) {
                            BridgeMethod bridgeMethod = new BridgeMethod(methodSignature, method.getGenericReturnType());
                            if (!hasAbstractPackagePrivateSuperClassWithImplementation(cls, bridgeMethod)) {
                                declaredBridgeMethods.add(bridgeMethod);
                            }
                        }
                    }
                }
                processedBridgeMethods.addAll(declaredBridgeMethods);
                cls = cls.getSuperclass();
            }
            // We want to iterate over pre-defined interfaces (getAdditionalInterfaces()) and also over those we discovered earlier (interfacesToInspect)
            Set<Class<?>> allInterfaces = new HashSet<>(getAdditionalInterfaces());
            if (interfacesToInspect != null) {
                allInterfaces.addAll(interfacesToInspect);
            }
            for (Class<?> c : allInterfaces) {
                for (Method method : c.getMethods()) {
                    MethodSignature signature = new MethodSignatureImpl(method);
                    // For interfaces we do not consider return types when going through processed bridge methods
                    if (enhancedMethodSignatures.contains(signature) && !bridgeMethodsContainsMethod(processedBridgeMethods,
                            signature, null, Modifier.isAbstract(method.getModifiers()))) {
                        try {
                            MethodInformation methodInfo = new RuntimeMethodInformation(method);
                            if (interceptedMethodSignatures.contains(signature) && Reflections.isDefault(method)) {
                                createDelegateMethod(proxyClassType, method, methodInfo);

                                // this method is intercepted
                                // override a subclass method to delegate to method handler
                                ClassMethod classMethod = proxyClassType.addMethod(method);
                                addConstructedGuardToMethodBody(classMethod);
                                createForwardingMethodBody(classMethod, methodInfo, staticConstructor);
                                BeanLogger.LOG.addingMethodToProxy(method);
                            } else {
                                // we only want to add default methods, rest is abstract and cannot be invoked
                                if (Reflections.isDefault(method)) {
                                    createDelegateMethod(proxyClassType, method, methodInfo);
                                }
                            }
                        } catch (DuplicateMemberException e) {
                        }
                    }
                    if (method.isBridge()) {
                        processedBridgeMethods.add(new BridgeMethod(signature, method.getGenericReturnType()));
                    }
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Returns true if super class of the parameter exists and is abstract and package private. In such case we want to omit
     * such method.
     *
     * See WELD-2507 and Oracle issue - https://bugs.java.com/view_bug.do?bug_id=6342411
     *
     * @return true if the super class exists and is abstract and package private
     */
    private boolean hasAbstractPackagePrivateSuperClassWithImplementation(Class<?> clazz, BridgeMethod bridgeMethod) {
        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            if (Modifier.isAbstract(superClass.getModifiers()) && Reflections.isPackagePrivate(superClass.getModifiers())) {
                // if superclass is abstract, we need to dig deeper
                for (Method method : superClass.getDeclaredMethods()) {
                    if (bridgeMethod.signature.matches(method) && method.getGenericReturnType().equals(bridgeMethod.returnType)
                            && !Reflections.isAbstract(method)) {
                        // this is the case we are after -> methods have same signature and the one in super class has actual implementation
                        return true;
                    }
                }
            }
            superClass = superClass.getSuperclass();
        }
        return false;
    }

    private boolean bridgeMethodsContainsMethod(Set<BridgeMethod> processedBridgeMethods, MethodSignature signature,
            Type returnType, boolean isMethodAbstract) {
        for (BridgeMethod bridgeMethod : processedBridgeMethods) {
            if (bridgeMethod.signature.equals(signature)) {
                // method signature is equal (name and params) but return type can still differ
                if (returnType != null) {
                    if (bridgeMethod.returnType.equals(Object.class) || isMethodAbstract) {
                        // bridge method with matching signature has Object as return type
                        // or the method we compare against is abstract meaning the bridge overrides it
                        // both cases are a match
                        return true;
                    } else {
                        if (bridgeMethod.returnType instanceof Class && returnType instanceof TypeVariable) {
                            // in this case we have encountered a bridge method with specific return type in subclass
                            // and we are observing a TypeVariable return type in superclass, this is a match
                            return true;
                        } else {
                            // as a last resort, we simply check equality of return Type
                            return bridgeMethod.returnType.equals(returnType);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected void createForwardingMethodBody(ClassMethod classMethod, MethodInformation method,
            ClassMethod staticConstructor) {
        createInterceptorBody(classMethod, method, true, staticConstructor);
    }

    /**
     * Creates the given method on the proxy class where the implementation
     * forwards the call directly to the method handler.
     * <p/>
     * the generated bytecode is equivalent to:
     * <p/>
     * return (RetType) methodHandler.invoke(this,param1,param2);
     *
     * @param methodInfo any JLR method
     * @param delegateToSuper
     * @return the method byte code
     */

    protected void createInterceptorBody(ClassMethod method, MethodInformation methodInfo, boolean delegateToSuper,
            ClassMethod staticConstructor) {

        invokeMethodHandler(method, methodInfo, true, DEFAULT_METHOD_RESOLVER, delegateToSuper, staticConstructor);
    }

    private void createDelegateToSuper(ClassMethod classMethod, MethodInformation method) {
        createDelegateToSuper(classMethod, method, classMethod.getClassFile().getSuperclass());
    }

    private void createDelegateToSuper(ClassMethod classMethod, MethodInformation method, String className) {
        CodeAttribute b = classMethod.getCodeAttribute();
        // first generate the invokespecial call to the super class method
        b.aload(0);
        b.loadMethodParameters();
        b.invokespecial(className, method.getName(), method.getDescriptor());
        b.returnInstruction();
    }

    /**
     * calls methodHandler.invoke for a given method
     *
     * @param methodInfo declaring class of the method
     * @param addReturnInstruction set to true you want to return the result of
     * @param bytecodeMethodResolver The method resolver
     * @param addProceed
     */
    protected void invokeMethodHandler(ClassMethod method, MethodInformation methodInfo, boolean addReturnInstruction,
            BytecodeMethodResolver bytecodeMethodResolver, boolean addProceed, ClassMethod staticConstructor) {
        // now we need to build the bytecode. The order we do this in is as
        // follows:
        // load methodHandler
        // dup the methodhandler
        // invoke isDisabledHandler on the method handler to figure out of this is
        // a self invocation.

        // load this
        // load the method object
        // load the proceed method that invokes the superclass version of the
        // current method
        // create a new array the same size as the number of parameters
        // push our parameter values into the array
        // invokeinterface the invoke method
        // add checkcast to cast the result to the return type, or unbox if
        // primitive
        // add an appropriate return instruction
        final CodeAttribute b = method.getCodeAttribute();
        b.aload(0);
        getMethodHandlerField(method.getClassFile(), b);

        if (addProceed) {
            b.dup();

            // get the Stack
            b.invokestatic(InterceptionDecorationContext.class.getName(), "getStack",
                    "()" + DescriptorUtils.makeDescriptor(Stack.class));

            // this is a self invocation optimisation
            // test to see if this is a self invocation, and if so invokespecial the
            // superclass method directly
            // Do not optimize in case of private and default methods
            if (!Reflections.isDefault(methodInfo.getMethod()) && !Modifier.isPrivate(method.getAccessFlags())) {
                b.dupX1(); // Handler, Stack -> Stack, Handler, Stack
                b.invokevirtual(COMBINED_INTERCEPTOR_AND_DECORATOR_STACK_METHOD_HANDLER_CLASS_NAME, "isDisabledHandler",
                        "(" + DescriptorUtils.makeDescriptor(Stack.class) + ")" + BytecodeUtils.BOOLEAN_CLASS_DESCRIPTOR);
                b.iconst(0);
                BranchEnd invokeSuperDirectly = b.ifIcmpeq();
                // now build the bytecode that invokes the super class method
                b.pop2(); // pop Stack and Handler
                b.aload(0);
                // create the method invocation
                b.loadMethodParameters();
                b.invokespecial(methodInfo.getDeclaringClass(), methodInfo.getName(), methodInfo.getDescriptor());
                b.returnInstruction();
                b.branchEnd(invokeSuperDirectly);
            }
        } else {
            b.aconstNull();
        }

        b.aload(0);
        bytecodeMethodResolver.getDeclaredMethod(method, methodInfo.getDeclaringClass(), methodInfo.getName(),
                methodInfo.getParameterTypes(), staticConstructor);

        if (addProceed) {
            if (Modifier.isPrivate(method.getAccessFlags())) {
                // If the original method is private we can't use WeldSubclass.method$$super() as proceed
                bytecodeMethodResolver.getDeclaredMethod(method, methodInfo.getDeclaringClass(), methodInfo.getName(),
                        methodInfo.getParameterTypes(),
                        staticConstructor);
            } else {
                bytecodeMethodResolver.getDeclaredMethod(method, method.getClassFile().getName(),
                        methodInfo.getName() + SUPER_DELEGATE_SUFFIX,
                        methodInfo.getParameterTypes(), staticConstructor);
            }
        } else {
            b.aconstNull();
        }

        b.iconst(methodInfo.getParameterTypes().length);
        b.anewarray(Object.class.getName());

        int localVariableCount = 1;

        for (int i = 0; i < methodInfo.getParameterTypes().length; ++i) {
            String typeString = methodInfo.getParameterTypes()[i];
            b.dup(); // duplicate the array reference
            b.iconst(i);
            // load the parameter value
            BytecodeUtils.addLoadInstruction(b, typeString, localVariableCount);
            // box the parameter if necessary
            Boxing.boxIfNessesary(b, typeString);
            // and store it in the array
            b.aastore();
            if (isWide(typeString)) {
                localVariableCount = localVariableCount + 2;
            } else {
                localVariableCount++;
            }
        }
        // now we have all our arguments on the stack
        // lets invoke the method
        b.invokeinterface(StackAwareMethodHandler.class.getName(), INVOKE_METHOD_NAME, LJAVA_LANG_OBJECT,
                INVOKE_METHOD_PARAMETERS);
        if (addReturnInstruction) {
            // now we need to return the appropriate type
            if (methodInfo.getReturnType().equals(BytecodeUtils.VOID_CLASS_DESCRIPTOR)) {
                b.returnInstruction();
            } else if (isPrimitive(methodInfo.getReturnType())) {
                Boxing.unbox(b, method.getReturnType());
                b.returnInstruction();
            } else {
                b.checkcast(BytecodeUtils.getName(methodInfo.getReturnType()));
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
    protected void addSpecialMethods(ClassFile proxyClassType, ClassMethod staticConstructor) {
        try {
            // Add special methods for interceptors
            for (Method method : LifecycleMixin.class.getMethods()) {
                BeanLogger.LOG.addingMethodToProxy(method);
                MethodInformation methodInfo = new RuntimeMethodInformation(method);
                createInterceptorBody(proxyClassType.addMethod(method), methodInfo, false, staticConstructor);
            }
            Method getInstanceMethod = TargetInstanceProxy.class.getMethod("weld_getTargetInstance");
            Method getInstanceClassMethod = TargetInstanceProxy.class.getMethod("weld_getTargetClass");
            generateGetTargetInstanceBody(proxyClassType.addMethod(getInstanceMethod));
            generateGetTargetClassBody(proxyClassType.addMethod(getInstanceClassMethod));

            Method setMethodHandlerMethod = ProxyObject.class.getMethod("weld_setHandler", MethodHandler.class);
            generateSetMethodHandlerBody(proxyClassType.addMethod(setMethodHandlerMethod));

            Method getMethodHandlerMethod = ProxyObject.class.getMethod("weld_getHandler");
            generateGetMethodHandlerBody(proxyClassType.addMethod(getMethodHandlerMethod));
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    private static void generateGetTargetInstanceBody(ClassMethod method) {
        final CodeAttribute b = method.getCodeAttribute();
        b.aload(0);
        b.returnInstruction();
    }

    private static void generateGetTargetClassBody(ClassMethod method) {
        final CodeAttribute b = method.getCodeAttribute();
        BytecodeUtils.pushClassType(b, method.getClassFile().getSuperclass());
        b.returnInstruction();
    }

    @Override
    public Class<?> getBeanType() {
        return proxiedBeanType;
    }

    @Override
    protected Class<? extends MethodHandler> getMethodHandlerType() {
        return CombinedInterceptorAndDecoratorStackMethodHandler.class;
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private void createDelegateMethod(ClassFile proxyClassType, Method method, MethodInformation methodInformation) {
        int modifiers = (method.getModifiers() | AccessFlag.SYNTHETIC | AccessFlag.PRIVATE) & ~AccessFlag.PUBLIC
                & ~AccessFlag.PROTECTED;
        ClassMethod delegatingMethod = proxyClassType.addMethod(modifiers, method.getName() + SUPER_DELEGATE_SUFFIX,
                DescriptorUtils.makeDescriptor(method.getReturnType()),
                DescriptorUtils.parameterDescriptors(method.getParameterTypes()));
        delegatingMethod.addCheckedExceptions((Class<? extends Exception>[]) method.getExceptionTypes());
        createDelegateToSuper(delegatingMethod, methodInformation);
    }

    private void invokePrivateMethodHandler(CodeAttribute b, ClassMethod classMethod, MethodInformation methodInfo,
            ClassMethod staticConstructor) {
        try {
            classMethod.getClassFile().addField(AccessFlag.PRIVATE, PRIVATE_METHOD_HANDLER_FIELD_NAME, MethodHandler.class);
        } catch (DuplicateMemberException ignored) {
        }
        // 1. Load private method handler
        b.aload(0);
        b.getfield(classMethod.getClassFile().getName(), PRIVATE_METHOD_HANDLER_FIELD_NAME,
                DescriptorUtils.makeDescriptor(MethodHandler.class));
        // 2. Load this
        b.aload(0);
        // 3. Load method
        DEFAULT_METHOD_RESOLVER.getDeclaredMethod(classMethod, methodInfo.getDeclaringClass(), methodInfo.getName(),
                methodInfo.getParameterTypes(),
                staticConstructor);
        // 4. No proceed method
        b.aconstNull();
        // 5. Load method params
        b.iconst(methodInfo.getParameterTypes().length);
        b.anewarray(Object.class.getName());
        int localVariableCount = 1;
        for (int i = 0; i < methodInfo.getParameterTypes().length; ++i) {
            String typeString = methodInfo.getParameterTypes()[i];
            b.dup(); // duplicate the array reference
            b.iconst(i);
            // load the parameter value
            BytecodeUtils.addLoadInstruction(b, typeString, localVariableCount);
            // box the parameter if necessary
            Boxing.boxIfNessesary(b, typeString);
            // and store it in the array
            b.aastore();
            if (isWide(typeString)) {
                localVariableCount = localVariableCount + 2;
            } else {
                localVariableCount++;
            }
        }
        // Invoke PrivateMethodHandler
        b.invokeinterface(MethodHandler.class.getName(), INVOKE_METHOD_NAME, LJAVA_LANG_OBJECT,
                new String[] { LJAVA_LANG_OBJECT, LJAVA_LANG_REFLECT_METHOD, LJAVA_LANG_REFLECT_METHOD,
                        "[" + LJAVA_LANG_OBJECT });
        if (methodInfo.getReturnType().equals(BytecodeUtils.VOID_CLASS_DESCRIPTOR)) {
            // No-op
        } else if (isPrimitive(methodInfo.getReturnType())) {
            Boxing.unbox(b, methodInfo.getReturnType());
        } else {
            b.checkcast(BytecodeUtils.getName(methodInfo.getReturnType()));
        }
    }

    /**
     * If the given instance represents a proxy and its class is synthetic and its class name ends with {@value #PROXY_SUFFIX},
     * attempt to find the
     * {@value #PRIVATE_METHOD_HANDLER_FIELD_NAME} field and set its value to {@link PrivateMethodHandler#INSTANCE}.
     *
     * @param instance
     */
    public static <T> void setPrivateMethodHandler(T instance) {
        if (instance instanceof ProxyObject && instance.getClass().isSynthetic()
                && instance.getClass().getName().endsWith(PROXY_SUFFIX)
                && Reflections.hasDeclaredField(instance.getClass(), PRIVATE_METHOD_HANDLER_FIELD_NAME)) {
            try {
                Field privateMethodHandlerField = instance.getClass().getDeclaredField(PRIVATE_METHOD_HANDLER_FIELD_NAME);
                Reflections.ensureAccessible(privateMethodHandlerField, instance);
                privateMethodHandlerField.set(instance, PrivateMethodHandler.INSTANCE);
            } catch (NoSuchFieldException ignored) {
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class BridgeMethod {

        private final Type returnType;

        private final MethodSignature signature;

        public BridgeMethod(MethodSignature signature, Type returnType) {
            this.signature = signature;
            this.returnType = returnType;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
            result = prime * result + ((signature == null) ? 0 : signature.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof BridgeMethod)) {
                return false;
            }
            BridgeMethod other = (BridgeMethod) obj;
            if (returnType == null) {
                if (other.returnType != null) {
                    return false;
                }
            } else if (!returnType.equals(other.returnType)) {
                return false;
            }
            if (signature == null) {
                if (other.signature != null) {
                    return false;
                }
            } else if (!signature.equals(other.signature)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return new StringBuilder().append("method ").append(returnType).append(" ").append(signature.getMethodName())
                    .append(Arrays.toString(signature.getParameterTypes()).replace('[', '(').replace(']', ')')).toString();
        }

    }

}
