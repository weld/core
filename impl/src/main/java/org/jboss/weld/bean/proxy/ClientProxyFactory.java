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

import java.io.ObjectStreamException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.util.SerializableClientProxy;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.serialization.spi.BeanIdentifier;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.bytecode.MethodInformation;

/**
 * Proxy factory that generates client proxies, it uses optimizations that
 * are not valid for other proxy types.
 *
 * @author Stuart Douglas
 * @author Marius Bogoevici
 */
public class ClientProxyFactory<T> extends ProxyFactory<T> {

    private static final String CLIENT_PROXY_SUFFIX = "ClientProxy";

    private static final String HASH_CODE_METHOD = "hashCode";
    private static final String EMPTY_PARENTHESES = "()";

    /**
     * It is possible although very unlikely that two different beans will end up with the same proxy class
     * (generally this will only happen in test situations where weld is being started/stopped multiple times
     * in the same class loader, such as during unit tests)
     * <p/>
     * To avoid this causing serialization problems we explicitly set the bean id on creation, and store it in this
     * field.
     */
    private static final String BEAN_ID_FIELD = "BEAN_ID_FIELD";
    private static final String CONTEXT_ID_FIELD = "CONTEXT_ID_FIELD";

    private final BeanIdentifier beanId;

    private volatile Field beanIdField;
    private volatile Field contextIdField;

