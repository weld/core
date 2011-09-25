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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;

import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.util.proxy.MethodHandler;
import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.util.SerializableClientProxy;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.bytecode.BytecodeUtils;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.JumpMarker;
import org.jboss.weld.util.bytecode.JumpUtils;
import org.jboss.weld.util.bytecode.MethodInformation;
import org.jboss.weld.util.bytecode.MethodUtils;
import org.jboss.weld.util.bytecode.StaticMethodInformation;

/**
 * Proxy factory that generates client proxies, it uses optimizations that
 * are not valid for other proxy types.
 *
 * @author Stuart Douglas
 * @author Marius Bogoevici
 */
public class ClientProxyFactory<T> extends ProxyFactory<T> {

    private static final Set<Class<? extends Annotation>> CACHABLE_SCOPES;

    public static final String CLIENT_PROXY_SUFFIX = "ClientProxy";

    private static final String CACHE_FIELD = "BEAN_INSTANCE_CACHE";

    /**
     * It is possible although very unlikely that two different beans will end up with the same proxy class
     * (generally this will only happen in test situations where weld is being started/stopped multiple times
     * in the same class loader, such as during unit tests)
     *
     * To avoid this causing serialization problems we explicitly set the bean id on creation, and store it in this
     * field.
     *
     *
     */
    private static final String BEAN_ID_FIELD = "BEAN_ID_FIELD";

    private final String beanId;

    private volatile Field beanIdField;

    static {
        Set<Class<? extends Annotation>> scopes = new HashSet<Class<? extends Annotation>>();
        scopes.add(RequestScoped.class);
        scopes.add(ConversationScoped.class);
        scopes.add(SessionScoped.class);
        scopes.add(ApplicationScoped.class);
        CACHABLE_SCOPES = Collections.unmodifiableSet(scopes);
    }

