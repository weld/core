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

import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.DuplicateMemberException;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.Boxing;
import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.InterceptionFactoryImpl;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.security.GetDeclaredMethodsAction;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;
import org.jboss.weld.util.reflection.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.jboss.classfilewriter.util.DescriptorUtils.isPrimitive;
import static org.jboss.classfilewriter.util.DescriptorUtils.isWide;

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

    private static final String JAVA_LANG_OBJECT = "java.lang.Object";

    private final Set<MethodSignature> enhancedMethodSignatures;

    private final Set<MethodSignature> interceptedMethodSignatures;

    private final String suffix;

    private final boolean builtFromInterface;
    private Set<Class<?>> interfacesToInspect;

    public InterceptedProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Set<MethodSignature> enhancedMethodSignatures,
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
    protected void addMethodsFromClass(ClassFile proxyClassType, ClassMethod staticConstructor) {
        try {

            final Set<MethodSignature> finalMethods = new HashSet<MethodSignature>();
            final Set<MethodSignature> processedBridgeMethods = new HashSet<MethodSignature>();

            // Add all methods from the class hierarchy + proxied type
            Set<Class<?>> classes = new LinkedHashSet<>();
            for (Class<?> cls = getBeanType(); cls != null; cls = cls.getSuperclass()) {
                classes.add(cls);
            }
            classes.add(getProxiedBeanType());

            for (Class<?> cls : classes) {
                Set<MethodSignature> declaredBridgeMethods = new HashSet<MethodSignature>();
                for (Method method : AccessController.doPrivileged(new GetDeclaredMethodsAction(cls))) {

                    final MethodSignatureImpl methodSignature = new MethodSignatureImpl(method);

                    if (isMethodAccepted(method, getProxySuperclass()) && enhancedMethodSignatures.contains(methodSignature)
                            && !finalMethods.contains(methodSignature) && !processedBridgeMethods.contains(methodSignature)) {
                        try {
                            final MethodInformation methodInfo = new RuntimeMethodInformation(method);
                            ClassMethod classMethod = proxyClassType.addMethod(method);

                            if (interceptedMethodSignatures.contains(methodSignature)) {
                                // this method is intercepted
                                createInterceptedMethod(classMethod, methodInfo, method, staticConstructor);
                                BeanLogger.LOG.addingMethodToProxy(method);
                            } else {
                                createNotInterceptedMethod(classMethod, methodInfo, method, staticConstructor);
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
                            declaredBridgeMethods.add(methodSignature);
                        }
                    }
                }
                processedBridgeMethods.addAll(declaredBridgeMethods);
            }
            if (builtFromInterface) {
                // since we are just on top of an interface, we will need to add all the methods from interface
                // hierarchy so that there are no AbstractMethodError exception popping up
                for (Class<?> c : interfacesToInspect) {
                    for (Method method : c.getMethods()) {
                        MethodSignature signature = new MethodSignatureImpl(method);
                        try {
                            if (isMethodAccepted(method, getProxySuperclass())
                                    && !processedBridgeMethods.contains(signature)) {

                                final MethodInformation methodInfo = new RuntimeMethodInformation(method);
                                ClassMethod classMethod = proxyClassType.addMethod(method);
                                createNotInterceptedMethod(classMethod, methodInfo, method, staticConstructor);
                                BeanLogger.LOG.addingMethodToProxy(method);
                            }
                        } catch (DuplicateMemberException e) {
                        }
                        if (method.isBridge()) {
                            processedBridgeMethods.add(signature);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    @Override
    public void addInterfacesFromTypeClosure(Set<? extends Type> typeClosure, Class<?> proxiedBeanType) {
        // store all interfaces we want to look into later, only usable if we make this proxy on top of an interface
        for (Type type : typeClosure) {
            Class<?> c = Reflections.getRawType(type);
            if (c.isInterface()) {
                addInterfaceToInspect(c);
            }
        }
    }

    @Override
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        return super.isMethodAccepted(method, proxySuperclass) && CommonProxiedMethodFilters.NON_PRIVATE.accept(method, proxySuperclass) && !method.isBridge();
    }

    private void createNotInterceptedMethod(ClassMethod classMethod, final MethodInformation methodInfo, Method method, ClassMethod staticConstructor) {
        // we only care about default and intercepted methods now
        final CodeAttribute b = classMethod.getCodeAttribute();

        b.aload(0);
        getMethodHandlerField(classMethod.getClassFile(), b);

        b.aload(0);
        DEFAULT_METHOD_RESOLVER.getDeclaredMethod(classMethod, methodInfo.getDeclaringClass(), method.getName(),
                methodInfo.getParameterTypes(), staticConstructor);
        b.aconstNull();

        b.iconst(method.getParameterCount());
        b.anewarray(JAVA_LANG_OBJECT);

        int localVariableCount = 1;

        for (int i = 0; i < method.getParameterCount(); ++i) {
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

        b.invokeinterface(MethodHandler.class.getName(), INVOKE_METHOD_NAME, LJAVA_LANG_OBJECT,
                new String[] { LJAVA_LANG_OBJECT, LJAVA_LANG_REFLECT_METHOD, LJAVA_LANG_REFLECT_METHOD, "[" + LJAVA_LANG_OBJECT });

        if (methodInfo.getReturnType().equals(BytecodeUtils.VOID_CLASS_DESCRIPTOR)) {
            b.returnInstruction();
        } else if (isPrimitive(methodInfo.getReturnType())) {
            Boxing.unbox(b, classMethod.getReturnType());
            b.returnInstruction();
        } else {
            b.checkcast(BytecodeUtils.getName(methodInfo.getReturnType()));
            b.returnInstruction();
        }
    }

    private void createInterceptedMethod(ClassMethod classMethod, final MethodInformation methodInfo, Method method, ClassMethod staticConstructor) {
        final CodeAttribute b = classMethod.getCodeAttribute();

        b.aload(0);
        getMethodHandlerField(classMethod.getClassFile(), b);

        // get the Stack
        b.invokestatic(InterceptionDecorationContext.class.getName(), "getStack", "()" + DescriptorUtils.makeDescriptor(Stack.class));

        b.aload(0);
        DEFAULT_METHOD_RESOLVER.getDeclaredMethod(classMethod, methodInfo.getDeclaringClass(), method.getName(),
                methodInfo.getParameterTypes(), staticConstructor);
        b.dup();
        // Params
        b.iconst(method.getParameterCount());
        b.anewarray(JAVA_LANG_OBJECT);
        int localVariableCount = 1;
        for (int i = 0; i < method.getParameterCount(); ++i) {
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

        b.invokeinterface(StackAwareMethodHandler.class.getName(), INVOKE_METHOD_NAME, LJAVA_LANG_OBJECT,
                InterceptedSubclassFactory.INVOKE_METHOD_PARAMETERS);

        if (methodInfo.getReturnType().equals(BytecodeUtils.VOID_CLASS_DESCRIPTOR)) {
            b.returnInstruction();
        } else if (isPrimitive(methodInfo.getReturnType())) {
            Boxing.unbox(b, classMethod.getReturnType());
            b.returnInstruction();
        } else {
            b.checkcast(BytecodeUtils.getName(methodInfo.getReturnType()));
            b.returnInstruction();
        }
    }

    private void addInterfaceToInspect(Class<?> iface) {
        if (interfacesToInspect == null) {
            interfacesToInspect = new HashSet<>();
        }
        this.interfacesToInspect.add(iface);
    }
}