    public ClientProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        super(contextId, proxiedBeanType, typeClosure, bean);
        beanId = Container.instance(contextId).services().get(ContextualStore.class).putIfAbsent(bean);
    }

    @Override
    public T create(BeanInstance beanInstance) {
        try {
            final T instance = super.create(beanInstance);
            if (beanIdField == null) {
                final Field f = SecurityActions.getDeclaredField(instance.getClass(), BEAN_ID_FIELD);
                SecurityActions.ensureAccessible(f);
                beanIdField = f;
            }
            if (contextIdField == null) {
                final Field f = SecurityActions.getDeclaredField(instance.getClass(), CONTEXT_ID_FIELD);
                SecurityActions.ensureAccessible(f);
                contextIdField = f;
            }
            beanIdField.set(instance, beanId);
            contextIdField.set(instance, getContextId());
            return instance;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    protected void addAdditionalInterfaces(Set<Class<?>> interfaces) {
        // add marker interface for client proxy, this also requires adding interface methods implementations
        interfaces.add(WeldClientProxy.class);
    }

    @Override
    protected void addMethods(ClassFile proxyClassType, ClassMethod staticConstructor) {
        // delegate to ProxyFactory#addMethods
        super.addMethods(proxyClassType, staticConstructor);

        // add method from WeldClientProxy
        generateWeldClientProxyMethod(proxyClassType);
    }

    private void generateWeldClientProxyMethod(ClassFile proxyClassType) {
        try {
            Method getContextualMetadata = WeldClientProxy.class.getMethod("getMetadata");
            generateBodyForWeldClientProxyMethod(proxyClassType.addMethod(getContextualMetadata));
        } catch (Exception e) {
            throw new WeldException(e);
        }
    }

    private void generateBodyForWeldClientProxyMethod(ClassMethod method) throws Exception {
        // ProxyMethodHandler implements ContextualMetadata, so let's just return reference to it
        final CodeAttribute b = method.getCodeAttribute();
        b.aload(0);
        getMethodHandlerField(method.getClassFile(), b);
        b.returnInstruction();
    }

    @Override
    protected void addFields(final ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode) {
        super.addFields(proxyClassType, initialValueBytecode);
        proxyClassType.addField(AccessFlag.VOLATILE | AccessFlag.PRIVATE, BEAN_ID_FIELD, BeanIdentifier.class);
        proxyClassType.addField(AccessFlag.VOLATILE | AccessFlag.PRIVATE, CONTEXT_ID_FIELD, String.class);
    }

    @Override
    protected Class<? extends MethodHandler> getMethodHandlerType() {
        return ProxyMethodHandler.class;
    }

    @Override
    protected void addSerializationSupport(ClassFile proxyClassType) {
        final Class<Exception>[] exceptions = new Class[] { ObjectStreamException.class };
        final ClassMethod writeReplace = proxyClassType.addMethod(AccessFlag.PRIVATE, "writeReplace", LJAVA_LANG_OBJECT);
        writeReplace.addCheckedExceptions(exceptions);

        CodeAttribute b = writeReplace.getCodeAttribute();
        b.newInstruction(SerializableClientProxy.class.getName());
        b.dup();
        b.aload(0);
        b.getfield(proxyClassType.getName(), BEAN_ID_FIELD, BeanIdentifier.class);
        b.aload(0);
        b.getfield(proxyClassType.getName(), CONTEXT_ID_FIELD, String.class);
        b.invokespecial(SerializableClientProxy.class.getName(), INIT_METHOD_NAME,
                "(" + LBEAN_IDENTIFIER + LJAVA_LANG_STRING + ")" + BytecodeUtils.VOID_CLASS_DESCRIPTOR);
        b.returnInstruction();
    }

    /**
     * Calls methodHandler.invoke with a null method parameter in order to
     * get the underlying instance. The invocation is then forwarded to
     * this instance with generated bytecode.
     */
    @Override
    protected void createForwardingMethodBody(ClassMethod classMethod, final MethodInformation methodInfo,
            ClassMethod staticConstructor) {
        final Method method = methodInfo.getMethod();
        // we can only use bytecode based invocation for some methods
        // at the moment we restrict it solely to public methods with public
        // return and parameter types
        boolean bytecodeInvocationAllowed = Modifier.isPublic(method.getModifiers())
                && Modifier.isPublic(method.getReturnType().getModifiers());
        for (Class<?> paramType : method.getParameterTypes()) {
            if (!Modifier.isPublic(paramType.getModifiers())) {
                bytecodeInvocationAllowed = false;
                break;
            }
        }
        if (!bytecodeInvocationAllowed) {
            createInterceptorBody(classMethod, methodInfo, staticConstructor);
            return;
        }

        // create a new interceptor invocation context whenever we invoke a method on a client proxy
        // we use a try-catch block in order to make sure that endInterceptorContext() is invoked regardless whether
        // the method has succeeded or not

        new RunWithinInterceptionDecorationContextGenerator(classMethod, this) {

            @Override
            void doWork(CodeAttribute b, ClassMethod classMethod) {
                loadBeanInstance(classMethod.getClassFile(), methodInfo, b);
                //now we should have the target bean instance on top of the stack
                // we need to dup it so we still have it to compare to the return value
                b.dup();

                //lets create the method invocation
                String methodDescriptor = methodInfo.getDescriptor();
                b.loadMethodParameters();
                if (method.getDeclaringClass().isInterface()) {
                    b.invokeinterface(methodInfo.getDeclaringClass(), methodInfo.getName(), methodDescriptor);
                } else {
                    b.invokevirtual(methodInfo.getDeclaringClass(), methodInfo.getName(), methodDescriptor);
                }
            }

            @Override
            void doReturn(CodeAttribute b, ClassMethod classMethod) {
                // assumes doWork() result is on top of the stack
                // if this method returns a primitive we just return
                if (method.getReturnType().isPrimitive()) {
                    b.returnInstruction();
                } else {
                    // otherwise we have to check that the proxy is not returning 'this;
                    // now we need to check if the proxy has return 'this' and if so return
                    // an
                    // instance of the proxy.
                    // currently we have result, beanInstance on the stack.
                    b.dupX1();
                    // now we have result, beanInstance, result
                    // we need to compare result and beanInstance

                    // first we need to build up the inner conditional that just returns
                    // the
                    // result
                    final BranchEnd returnInstruction = b.ifAcmpeq();
                    b.returnInstruction();
                    b.branchEnd(returnInstruction);

                    // now add the case where the proxy returns 'this';
                    b.aload(0);
                    b.checkcast(methodInfo.getMethod().getReturnType().getName());
                    b.returnInstruction();
                }
            }
        }.runStartIfNotEmpty();
    }

    private void loadBeanInstance(ClassFile file, MethodInformation methodInfo, CodeAttribute b) {
        b.aload(0);
        getMethodHandlerField(file, b);
        // lets invoke the method
        b.invokevirtual(ProxyMethodHandler.class.getName(), "getInstance", EMPTY_PARENTHESES + LJAVA_LANG_OBJECT);
        b.checkcast(methodInfo.getDeclaringClass());
    }

    /**
     * Client proxies use the following hashCode:
     * <code>MyProxyName.class.hashCode()</code>
     */
    @Override
    protected void generateHashCodeMethod(ClassFile proxyClassType) {
        final ClassMethod method = proxyClassType.addMethod(AccessFlag.PUBLIC, HASH_CODE_METHOD,
                BytecodeUtils.INT_CLASS_DESCRIPTOR);
        final CodeAttribute b = method.getCodeAttribute();
        // MyProxyName.class.hashCode()
        b.loadClass(proxyClassType.getName());
        // now we have the class object on top of the stack
        b.invokevirtual("java.lang.Object", HASH_CODE_METHOD, EMPTY_PARENTHESES + BytecodeUtils.INT_CLASS_DESCRIPTOR);
        // now we have the hashCode
        b.returnInstruction();
    }

    /**
     * Client proxies are equal to other client proxies for the same bean.
     * <p/>
     * The corresponding java code: <code>
     * return other instanceof MyProxyClassType.class
     * </code>
     */
    @Override
    protected void generateEqualsMethod(ClassFile proxyClassType) {
        ClassMethod method = proxyClassType.addMethod(AccessFlag.PUBLIC, "equals", BytecodeUtils.BOOLEAN_CLASS_DESCRIPTOR,
                LJAVA_LANG_OBJECT);
        CodeAttribute b = method.getCodeAttribute();
        b.aload(1);
        b.instanceofInstruction(proxyClassType.getName());
        b.returnInstruction();
    }

    @Override
    protected String getProxyNameSuffix() {
        return CLIENT_PROXY_SUFFIX;
    }

    @Override
    protected boolean isMethodAccepted(Method method, Class<?> proxySuperclass) {
        return super.isMethodAccepted(method, proxySuperclass)
                && CommonProxiedMethodFilters.NON_PRIVATE.accept(method, proxySuperclass);
    }
}
