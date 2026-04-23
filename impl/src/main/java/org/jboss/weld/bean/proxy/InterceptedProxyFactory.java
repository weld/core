/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.InterceptionFactoryImpl;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.reflection.Reflections;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * Generates proxies used to apply interceptors to custom bean instances and return values of producer methods.
 *
 * @author Martin Kouba
 * @author Matej Novotny
 *
 * @param <T>
 * @see InterceptionFactoryImpl
 */
public class InterceptedProxyFactory<T> extends ProxyFactory<T> {

    public static final String PROXY_SUFFIX = "InterceptedProxy";

    private final Set<MethodSignature> enhancedMethodSignatures;

    private final Set<MethodSignature> interceptedMethodSignatures;

    private final String suffix;

    private final boolean builtFromInterface;
    private Set<Class<?>> interfacesToInspect;

    public InterceptedProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure,
            Set<MethodSignature> enhancedMethodSignatures,
            Set<MethodSignature> interceptedMethodSignatures, String suffix) {
        super(contextId, proxiedBeanType, typeClosure, null);
        this.enhancedMethodSignatures = enhancedMethodSignatures;
        this.interceptedMethodSignatures = interceptedMethodSignatures;
        this.suffix = suffix;
        // it can happen that we are building the proxy from an interface, in such case we'll need to add more methods
        builtFromInterface = proxiedBeanType.isInterface();
    }

    protected String getProxyNameSuffix() {
        return PROXY_SUFFIX + suffix;
    }

    @Override
    protected void addMethods(ClassCreator cc) {
        // For interface-based proxies, ensure all interface methods are collected
        // This must be done before calling super.addMethods() which creates the static initializer
        if (builtFromInterface) {
            // This will be called by super.addMethods(), but we need to augment the list first
            // Unfortunately we can't easily override collectMethodsToProxy() due to inner class access
            // So we'll add missing methods in addMethodsFromClass() instead
        }
        super.addMethods(cc);
    }

    @Override
    protected List<MethodInfo> collectMethodsToProxy() {
        List<MethodInfo> methods = super.collectMethodsToProxy();

        // For interface-based proxies, ensure ALL interface methods are included
        // This is needed to avoid AbstractMethodError when the proxy is invoked
        if (builtFromInterface) {
            // Collect signatures of methods already in the list
            Set<MethodSignature> existingMethods = new HashSet<>();
            for (MethodInfo methodInfo : methods) {
                existingMethods.add(new MethodSignatureImpl(methodInfo.method));
            }

            // Get all methods from the proxied interface (including inherited)
            for (Method method : getProxiedBeanType().getMethods()) {
                MethodSignature signature = new MethodSignatureImpl(method);
                if (!existingMethods.contains(signature) && isMethodAccepted(method, getProxySuperclass())) {
                    // Add missing method to the list
                    methods.add(new MethodInfo(method,
                            Reflections.isDefault(method)));
                    existingMethods.add(signature);
                }
            }
        }

        return methods;
    }

    @Override
    protected void addMethodsFromClass(ClassCreator cc,
            List<MethodInfo> methodsToProxy,
            Map<MethodInfo, String> methodFieldNames) {

        // Process all collected methods (including those added by collectMethodsToProxy override)
        for (MethodInfo methodInfo : methodsToProxy) {
            Method method = methodInfo.method;
            MethodSignature methodSignature = new MethodSignatureImpl(method);

            String methodFieldName = methodFieldNames.get(methodInfo);

            // Check if method is in enhancedMethodSignatures (has interception metadata)
            boolean isEnhanced = enhancedMethodSignatures.contains(methodSignature);

            if (isEnhanced) {
                // Enhanced method: check if it has interceptors
                boolean hasInterceptors = interceptedMethodSignatures.contains(methodSignature);

                if (hasInterceptors) {
                    // Intercepted method: use StackAwareMethodHandler with method as proceed
                    addInterceptedProxyMethod(cc, methodInfo, methodFieldName);
                } else {
                    // Enhanced but non-intercepted method: use regular MethodHandler
                    super.addProxyMethod(cc, methodInfo, methodFieldName);
                }
            } else {
                // Not enhanced: still add proxy method to avoid AbstractMethodError
                // This is needed for interface-based proxies where all methods need implementations
                super.addProxyMethod(cc, methodInfo, methodFieldName);
            }
        }
    }

    /**
     * Adds an intercepted proxy method that uses StackAwareMethodHandler.
     * The method is passed as both thisMethod and proceed to the handler.
     */
    protected void addInterceptedProxyMethod(ClassCreator cc,
            MethodInfo methodInfo, String methodFieldName) {
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
                // InterceptedProxy doesn't use constructed flag - it wraps an existing instance
                // No constructed guard needed

                // Invoke using StackAwareMethodHandler with method as proceed
                invokeStackAwareMethodHandler(m, b, method, methodFieldName, params);
            });
        });

        BeanLogger.LOG.addingMethodToProxy(method);
    }

    /**
     * Invokes the StackAwareMethodHandler: handler.invoke(stack, this, method, method, args)
     * The method is passed as both thisMethod and proceed.
     */
    protected void invokeStackAwareMethodHandler(InstanceMethodCreator m,
            BlockCreator b, Method method, String methodFieldName,
            ParamVar[] params) {

        // 1. Load this.methodHandler and cast to StackAwareMethodHandler
        FieldDesc methodHandlerField = FieldDesc.of(
                m.owner(),
                METHOD_HANDLER_FIELD_NAME,
                getMethodHandlerType());
        Expr handlerField = b.get(m.this_().field(methodHandlerField));
        Expr handler = b.cast(handlerField, StackAwareMethodHandler.class);

        // 2. Get the Stack from InterceptionDecorationContext
        MethodDesc getStackDesc = MethodDesc.of(
                InterceptionDecorationContext.class,
                "getStack",
                InterceptionDecorationContext.Stack.class);
        Expr stackExpr = b.invokeStatic(getStackDesc);
        var stack = b.localVar("stack", stackExpr);

        // 3. Load the static Method field
        FieldDesc methodField = FieldDesc.of(
                m.owner(),
                methodFieldName,
                Method.class);
        Expr methodObj = Expr.staticField(methodField);

        // 4. Store method in LocalVar so we can use it twice (as thisMethod and proceed)
        var methodVar = b.localVar("method", methodObj);

        // 5. Create and populate Object[] args array
        Class<?>[] paramTypes = method.getParameterTypes();

        Expr argsArray;
        if (paramTypes.length == 0) {
            argsArray = b.newEmptyArray(Object.class, 0);
        } else {
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

        // 6. Call handler.invoke(stack, this, method, method, args)
        // StackAwareMethodHandler.invoke(Stack, Object, Method, Method, Object[])
        MethodDesc invokeDesc = MethodDesc.of(
                StackAwareMethodHandler.class,
                INVOKE_METHOD_NAME,
                Object.class,
                InterceptionDecorationContext.Stack.class, Object.class,
                Method.class, Method.class, Object[].class);

        // Pass methodVar twice - as both thisMethod and proceed
        Expr result = b.invokeInterface(invokeDesc, handler,
                stack, m.this_(), methodVar, methodVar, argsArray);

        // 7. Handle return value
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            b.return_();
        } else if (returnType.isPrimitive()) {
            Expr unboxed = unboxPrimitive(b, result, returnType);
            b.return_(unboxed);
        } else {
            Expr casted = b.cast(result, returnType);
            b.return_(casted);
        }
    }

    // DISABLED     @Override

    @Override
    protected void addSpecialMethods(ClassCreator cc) {
        try {
            // Call parent to add LifecycleMixin and ProxyObject methods
            super.addSpecialMethods(cc);

            // Add TargetInstanceProxy methods (not implemented by parent)
            Method getInstanceMethod = TargetInstanceProxy.class
                    .getMethod("weld_getTargetInstance");
            generateGetTargetInstanceBody(cc, getInstanceMethod);

            Method getInstanceClassMethod = TargetInstanceProxy.class
                    .getMethod("weld_getTargetClass");
            generateGetTargetClassBody(cc, getInstanceClassMethod);
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    /**
     * Generates weld_getTargetInstance() which returns 'this'.
     */
    private void generateGetTargetInstanceBody(ClassCreator cc,
            Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(method.getReturnType());

            m.body(b -> {
                b.return_(m.this_());
            });
        });
    }

    /**
     * Generates weld_getTargetClass() which returns the bean type class.
     */
    private void generateGetTargetClassBody(ClassCreator cc,
            Method method) {
        cc.method(method.getName(), m -> {
            m.public_();
            m.returning(method.getReturnType());

            m.body(b -> {
                Expr beanTypeClass = Const.of(getBeanType());
                b.return_(beanTypeClass);
            });
        });
    }
}
