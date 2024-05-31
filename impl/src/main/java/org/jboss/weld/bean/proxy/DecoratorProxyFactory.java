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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.util.DescriptorUtils;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.RuntimeMethodInformation;
import org.jboss.weld.util.bytecode.StaticMethodInformation;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This special proxy factory is mostly used for abstract decorators. When a
 * delegate field is injected, the abstract methods directly invoke the
 * corresponding method on the delegate. All other cases forward the calls to
 * the {@link BeanInstance} for further processing.
 *
 * @author David Allen
 * @author Stuart Douglas
 */
public class DecoratorProxyFactory<T> extends ProxyFactory<T> {

    public static final String PROXY_SUFFIX = "DecoratorProxy";
    private static final String INIT_MH_METHOD_NAME = "_initMH";
    private final WeldInjectionPointAttributes<?, ?> delegateInjectionPoint;
    private final Field delegateField;
    private final TargetInstanceBytecodeMethodResolver targetInstanceBytecodeMethodResolver = new TargetInstanceBytecodeMethodResolver();

    public DecoratorProxyFactory(String contextId, Class<T> proxyType,
            WeldInjectionPointAttributes<?, ?> delegateInjectionPoint, Bean<?> bean) {
        super(contextId, proxyType, Collections.<Type> emptySet(), bean);
        this.delegateInjectionPoint = delegateInjectionPoint;
        if (delegateInjectionPoint instanceof FieldInjectionPoint<?, ?>) {
            delegateField = (Field) ((FieldInjectionPoint<?, ?>) delegateInjectionPoint).getMember();
        } else {
            delegateField = null;
        }
    }

    /**
     * calls _initMH on the method handler and then stores the result in the
     * methodHandler field as then new methodHandler
     */
    private void addHandlerInitializerMethod(ClassFile proxyClassType, ClassMethod staticConstructor) throws Exception {
        ClassMethod classMethod = proxyClassType.addMethod(AccessFlag.PRIVATE, INIT_MH_METHOD_NAME,
                BytecodeUtils.VOID_CLASS_DESCRIPTOR, LJAVA_LANG_OBJECT);
        final CodeAttribute b = classMethod.getCodeAttribute();
        b.aload(0);
        StaticMethodInformation methodInfo = new StaticMethodInformation(INIT_MH_METHOD_NAME, new Class[] { Object.class },
                void.class,
                classMethod.getClassFile().getName());
        invokeMethodHandler(classMethod, methodInfo, false, DEFAULT_METHOD_RESOLVER, staticConstructor);
        b.checkcast(MethodHandler.class);
        b.putfield(classMethod.getClassFile().getName(), METHOD_HANDLER_FIELD_NAME,
                DescriptorUtils.makeDescriptor(MethodHandler.class));
        b.returnInstruction();
        BeanLogger.LOG.createdMethodHandlerInitializerForDecoratorProxy(getBeanType());

    }