    public ClientProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        super(contextId, proxiedBeanType, typeClosure, bean);
        beanId = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
    }

    public ClientProxyFactory(String contextId, Class<?> proxiedBeanType, Set<? extends Type> typeClosure, String proxyName, Bean<?> bean) {
        super(contextId, proxiedBeanType, typeClosure, proxyName, bean);
        beanId = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
    }

    @Override
    public T create(BeanInstance beanInstance) {
        try {
            final T instance = super.create(beanInstance);
            if (beanIdField == null) {
                final Field f = instance.getClass().getDeclaredField(BEAN_ID_FIELD);
                f.setAccessible(true);
                beanIdField = f;
            }
            beanIdField.set(instance, beanId);
            return instance;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void addFields(ClassFile proxyClassType, Bytecode initialValueBytecode) {
        super.addFields(proxyClassType, initialValueBytecode);
        if (CACHABLE_SCOPES.contains(getBean().getScope())) {
            try {
                FieldInfo sfield = new FieldInfo(proxyClassType.getConstPool(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
                sfield.setAccessFlags(AccessFlag.TRANSIENT | AccessFlag.PRIVATE);
                proxyClassType.addField(sfield);
                initialValueBytecode.addAload(0);
                initialValueBytecode.addNew(ThreadLocal.class.getName());
                initialValueBytecode.add(Opcode.DUP);
                initialValueBytecode.addInvokespecial(ThreadLocal.class.getName(), "<init>", "()V");
                initialValueBytecode.addPutfield(proxyClassType.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
            } catch (DuplicateMemberException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            FieldInfo beanIdField = new FieldInfo(proxyClassType.getConstPool(), BEAN_ID_FIELD, "Ljava/lang/String;");
            beanIdField.setAccessFlags(AccessFlag.VOLATILE | AccessFlag.PRIVATE);
            proxyClassType.addField(beanIdField);
        } catch (DuplicateMemberException e) {
            throw new RuntimeException(e);
        }
    }

    protected void addSerializationSupport(ClassFile proxyClassType) {
        try {
            Class<?>[] exceptions = new Class[]{ObjectStreamException.class};
            Bytecode writeReplaceBody = createWriteReplaceBody(proxyClassType);
            MethodInformation writeReplaceInfo = new StaticMethodInformation("writeReplace", new Class[]{}, Object.class, proxyClassType.getName());
            proxyClassType.addMethod(MethodUtils.makeMethod(writeReplaceInfo, exceptions, writeReplaceBody, proxyClassType.getConstPool()));
        } catch (DuplicateMemberException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates serialization code that returns a SerializableClientProxy
     */
    private Bytecode createWriteReplaceBody(ClassFile proxyClassType) {
        Bytecode b = new Bytecode(proxyClassType.getConstPool());
        b.addNew(SerializableClientProxy.class.getName());
        b.add(Opcode.DUP);
        b.add(Opcode.ALOAD_0);
        b.addGetfield(proxyClassType.getName(), BEAN_ID_FIELD, "Ljava/lang/String;");
        b.addInvokespecial(SerializableClientProxy.class.getName(), "<init>", "(Ljava/lang/String;)V");
        b.add(Opcode.ARETURN);
        b.setMaxLocals(1);
        return b;
    }


    /**
     * Calls methodHandler.invoke with a null method parameter in order to
     * get the underlying instance. The invocation is then forwarded to
     * this instance with generated bytecode.
     */
    protected Bytecode createForwardingMethodBody(ClassFile file, MethodInformation methodInfo) throws NotFoundException {
        Method method = methodInfo.getMethod();
        // we can only use bytecode based invocation for some methods
        // at the moment we restrict it solely to public methods with public
        // return and parameter types
        boolean bytecodeInvocationAllowed = Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(method.getReturnType().getModifiers());
        for (Class<?> paramType : method.getParameterTypes()) {
            if (!Modifier.isPublic(paramType.getModifiers())) {
                bytecodeInvocationAllowed = false;
                break;
            }
        }
        if (!bytecodeInvocationAllowed) {
            return createInterceptorBody(file, methodInfo);
        }
        Bytecode b = new Bytecode(file.getConstPool());
        int localCount = MethodUtils.calculateMaxLocals(method) + 1;

        // create a new interceptor invocation context whenever we invoke a method on a client proxy
        // we use a try-catch block in order to make sure that endInterceptorContext() is invoked regardless whether
        // the method has succeeded or not
        int start = b.currentPc();
        b.addInvokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "startInterceptorContext", "()V");

        final Class<? extends Annotation> scope = getBean().getScope();

        if (CACHABLE_SCOPES.contains(scope)) {
            loadCachableBeanInstance(file, methodInfo, b);
        } else {
            loadBeanInstance(file, methodInfo, b);
        }
        //now we should have the target bean instance on top of the stack
        // we need to dup it so we still have it to compare to the return value
        b.add(Opcode.DUP);

        //lets create the method invocation
        String methodDescriptor = methodInfo.getDescriptor();
        BytecodeUtils.loadParameters(b, methodDescriptor);
        if (method.getDeclaringClass().isInterface()) {
            b.addInvokeinterface(methodInfo.getDeclaringClass(), methodInfo.getName(), methodDescriptor, method.getParameterTypes().length + 1);
        } else {
            b.addInvokevirtual(methodInfo.getDeclaringClass(), methodInfo.getName(), methodDescriptor);
        }

        // end the interceptor context, everything was fine
        b.addInvokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "endInterceptorContext", "()V");

        // jump over the catch block
        b.addOpcode(Opcode.GOTO);
        JumpMarker gotoEnd = JumpUtils.addJumpInstruction(b);

        // create catch block
        b.addExceptionHandler(start, b.currentPc(), b.currentPc(), 0);
        b.addInvokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "endInterceptorContext", "()V");
        b.add(Opcode.ATHROW);

        // update the correct address to jump over the catch block
        gotoEnd.mark();

        // if this method returns a primitive we just return
        if (method.getReturnType().isPrimitive()) {
            BytecodeUtils.addReturnInstruction(b, methodInfo.getReturnType());
        } else {
            // otherwise we have to check that the proxy is not returning 'this;
            // now we need to check if the proxy has return 'this' and if so return
            // an
            // instance of the proxy.
            // currently we have result, beanInstance on the stack.
            b.add(Opcode.DUP_X1);
            // now we have result, beanInstance, result
            // we need to compare result and beanInstance

            // first we need to build up the inner conditional that just returns
            // the
            // result
            b.add(Opcode.IF_ACMPEQ);
            JumpMarker returnInstruction = JumpUtils.addJumpInstruction(b);
            BytecodeUtils.addReturnInstruction(b, methodInfo.getReturnType());
            returnInstruction.mark();

            // now add the case where the proxy returns 'this';
            b.add(Opcode.ALOAD_0);
            b.addCheckcast(methodInfo.getMethod().getReturnType().getName());
            BytecodeUtils.addReturnInstruction(b, methodInfo.getReturnType());
        }
        if (b.getMaxLocals() < localCount) {
            b.setMaxLocals(localCount);
        }
        return b;
    }

    /**
     * If the bean is part of a well known scope then this code caches instances in a thread local for the life of the
     * request, as a performance enhancement.
     */
    private void loadCachableBeanInstance(ClassFile file, MethodInformation methodInfo, Bytecode b) {
        //first we need to see if the scope is active
        b.addInvokestatic(RequestScopedBeanCache.class.getName(), "isActive", "()Z");
        //if it is not active we just get the bean directly


        b.add(Opcode.IFEQ);
        final JumpMarker returnInstruction = JumpUtils.addJumpInstruction(b);
        //get the bean from the cache
        b.addAload(0);
        b.addGetfield(file.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
        b.addInvokevirtual(ThreadLocal.class.getName(), "get", "()Ljava/lang/Object;");
        b.add(Opcode.DUP);
        b.add(Opcode.IFNULL);
        final JumpMarker createNewInstance = JumpUtils.addJumpInstruction(b);
        //so we have a not-null bean instance in the cache
        b.addCheckcast(methodInfo.getDeclaringClass());
        b.add(Opcode.GOTO);
        final JumpMarker loadedFromCache = JumpUtils.addJumpInstruction(b);
        createNewInstance.mark();
        //we need to get a bean instance and cache it
        //first clear the null off the top of the stack
        b.add(Opcode.POP);
        loadBeanInstance(file, methodInfo, b);
        b.add(Opcode.DUP);
        b.addAload(0);
        b.addGetfield(file.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
        b.add(Opcode.DUP_X1);
        b.add(Opcode.SWAP);
        b.addInvokevirtual(ThreadLocal.class.getName(), "set", "(Ljava/lang/Object;)V");
        b.addInvokestatic(RequestScopedBeanCache.class.getName(), "addItem", "(Ljava/lang/ThreadLocal;)V");
        b.add(Opcode.GOTO);
        final JumpMarker endOfIfStatement = JumpUtils.addJumpInstruction(b);
        returnInstruction.mark();
        loadBeanInstance(file, methodInfo, b);
        endOfIfStatement.mark();
        loadedFromCache.mark();
    }

    private void loadBeanInstance(ClassFile file, MethodInformation methodInfo, Bytecode b) {
        b.add(Opcode.ALOAD_0);
        b.addGetfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
        //pass null arguments to methodHandler.invoke
        b.add(Opcode.ALOAD_0);
        b.add(Opcode.ACONST_NULL);
        b.add(Opcode.ACONST_NULL);
        b.add(Opcode.ACONST_NULL);

        // now we have all our arguments on the stack
        // lets invoke the method
        b.addInvokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", 5);

        b.addCheckcast(methodInfo.getDeclaringClass());
    }

    /**
     * Client proxies use the following hashCode:
     * <code>MyProxyName.class.hashCode()</code>
     */
    @Override
    protected MethodInfo generateHashCodeMethod(ClassFile proxyClassType) {
        MethodInfo method = new MethodInfo(proxyClassType.getConstPool(), "hashCode", "()I");
        method.setAccessFlags(AccessFlag.PUBLIC);
        Bytecode b = new Bytecode(proxyClassType.getConstPool());
        // MyProxyName.class.hashCode()
        int classLocation = proxyClassType.getConstPool().addClassInfo(proxyClassType.getName());
        b.addLdc(classLocation);
        // now we have the class object on top of the stack
        b.addInvokevirtual("java.lang.Object", "hashCode", "()I");
        // now we have the hashCode
        b.add(Opcode.IRETURN);
        b.setMaxLocals(1);
        b.setMaxStack(1);
        method.setCodeAttribute(b.toCodeAttribute());
        return method;
    }

    /**
     * Client proxies are equal to other client proxies for the same bean.
     * <p/>
     * The corresponding java code: <code>
     * return other instanceof MyProxyClassType.class
     * </code>
     */
    @Override
    protected MethodInfo generateEqualsMethod(ClassFile proxyClassType) {
        MethodInfo method = new MethodInfo(proxyClassType.getConstPool(), "equals", "(Ljava/lang/Object;)Z");
        method.setAccessFlags(AccessFlag.PUBLIC);
        Bytecode b = new Bytecode(proxyClassType.getConstPool());
        b.addAload(1);
        b.addInstanceof(proxyClassType.getName());
        b.add(Opcode.IRETURN);
        b.setMaxLocals(2);
        b.setMaxStack(1);
        method.setCodeAttribute(b.toCodeAttribute());
        return method;
    }

    @Override
    protected String getProxyNameSuffix() {
        return CLIENT_PROXY_SUFFIX;
    }

}
