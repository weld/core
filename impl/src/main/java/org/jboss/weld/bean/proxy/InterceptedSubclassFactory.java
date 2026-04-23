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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.LifecycleMixin;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.reflection.Reflections;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

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
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        // Use parent filters first
        if (!super.isMethodAccepted(method, proxySuperclass)) {
            return false;
        }
        // Additionally filter out private methods with package-private parameters
        // These would cause IllegalAccessError when the proxy subclass (in a different package)
        // tries to reference the package-private type
        return CommonProxiedMethodFilters.NON_PRIVATE_WITHOUT_PACK_PRIVATE_PARAMS.accept(method, proxySuperclass);
    }

    /**
     * Override to handle bridge methods specially for intercepted subclasses.
     * Bridge methods need special handling because:
     * 1. We can't use invokespecial on interface methods (VerifyError)
     * 2. We should skip bridge methods that have concrete implementations
     * 3. We must intercept bridge methods that don't have concrete implementations
     */
    @Override
    protected List<MethodInfo> collectMethodsToProxy() {
        List<MethodInfo> methods = new ArrayList<>();
        Class<?> cls = getBeanType();
        Set<MethodSignature> foundFinalMethods = new HashSet<>();
        Set<MethodSignature> addedMethods = new HashSet<>();
        Set<BridgeMethod> processedBridgeMethods = new HashSet<>();

        // Add methods from the class hierarchy
        while (cls != null) {
            Method[] classDeclaredMethods = cls.getDeclaredMethods();
            Set<BridgeMethod> declaredBridgeMethods = new HashSet<>();

            for (Method method : classDeclaredMethods) {
                MethodSignature methodSignature = new MethodSignatureImpl(method);

                if (Modifier.isFinal(method.getModifiers())) {
                    foundFinalMethods.add(methodSignature);
                }

                // Check if this is a bridge method with a concrete implementation
                boolean skipBridgeMethod = method.isBridge() &&
                        hasConcreteImplementation(method, classDeclaredMethods);

                // Check if method should be proxied (don't restrict to enhancedMethodSignatures - proxy all methods like parent)
                if (isMethodAccepted(method, getProxySuperclass())
                        && !skipBridgeMethod
                        && !foundFinalMethods.contains(methodSignature)
                        && !addedMethods.contains(methodSignature)
                        && !bridgeMethodsContainsMethod(processedBridgeMethods, methodSignature,
                                method.getGenericReturnType(), Modifier.isAbstract(method.getModifiers()))) {
                    methods.add(new MethodInfo(method, false));
                    addedMethods.add(methodSignature);
                } else {
                    // Track bridge methods even if we skip them
                    if (method.isBridge()) {
                        BridgeMethod bridgeMethod = new BridgeMethod(methodSignature, method.getGenericReturnType());
                        declaredBridgeMethods.add(bridgeMethod);
                    }
                }
            }

            processedBridgeMethods.addAll(declaredBridgeMethods);
            cls = cls.getSuperclass();
        }

        // Add methods from interfaces (including bridge methods from interfaces)
        Set<Class<?>> allInterfaces = new HashSet<>(getAdditionalInterfaces());
        if (interfacesToInspect != null) {
            allInterfaces.addAll(interfacesToInspect);
        }

        for (Class<?> iface : allInterfaces) {
            for (Method method : iface.getMethods()) {
                MethodSignature signature = new MethodSignatureImpl(method);
                // For interfaces we do not consider return types when checking bridge methods
                if (enhancedMethodSignatures.contains(signature)
                        && !addedMethods.contains(signature) // Check if already added
                        && !bridgeMethodsContainsMethod(processedBridgeMethods, signature, null,
                                Modifier.isAbstract(method.getModifiers()))) {
                    // Only add default methods from interfaces
                    if (Reflections.isDefault(method)) {
                        methods.add(new MethodInfo(method, true));
                        addedMethods.add(signature);
                    }
                }
                if (method.isBridge()) {
                    processedBridgeMethods.add(new BridgeMethod(signature, method.getGenericReturnType()));
                }
            }
        }

        return methods;
    }

    /**
     * Checks if a method signature is already covered by a processed bridge method.
     */
    private boolean bridgeMethodsContainsMethod(Set<BridgeMethod> processedBridgeMethods,
            MethodSignature signature, Type returnType, boolean isMethodAbstract) {
        for (BridgeMethod bridgeMethod : processedBridgeMethods) {
            if (bridgeMethod.signature.equals(signature)) {
                // Method signature is equal (name and params) but return type can still differ
                if (returnType != null) {
                    if (bridgeMethod.returnType.equals(Object.class) || isMethodAbstract) {
                        // Bridge method with matching signature has Object as return type
                        // or the method we compare against is abstract meaning the bridge overrides it
                        return true;
                    } else {
                        if (bridgeMethod.returnType instanceof Class && returnType instanceof TypeVariable) {
                            // Bridge method with specific return type in subclass
                            // and we are observing a TypeVariable return type in superclass
                            return true;
                        } else {
                            // As a last resort, check equality of return type
                            return bridgeMethod.returnType.equals(returnType);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a bridge method has a concrete (non-bridge) implementation in the same class.
     * Based on the old skipIfBridgeMethod logic.
     */
    @Override
    protected boolean hasConcreteImplementation(Method bridgeMethod,
            Method[] classDeclaredMethods) {
        if (!bridgeMethod.isBridge()) {
            return false;
        }

        String bridgeName = bridgeMethod.getName();
        Class<?>[] bridgeParams = bridgeMethod.getParameterTypes();

        for (Method declaredMethod : classDeclaredMethods) {
            // Only check non-bridge declared methods
            if (declaredMethod.isBridge()) {
                continue;
            }

            if (bridgeName.equals(declaredMethod.getName())) {
                Class<?>[] methodParams = bridgeMethod.getParameterTypes();
                Class<?>[] declaredMethodParams = declaredMethod.getParameterTypes();

                if (methodParams.length == declaredMethodParams.length) {
                    boolean paramsMatch = true;
                    boolean paramsNotMatching = false;
                    for (int i = 0; i < methodParams.length; i++) {
                        String methodParamName = methodParams[i].getName();
                        String declaredMethodParamName = declaredMethodParams[i].getName();
                        if (!methodParamName.equals(declaredMethodParamName)
                                && !methodParamName.equals(Object.class.getName())) {
                            paramsNotMatching = true;
                            break;
                        }
                    }

                    if (paramsNotMatching) {
                        continue;
                    }

                    // Parameters match, check if this is not an interface method
                    if (!Modifier.isInterface(declaredMethod.getDeclaringClass().getModifiers())) {
                        if (bridgeMethod.getReturnType().getName().equals(Object.class.getName())
                                || Modifier.isAbstract(declaredMethod.getModifiers())) {
                            // Bridge method with matching signature has Object as return type
                            // or the method we compare against is abstract meaning the bridge overrides it
                            return true;
                        } else {
                            // As a last resort, check equality of return type
                            if (bridgeMethod.getReturnType().getName().equals(declaredMethod.getReturnType().getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Helper class to track bridge methods with their return types.
     */
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
    }

    // DISABLED     @Override
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
    protected Class<? extends MethodHandler> getMethodHandlerType() {
        return CombinedInterceptorAndDecoratorStackMethodHandler.class;
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }

    @Override
    public Class<?> getBeanType() {
        return proxiedBeanType;
    }

    @Override
    protected void addFields(ClassCreator cc,
            List<DeferredBytecode> initialValueBytecode) {
        super.addFields(cc, initialValueBytecode);

        // Add private method handler field for private method interception
        cc.field(PRIVATE_METHOD_HANDLER_FIELD_NAME, f -> {
            f.setType(MethodHandler.class);
            f.private_();
        });
    }

    @Override
    protected void addMethodsFromClass(ClassCreator cc,
            List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {

        for (MethodInfo methodInfo : methodsToProxy) {
            Method method = methodInfo.method;
            String methodFieldName = methodFieldNames.get(methodInfo);

            // Check if this method should be intercepted
            MethodSignature methodSignature = new MethodSignatureImpl(method);
            boolean hasInterceptors = interceptedMethodSignatures.contains(methodSignature);

            // IMPORTANT: For bridge methods, check if the corresponding non-bridge method is intercepted
            // Bridge methods use invokespecial which bypasses virtual dispatch, so we must override them
            // in the proxy to ensure interception works
            if (!hasInterceptors && !method.isBridge()) {
                // This is a non-bridge method - check if there's a bridge with the same name that's intercepted
                for (MethodInfo other : methodsToProxy) {
                    if (other.method.getName().equals(method.getName()) && other.method.isBridge()) {
                        MethodSignature otherSig = new MethodSignatureImpl(
                                other.method);
                        if (interceptedMethodSignatures.contains(otherSig)) {
                            hasInterceptors = true;
                            break;
                        }
                    }
                }
            }

            if (hasInterceptors) {
                // For intercepted methods, we need to differentiate between bridge and non-bridge methods
                if (method.isBridge()) {
                    // For bridge methods, we need to determine if there's a corresponding non-bridge method
                    // that will handle the interception. If so, skip the bridge and let it delegate naturally.
                    // Only create a bridge delegate if the bridge is the ONLY intercepted method with this name.
                    boolean hasNonBridgeIntercepted = false;
                    for (MethodInfo other : methodsToProxy) {
                        if (other.method.getName().equals(method.getName()) && !other.method.isBridge()) {
                            MethodSignature otherSig = new MethodSignatureImpl(
                                    other.method);
                            if (interceptedMethodSignatures.contains(otherSig)) {
                                hasNonBridgeIntercepted = true;
                                break;
                            }
                        }
                    }

                    if (hasNonBridgeIntercepted) {
                        // Skip this bridge - there's a non-bridge method that's intercepted,
                        // and we'll create a proper bridge delegate after that method is created
                        continue;
                    } else {
                        // This bridge has no corresponding non-bridge intercepted method,
                        // so we need to intercept it directly

                        // First create the $$super method (skip for private methods only)
                        if (!Modifier.isPrivate(method.getModifiers())) {
                            if (methodInfo.isDefault) {
                                createDefaultMethodSuperDelegate(cc, method);
                            } else {
                                createSuperDelegateMethod(cc, method);
                            }
                        }

                        // Then create the intercepted method
                        createInterceptedMethod(cc, methodInfo, methodFieldName);
                    }
                } else {
                    // For non-bridge intercepted methods, create TWO methods:
                    // 1. The regular method that delegates to the interceptor chain
                    // 2. The method$$super() that calls super.method() (used as proceed by interceptors)

                    // Create the $$super method first (skip for private methods only)
                    // For default interface methods, create a special $$super that uses invokeSpecial on bean class
                    if (!Modifier.isPrivate(method.getModifiers())) {
                        if (methodInfo.isDefault) {
                            createDefaultMethodSuperDelegate(cc, method);
                        } else {
                            createSuperDelegateMethod(cc, method);
                        }
                    }

                    // Then create the regular method that delegates to interceptor chain
                    createInterceptedMethod(cc, methodInfo, methodFieldName);

                    // After creating the intercepted method, check if there are any bridge methods with the same name
                    // and create simple delegates for them (to prevent inherited bridges from using invokespecial)
                    for (MethodInfo bridgeCandidate : methodsToProxy) {
                        if (bridgeCandidate.method.isBridge()
                                && bridgeCandidate.method.getName().equals(method.getName())) {
                            MethodSignature bridgeSig = new MethodSignatureImpl(
                                    bridgeCandidate.method);
                            if (interceptedMethodSignatures.contains(bridgeSig)) {
                                // Create a simple bridge that calls this non-bridge method
                                createBridgeDelegateToMethod(cc, bridgeCandidate.method, method);
                            }
                        }
                    }
                }
            } else {
                // For non-intercepted methods in InterceptedSubclass, we still need special handling
                // They must call super directly but need to manage InterceptionDecorationContext
                // to prevent full interception when calling other methods
                //
                // EXCEPTION: Skip bridge methods and default interface methods - they naturally delegate
                // to concrete methods and we can't use invokespecial on interface methods anyway
                if (!method.isBridge() && !methodInfo.isDefault) {
                    createNonInterceptedMethod(cc, methodInfo, methodFieldName);
                }
            }
        }

    }

    /**
     * Creates a bridge method that delegates to a specific target method using virtual dispatch.
     * This prevents the inherited bridge from using invokespecial which would bypass interception.
     */
    private void createBridgeDelegateToMethod(ClassCreator cc, Method bridgeMethod,
            Method targetMethod) {
        MethodDesc bridgeDesc = MethodDesc.of(bridgeMethod);
        MethodDesc targetDesc = MethodDesc.of(targetMethod);

        cc.method(bridgeDesc, m -> {
            m.public_();
            m.synthetic();
            m.returning(bridgeMethod.getReturnType());

            // Add parameters
            Class<?>[] paramTypes = bridgeMethod.getParameterTypes();
            ParamVar[] params = new ParamVar[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = m.parameter("arg" + i, paramTypes[i]);
            }

            m.body(b -> {
                // Call this.targetMethod(params) using virtual dispatch
                // Cast parameters to match target method signature
                Class<?>[] targetParamTypes = targetMethod.getParameterTypes();
                Expr[] args = new Expr[params.length];
                for (int i = 0; i < params.length; i++) {
                    if (paramTypes[i] != targetParamTypes[i]) {
                        args[i] = b.cast(params[i], targetParamTypes[i]);
                    } else {
                        args[i] = params[i];
                    }
                }

                Expr result;
                if (params.length == 0) {
                    result = b.invokeVirtual(targetDesc, m.this_());
                } else if (params.length == 1) {
                    result = b.invokeVirtual(targetDesc, m.this_(), args[0]);
                } else if (params.length == 2) {
                    result = b.invokeVirtual(targetDesc, m.this_(), args[0], args[1]);
                } else {
                    result = b.invokeVirtual(targetDesc, m.this_(), args);
                }

                if (bridgeMethod.getReturnType() == void.class) {
                    b.return_();
                } else {
                    b.return_(result);
                }
            });
        });
    }

    /**
     * Creates a method$$super() for default interface methods.
     * Uses invokeSpecial on the bean class (superclass), which naturally delegates to the default interface method.
     */
    private void createDefaultMethodSuperDelegate(ClassCreator cc, Method method) {
        String superMethodName = method.getName() + SUPER_DELEGATE_SUFFIX;

        cc.method(superMethodName, m -> {
            // Make it private and synthetic
            m.private_();
            m.synthetic();
            m.returning(method.getReturnType());

            // Set varargs flag if the method is varargs
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            Class<?>[] paramTypes = method.getParameterTypes();
            ParamVar[] params = new ParamVar[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = m.parameter("arg" + i, paramTypes[i]);
            }

            // Add exception types
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableType = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableType);
            }

            m.body(b -> {
                // For default interface methods, call the bean class (superclass) method via invokeSpecial
                // Since the bean class doesn't override this method, it will naturally delegate to the
                // default interface implementation
                MethodDesc superMethodDesc = MethodDesc.of(getBeanType(), method.getName(),
                        method.getReturnType(), method.getParameterTypes());

                Expr result;
                if (params.length == 0) {
                    result = b.invokeSpecial(superMethodDesc, m.this_());
                } else if (params.length == 1) {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), params[0]);
                } else if (params.length == 2) {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), params[0], params[1]);
                } else {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), (Expr[]) params);
                }

                if (method.getReturnType() == void.class) {
                    b.return_();
                } else {
                    b.return_(result);
                }
            });
        });
    }

    /**
     * Creates a method$$super() that simply calls super.method().
     * This is used by interceptors as the "proceed" method.
     */
    private void createSuperDelegateMethod(ClassCreator cc, Method method) {
        String superMethodName = method.getName() + SUPER_DELEGATE_SUFFIX;

        cc.method(superMethodName, m -> {
            // Make it private and synthetic
            m.private_();
            m.synthetic();
            m.returning(method.getReturnType());

            // Set varargs flag if the method is varargs
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            Class<?>[] paramTypes = method.getParameterTypes();
            ParamVar[] params = new ParamVar[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = m.parameter("arg" + i, paramTypes[i]);
            }

            // Add exception types
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableType = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableType);
            }

            m.body(b -> {
                // Call super.method(args)
                MethodDesc superMethodDesc = MethodDesc.of(method);

                Expr result;
                if (params.length == 0) {
                    result = b.invokeSpecial(superMethodDesc, m.this_());
                } else if (params.length == 1) {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), params[0]);
                } else if (params.length == 2) {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), params[0], params[1]);
                } else {
                    result = b.invokeSpecial(superMethodDesc, m.this_(), (Expr[]) params);
                }

                if (method.getReturnType() == void.class) {
                    b.return_();
                } else {
                    b.return_(result);
                }
            });
        });
    }

    /**
     * Creates the regular method that delegates to the interceptor chain.
     * The interceptor chain will call method$$super() as the proceed method.
     */
    private void createInterceptedMethod(ClassCreator cc, MethodInfo methodInfo,
            String methodFieldName) {
        Method method = methodInfo.method;
        // Use methodInfo.isDefault to check if this is a default interface method
        boolean isDefaultInterfaceMethod = methodInfo.isDefault;

        // Use MethodDesc to properly handle overloaded methods
        MethodDesc methodDesc = MethodDesc.of(method);

        cc.method(methodDesc, m -> {
            m.public_();
            m.returning(method.getReturnType());

            // Set varargs flag if the method is varargs
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            Class<?>[] paramTypes = method.getParameterTypes();
            ParamVar[] params = new ParamVar[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = m.parameter("arg" + i, paramTypes[i]);
            }

            // Add exception types
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableType = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableType);
            }

            m.body(b -> {
                // Get the method handler field descriptor
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());

                // Create a local variable to store the instance reference (to avoid repeated field access)
                var thisVar = b.localVar("thisInstance", m.this_());

                // For relaxed construction mode, if methodHandler is null (during construction),
                // just call the super method directly (without interception)
                // EXCEPT for bridge/default interface methods - they can't use invokespecial
                if (!method.isBridge() && !isDefaultInterfaceMethod) {
                    Expr handlerCheck = b.get(thisVar.field(methodHandlerField));
                    Expr isNull = b.eq(handlerCheck, Const.ofNull(getMethodHandlerType()));
                    b.if_(isNull, nullBlock -> {
                        // Call super.method(args) directly
                        MethodDesc superMethodDesc = MethodDesc.of(method);

                        Expr superResult;
                        if (params.length == 0) {
                            superResult = nullBlock.invokeSpecial(superMethodDesc, thisVar);
                        } else if (params.length == 1) {
                            superResult = nullBlock.invokeSpecial(superMethodDesc, thisVar, params[0]);
                        } else if (params.length == 2) {
                            superResult = nullBlock.invokeSpecial(superMethodDesc, thisVar, params[0], params[1]);
                        } else {
                            superResult = nullBlock.invokeSpecial(superMethodDesc, thisVar, (Expr[]) params);
                        }

                        if (method.getReturnType() == void.class) {
                            nullBlock.return_();
                        } else {
                            nullBlock.return_(superResult);
                        }
                    });
                }
                // For bridge/default interface methods, skip the null check and always delegate to interceptor chain
                // These will be invoked via reflection

                // Read the handler field again (after null check, so we know it's not null here)
                Expr handlerField = b.get(thisVar.field(methodHandlerField));

                // Cast to StackAwareMethodHandler to ensure we call the correct invoke overload
                Expr handler = b.cast(handlerField, StackAwareMethodHandler.class);

                // Get the InterceptionDecorationContext Stack
                MethodDesc getStackDesc = MethodDesc.of(
                        InterceptionDecorationContext.class,
                        "getStack",
                        InterceptionDecorationContext.Stack.class);
                Expr stack = b.invokeStatic(getStackDesc);

                // Get the Method object for this method (from static field)
                FieldDesc methodField = FieldDesc.of(
                        cc.type(),
                        methodFieldName,
                        Method.class);
                Expr thisMethodObj = Expr.staticField(methodField);

                // Get the proceed Method object
                // For private methods ONLY: use the original method (will be invoked via reflection)
                // For all other methods (including bridge/default interface): use the $$super method
                Expr proceedMethodObj;
                if (Modifier.isPrivate(method.getModifiers())) {
                    // For private methods, the proceed method is the same as thisMethod
                    // These will be invoked via reflection by the method handler
                    proceedMethodObj = thisMethodObj;
                } else {
                    // For all non-private methods (regular, bridge, default interface), look up the $$super method
                    // The $$super method will be created separately and calls super.method() using invokespecial
                    // Call this.getClass().getDeclaredMethod(methodName + "$$super", paramTypes)
                    Expr thisClass = b.invokeVirtual(
                            MethodDesc.of(Object.class, "getClass", Class.class),
                            m.this_());

                    Expr superMethodName = Const.of(method.getName() + SUPER_DELEGATE_SUFFIX);

                    // Create parameter types array
                    Expr paramTypesArray;
                    if (paramTypes.length == 0) {
                        paramTypesArray = b.newEmptyArray(Class.class, 0);
                    } else {
                        Expr paramTypesExpr = b.newEmptyArray(Class.class, paramTypes.length);
                        var paramTypesVar = b.localVar("paramTypes", paramTypesExpr);

                        for (int i = 0; i < paramTypes.length; i++) {
                            b.set(paramTypesVar.elem(i), Const.of(paramTypes[i]));
                        }
                        paramTypesArray = paramTypesVar;
                    }

                    proceedMethodObj = b.invokeVirtual(
                            MethodDesc.of(Class.class, "getDeclaredMethod",
                                    Method.class, String.class, Class[].class),
                            thisClass, superMethodName, paramTypesArray);
                }

                // Create args array
                Expr argsArray;
                if (paramTypes.length == 0) {
                    argsArray = b.newEmptyArray(Object.class, 0);
                } else {
                    Expr arrayExpr = b.newEmptyArray(Object.class, paramTypes.length);
                    var argsVar = b.localVar("args", arrayExpr);

                    for (int i = 0; i < paramTypes.length; i++) {
                        Expr paramValue = params[i];
                        if (paramTypes[i].isPrimitive()) {
                            paramValue = b.box(paramValue);
                        }
                        b.set(argsVar.elem(i), paramValue);
                    }
                    argsArray = argsVar;
                }

                // Call methodHandler.invoke(stack, this, thisMethod, proceedMethod, args)
                // Use StackAwareMethodHandler interface which has a 5-parameter invoke method
                MethodDesc invokeDesc = MethodDesc.of(
                        StackAwareMethodHandler.class,
                        INVOKE_METHOD_NAME,
                        Object.class,
                        InterceptionDecorationContext.Stack.class, Object.class, Method.class,
                        Method.class, Object[].class);

                Expr result = b.invokeInterface(invokeDesc, handler,
                        stack, m.this_(), thisMethodObj, proceedMethodObj, argsArray);

                // Handle return value
                if (method.getReturnType() == void.class) {
                    b.return_();
                } else if (method.getReturnType().isPrimitive()) {
                    Expr casted = b.cast(result, getWrapperType(method.getReturnType()));
                    Expr unboxed = b.unbox(casted);
                    b.return_(unboxed);
                } else {
                    Expr casted = b.cast(result, method.getReturnType());
                    b.return_(casted);
                }
            });
        });
    }

    @Override
    protected void addSpecialMethods(ClassCreator cc) {
        try {
            // Add LifecycleMixin methods (postConstruct, preDestroy) using Stack-aware invoke
            for (Method method : LifecycleMixin.class.getMethods()) {
                BeanLogger.LOG.addingMethodToProxy(method);
                generateStackAwareLifecycleMixinMethod(cc, method);
            }

            // Add TargetInstanceProxy methods
            Method getInstanceMethod = TargetInstanceProxy.class
                    .getMethod("weld_getTargetInstance");
            generateGetTargetInstanceBody(cc, getInstanceMethod);

            Method getInstanceClassMethod = TargetInstanceProxy.class
                    .getMethod("weld_getTargetClass");
            generateGetTargetClassBody(cc, getInstanceClassMethod);

            // Add ProxyObject methods (getMethodHandler, setMethodHandler)
            Method setMethodHandlerMethod = ProxyObject.class.getMethod("weld_setHandler",
                    MethodHandler.class);
            generateSetMethodHandlerBody(cc, setMethodHandlerMethod);

            Method getMethodHandlerMethod = ProxyObject.class.getMethod("weld_getHandler");
            generateGetMethodHandlerBody(cc, getMethodHandlerMethod);
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Generates a LifecycleMixin method using Stack-aware invoke (5 parameters instead of 4).
     * This is needed for InterceptedSubclass to properly handle the interception/decoration context.
     */
    private void generateStackAwareLifecycleMixinMethod(ClassCreator cc,
            Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(void.class);

            m.body(b -> {
                // Get the method handler field
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());
                Expr handlerField = b.get(m.this_().field(methodHandlerField));

                // Cast to StackAwareMethodHandler to ensure we call the correct invoke overload
                Expr handler = b.cast(handlerField, StackAwareMethodHandler.class);

                // Get the InterceptionDecorationContext Stack
                // Pass null to let the handler fetch the stack (lifecycle callbacks use this pattern)
                Expr stack = Const.ofNull(InterceptionDecorationContext.Stack.class);

                // Get the Method object for this lifecycle method
                Expr lifecycleMixinClass = Const
                        .of(LifecycleMixin.class);
                Expr methodName = Const.of(method.getName());
                Expr emptyClassArray = b.newEmptyArray(Class.class, 0);

                MethodDesc getMethodDesc = MethodDesc.of(
                        Class.class, "getMethod", Method.class, String.class, Class[].class);
                Expr methodObj = b.invokeVirtual(getMethodDesc, lifecycleMixinClass,
                        methodName, emptyClassArray);

                // Create null proceed Method parameter (lifecycle callbacks don't have proceed)
                Expr nullMethod = Const.ofNull(Method.class);

                // Create empty args array
                Expr emptyArgs = b.newEmptyArray(Object.class, 0);

                // Call methodHandler.invoke(stack, this, methodObj, null, emptyArgs)
                // Use StackAwareMethodHandler's 5-parameter invoke
                MethodDesc invokeDesc = MethodDesc.of(
                        StackAwareMethodHandler.class,
                        INVOKE_METHOD_NAME,
                        Object.class,
                        InterceptionDecorationContext.Stack.class, Object.class, Method.class,
                        Method.class, Object[].class);

                b.invokeInterface(invokeDesc, handler, stack, m.this_(), methodObj, nullMethod, emptyArgs);

                // Return (void method)
                b.return_();
            });
        });
    }

    /**
     * Creates a non-intercepted method that calls super directly.
     * These methods still need to manage the InterceptionDecorationContext to prevent
     * full interception when they call other intercepted methods.
     */
    private void createNonInterceptedMethod(ClassCreator cc, MethodInfo methodInfo,
            String methodFieldName) {
        Method method = methodInfo.method;

        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(method.getReturnType());

            // Set varargs flag if the method is varargs
            if (method.isVarArgs()) {
                m.varargs();
            }

            // Add parameters
            Class<?>[] paramTypes = method.getParameterTypes();
            ParamVar[] params = new ParamVar[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = m.parameter("arg" + i, paramTypes[i]);
            }

            // Add exception types
            for (Class<?> exceptionType : method.getExceptionTypes()) {
                @SuppressWarnings("unchecked")
                Class<? extends Throwable> throwableType = (Class<? extends Throwable>) exceptionType;
                m.throws_(throwableType);
            }

            m.body(b -> {
                // For non-intercepted methods, we need to manage the InterceptionDecorationContext
                // to suppress interception for any calls made from within this method.
                // This prevents interceptors from firing on direct/self-invocations.

                // Get InterceptionDecorationContext stack
                MethodDesc getStackDesc = MethodDesc.of(
                        InterceptionDecorationContext.class,
                        "getStack",
                        InterceptionDecorationContext.Stack.class);
                Expr stackExpr = b.invokeStatic(getStackDesc);
                var stack = b.localVar("stack", stackExpr);

                // Get method handler to push onto stack
                FieldDesc methodHandlerField = FieldDesc.of(
                        cc.type(),
                        METHOD_HANDLER_FIELD_NAME,
                        getMethodHandlerType());
                Expr handler = b.get(m.this_().field(methodHandlerField));

                // Call stack.startIfNotOnTop(handler)
                // This returns true if we pushed, false if handler was already on top
                MethodDesc startIfNotOnTopDesc = MethodDesc.of(
                        InterceptionDecorationContext.Stack.class,
                        "startIfNotOnTop",
                        boolean.class,
                        CombinedInterceptorAndDecoratorStackMethodHandler.class);
                Expr shouldPopExpr = b.invokeVirtual(startIfNotOnTopDesc, stack, handler);
                var shouldPop = b.localVar("shouldPop", shouldPopExpr);

                // Call the method
                // - For private/interface methods: use privateMethodHandler to invoke via reflection
                // - For regular class methods: call super.method() directly with invokeSpecial
                Expr result;
                boolean isInterfaceMethod = method.getDeclaringClass().isInterface();
                if (Modifier.isPrivate(method.getModifiers()) || isInterfaceMethod) {
                    // For private/interface methods, use the private method handler to invoke via reflection
                    // Get the privateMethodHandler field
                    FieldDesc privateMethodHandlerField = FieldDesc.of(
                            cc.type(),
                            PRIVATE_METHOD_HANDLER_FIELD_NAME,
                            MethodHandler.class);
                    Expr privateHandler = b.get(m.this_().field(privateMethodHandlerField));

                    // Get the Method object for this private method
                    FieldDesc methodField = FieldDesc.of(
                            cc.type(),
                            methodFieldName,
                            Method.class);
                    Expr thisMethodObj = Expr.staticField(methodField);

                    // Create args array
                    Expr argsArray;
                    if (paramTypes.length == 0) {
                        argsArray = b.newEmptyArray(Object.class, 0);
                    } else {
                        Expr arrayExpr = b.newEmptyArray(Object.class, paramTypes.length);
                        var argsVar = b.localVar("args", arrayExpr);

                        for (int i = 0; i < paramTypes.length; i++) {
                            Expr paramValue = params[i];
                            if (paramTypes[i].isPrimitive()) {
                                paramValue = b.box(paramValue);
                            }
                            b.set(argsVar.elem(i), paramValue);
                        }
                        argsArray = argsVar;
                    }

                    // Call privateMethodHandler.invoke(this, thisMethod, null, args)
                    MethodDesc invokeDesc = MethodDesc.of(
                            MethodHandler.class,
                            INVOKE_METHOD_NAME,
                            Object.class,
                            Object.class, Method.class,
                            Method.class, Object[].class);
                    result = b.invokeInterface(invokeDesc, privateHandler,
                            m.this_(), thisMethodObj, Const.ofNull(Method.class), argsArray);
                } else {
                    // For regular class methods (non-private, non-interface), call super.method(args) directly
                    MethodDesc superMethodDesc = MethodDesc.of(method);
                    if (params.length == 0) {
                        result = b.invokeSpecial(superMethodDesc, m.this_());
                    } else if (params.length == 1) {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), params[0]);
                    } else if (params.length == 2) {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), params[0], params[1]);
                    } else {
                        result = b.invokeSpecial(superMethodDesc, m.this_(), (Expr[]) params);
                    }
                }

                // If we pushed onto the stack, pop it now
                Expr shouldPopCheck = b.eq(shouldPop, Const.of(true));
                b.if_(shouldPopCheck, popBlock -> {
                    MethodDesc endDesc = MethodDesc.of(
                            InterceptionDecorationContext.Stack.class,
                            "end",
                            void.class);
                    popBlock.invokeVirtual(endDesc, stack);
                });

                // Return the result
                if (method.getReturnType() == void.class) {
                    b.return_();
                } else {
                    b.return_(result);
                }
            });
        });
    }

    /**
     * Generates weld_getTargetInstance() which returns 'this'.
     * Note: The method is declared in TargetInstanceProxy<T> to return T, but at runtime
     * it's erased to Object. We use Object as the return type to match the erased signature.
     */
    private void generateGetTargetInstanceBody(ClassCreator cc,
            Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            // Use the actual return type from the method (which is erased to Object)
            m.returning(method.getReturnType());

            m.body(b -> {
                // Simply return this
                b.return_(m.this_());
            });
        });
    }

    /**
     * Generates weld_getTargetClass() which returns the bean type class.
     * Note: The method is declared in TargetInstanceProxy<T> to return Class<? extends T>,
     * but at runtime it's erased to Class. We match the erased signature.
     */
    private void generateGetTargetClassBody(ClassCreator cc,
            Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            // Use the actual return type from the method (which is erased to Class)
            m.returning(method.getReturnType());

            m.body(b -> {
                // Return the bean type class (superclass of this proxy)
                Expr beanTypeClass = Const.of(getBeanType());
                b.return_(beanTypeClass);
            });
        });
    }

    /**
     * Temporarily stubbed - not yet migrated to Gizmo 2
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
}