    @Override
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
        interfaces.add(DecoratorProxy.class);
    }

    @Override
    protected void addMethodsFromClass(ClassFile proxyClassType, ClassMethod staticConstructor) {
        Method initializerMethod = null;
        int delegateParameterPosition = -1;
        if (delegateInjectionPoint instanceof ParameterInjectionPoint<?, ?>) {
            ParameterInjectionPoint<?, ?> parameterIP = (ParameterInjectionPoint<?, ?>) delegateInjectionPoint;
            if (parameterIP.getMember() instanceof Method) {
                initializerMethod = ((Method) parameterIP.getMember());
                delegateParameterPosition = parameterIP.getAnnotated().getPosition();
            }
        }
        try {
            if (delegateParameterPosition >= 0) {
                addHandlerInitializerMethod(proxyClassType, staticConstructor);
            }
            Class<?> cls = getBeanType();
            Set<Method> methods = new LinkedHashSet<Method>();
            decoratorMethods(cls, methods);
            for (Method method : methods) {
                MethodInformation methodInfo = new RuntimeMethodInformation(method);
                if (!method.getDeclaringClass().getName().equals("java.lang.Object") || method.getName().equals("toString")) {

                    if ((delegateParameterPosition >= 0) && (initializerMethod.equals(method))) {
                        createDelegateInitializerCode(proxyClassType.addMethod(method), methodInfo, delegateParameterPosition);
                    }
                    // exclude bridge methods
                    if (Modifier.isAbstract(method.getModifiers())) {
                        createAbstractMethodCode(proxyClassType.addMethod(method), methodInfo, staticConstructor);
                    }
                }
            }
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    private void decoratorMethods(Class<?> cls, Set<Method> all) {
        if (cls == null) {
            return;
        }
        all.addAll(Arrays.asList(cls.getDeclaredMethods()));

        decoratorMethods(cls.getSuperclass(), all);

        // by now we should have all declared methods, let's only add the missing ones
        for (Class<?> ifc : cls.getInterfaces()) {
            Method[] methods = ifc.getMethods();
            for (Method m : methods) {
                boolean isEqual = false;
                for (Method a : all) {
                    if (isEqual(m, a)) {
                        isEqual = true;
                        break;
                    }
                }
                if (!isEqual) {
                    all.add(m);
                }
            }
        }
    }

    // m is more generic than a
    private static boolean isEqual(Method m, Method a) {
        if (m.getName().equals(a.getName()) && m.getParameterCount() == a.getParameterCount()
                && m.getReturnType().isAssignableFrom(a.getReturnType())) {
            for (int i = 0; i < m.getParameterCount(); i++) {
                if (!(m.getParameterTypes()[i].isAssignableFrom(a.getParameterTypes()[i]))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected String getProxyNameSuffix() {
        return PROXY_SUFFIX;
    }

    @Override
    protected boolean isUsingProxyInstantiator() {
        return false;
    }

    private void createAbstractMethodCode(ClassMethod classMethod, MethodInformation method, ClassMethod staticConstructor) {
        if ((delegateField != null) && (!Modifier.isPrivate(delegateField.getModifiers()))) {
            // Call the corresponding method directly on the delegate
            final CodeAttribute b = classMethod.getCodeAttribute();
            // load the delegate field
            b.aload(0);
            b.getfield(classMethod.getClassFile().getName(), delegateField.getName(),
                    DescriptorUtils.makeDescriptor(delegateField.getType()));
            // load the parameters
            b.loadMethodParameters();
            // invoke the delegate method
            b.invokeinterface(delegateField.getType().getName(), method.getName(), method.getDescriptor());
            // return the value if applicable
            b.returnInstruction();
        } else {
            if (!Modifier.isPrivate(method.getMethod().getModifiers())) {
                // if it is a parameter injection point we need to initialize the
                // injection point then handle the method with the method handler

                // this is slightly different to a normal method handler call, as we pass
                // in a TargetInstanceBytecodeMethodResolver. This resolver uses the
                // method handler to call getTargetClass to get the correct class type to
                // resolve the method with, and then resolves this method

                invokeMethodHandler(classMethod, method, true, targetInstanceBytecodeMethodResolver, staticConstructor);
            } else {
                // if the delegate is private we need to use the method handler
                createInterceptorBody(classMethod, method, staticConstructor);
            }
        }
    }

    /**
     * When creates the delegate initializer code when the delegate is injected
     * into a method.
     * <p/>
     * super initializer method is called first, and then _initMH is called
     *
     * @param initializerMethodInfo
     * @param delegateParameterPosition
     * @return
     */
    private void createDelegateInitializerCode(ClassMethod classMethod, MethodInformation initializerMethodInfo,
            int delegateParameterPosition) {
        final CodeAttribute b = classMethod.getCodeAttribute();
        // we need to push all the parameters on the stack to call the corresponding
        // superclass arguments
        b.aload(0); // load this
        int localVariables = 1;
        int actualDelegateParameterPosition = 0;
        for (int i = 0; i < initializerMethodInfo.getMethod().getParameterCount(); ++i) {
            if (i == delegateParameterPosition) {
                // figure out the actual position of the delegate in the local
                // variables
                actualDelegateParameterPosition = localVariables;
            }
            Class<?> type = initializerMethodInfo.getMethod().getParameterTypes()[i];
            BytecodeUtils.addLoadInstruction(b, DescriptorUtils.makeDescriptor(type), localVariables);
            if (type == long.class || type == double.class) {
                localVariables = localVariables + 2;
            } else {
                localVariables++;
            }
        }
        b.invokespecial(classMethod.getClassFile().getSuperclass(), initializerMethodInfo.getName(),
                initializerMethodInfo.getDescriptor());
        // if this method returns a value it is now sitting on top of the stack
        // we will leave it there are return it later

        // now we need to call _initMH
        b.aload(0); // load this
        b.aload(actualDelegateParameterPosition); // load the delegate
        b.invokevirtual(classMethod.getClassFile().getName(), INIT_MH_METHOD_NAME,
                "(" + LJAVA_LANG_OBJECT + ")" + BytecodeUtils.VOID_CLASS_DESCRIPTOR);
        // return the object from the top of the stack that we got from calling
        // the superclass method earlier
        b.returnInstruction();

    }

    protected class TargetInstanceBytecodeMethodResolver implements BytecodeMethodResolver {
        private static final String JAVA_LANG_CLASS_CLASS_NAME = "java.lang.Class";

        public void getDeclaredMethod(ClassMethod classMethod, String declaringClass, String methodName,
                String[] parameterTypes, ClassMethod staticConstructor) {
            // get the correct class type to use to resolve the method
            MethodInformation methodInfo = new StaticMethodInformation("weld_getTargetClass", new String[0], LJAVA_LANG_CLASS,
                    TargetInstanceProxy.class.getName());
            invokeMethodHandler(classMethod, methodInfo, false, DEFAULT_METHOD_RESOLVER, staticConstructor);
            CodeAttribute code = classMethod.getCodeAttribute();
            code.checkcast("java/lang/Class");
            // now we have the class on the stack
            code.ldc(methodName);
            // now we need to load the parameter types into an array
            code.iconst(parameterTypes.length);
            code.anewarray(JAVA_LANG_CLASS_CLASS_NAME);
            for (int i = 0; i < parameterTypes.length; ++i) {
                code.dup(); // duplicate the array reference
                code.iconst(i);
                // now load the class object
                String type = parameterTypes[i];
                BytecodeUtils.pushClassType(code, type);
                // and store it in the array
                code.aastore();
            }
            code.invokestatic(Reflections.class.getName(), "wrapException",
                    "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            code.checkcast(Method.class);
        }

    }

}
