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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.Bean;

import org.jboss.classfilewriter.AccessFlag;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.ClassMethod;
import org.jboss.classfilewriter.DuplicateMemberException;
import org.jboss.classfilewriter.code.BranchEnd;
import org.jboss.classfilewriter.code.CodeAttribute;
import org.jboss.classfilewriter.code.ExceptionHandler;
import org.jboss.weld.Container;
import org.jboss.weld.bean.proxy.util.SerializableClientProxy;
import org.jboss.weld.context.cache.RequestScopedBeanCache;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.bytecode.DeferredBytecode;
import org.jboss.weld.util.bytecode.DescriptorUtils;
import org.jboss.weld.util.bytecode.MethodInformation;

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

    private final String beanId;

    static {
        Set<Class<? extends Annotation>> scopes = new HashSet<Class<? extends Annotation>>();
        scopes.add(RequestScoped.class);
        scopes.add(ConversationScoped.class);
        scopes.add(SessionScoped.class);
        scopes.add(ApplicationScoped.class);
        CACHABLE_SCOPES = Collections.unmodifiableSet(scopes);
    }

    public ClientProxyFactory(Class<?> proxiedBeanType, Set<? extends Type> typeClosure, Bean<?> bean) {
        super(proxiedBeanType, typeClosure, bean);
        beanId = Container.instance().services().get(ContextualStore.class).putIfAbsent(bean);
    }

    @Override
    protected void addFields(final ClassFile proxyClassType, List<DeferredBytecode> initialValueBytecode) {
        super.addFields(proxyClassType, initialValueBytecode);
        if (CACHABLE_SCOPES.contains(getBean().getScope())) {
            try {
                proxyClassType.addField(AccessFlag.TRANSIENT | AccessFlag.PRIVATE, CACHE_FIELD, "Ljava/lang/ThreadLocal;");
                initialValueBytecode.add(new DeferredBytecode() {
                    public void apply(final CodeAttribute codeAttribute) {

                        codeAttribute.aload(0);
                        codeAttribute.newInstruction(ThreadLocal.class.getName());
                        codeAttribute.dup();
                        codeAttribute.invokespecial(ThreadLocal.class.getName(), "<init>", "()V");
                        codeAttribute.putfield(proxyClassType.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
                    }
                });
            } catch (DuplicateMemberException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void addSerializationSupport(ClassFile proxyClassType) {

        final Class<Exception>[] exceptions = new Class[]{ObjectStreamException.class};
        final ClassMethod writeReplace = proxyClassType.addMethod(AccessFlag.PRIVATE, "writeReplace", "Ljava/lang/Object;");
        writeReplace.addCheckedExceptions(exceptions);

        CodeAttribute b = writeReplace.getCodeAttribute();
        b.newInstruction(SerializableClientProxy.class.getName());
        b.dup();
        b.ldc(beanId);
        b.invokespecial(SerializableClientProxy.class.getName(), "<init>", "(Ljava/lang/String;)V");
        b.returnInstruction();
    }


    /**
     * Calls methodHandler.invoke with a null method parameter in order to
     * get the underlying instance. The invocation is then forwarded to
     * this instance with generated bytecode.
     */
    @Override
    protected void createForwardingMethodBody(ClassMethod classMethod, MethodInformation methodInfo){
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
            createInterceptorBody(classMethod, methodInfo);
            return;
        }
        final CodeAttribute b = classMethod.getCodeAttribute();

        // create a new interceptor invocation context whenever we invoke a method on a client proxy
        // we use a try-catch block in order to make sure that endInterceptorContext() is invoked regardless whether
        // the method has succeeded or not

        final ExceptionHandler start = b.exceptionBlockStart(Throwable.class.getName());
        b.invokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "startInterceptorContext", "()V");

        final Class<? extends Annotation> scope = getBean().getScope();

        if (CACHABLE_SCOPES.contains(scope)) {
            loadCachableBeanInstance(classMethod.getClassFile(), methodInfo, b);
        } else {
            loadBeanInstance(classMethod.getClassFile(), methodInfo, b);
        }
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

        // end the interceptor context, everything was fine
        b.invokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "endInterceptorContext", "()V");

        // jump over the catch block
        BranchEnd gotoEnd = b.gotoInstruction();

        // create catch block
        b.exceptionBlockEnd(start);
        b.exceptionHandlerStart(start);
        b.invokestatic("org.jboss.weld.bean.proxy.InterceptionDecorationContext", "endInterceptorContext", "()V");
        b.athrow();

        // update the correct address to jump over the catch block
        b.branchEnd(gotoEnd);

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

    /**
     * If the bean is part of a well known scope then this code caches instances in a thread local for the life of the
     * request, as a performance enhancement.
     */
    private void loadCachableBeanInstance(ClassFile file, MethodInformation methodInfo, CodeAttribute b) {
        //first we need to see if the scope is active
        b.invokestatic(RequestScopedBeanCache.class.getName(), "isActive", "()Z");
        //if it is not active we just get the bean directly

        final BranchEnd returnInstruction = b.ifeq();
        //get the bean from the cache
        b.aload(0);
        b.getfield(file.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
        b.invokevirtual(ThreadLocal.class.getName(), "get", "()Ljava/lang/Object;");
        b.dup();
        final BranchEnd createNewInstance = b.ifnull();
        //so we have a not-null bean instance in the cache
        b.checkcast(methodInfo.getDeclaringClass());
        final BranchEnd loadedFromCache = b.gotoInstruction();
        b.branchEnd(createNewInstance);
        //we need to get a bean instance and cache it
        //first clear the null off the top of the stack
        b.pop();
        loadBeanInstance(file, methodInfo, b);
        b.dup();
        b.aload(0);
        b.getfield(file.getName(), CACHE_FIELD, "Ljava/lang/ThreadLocal;");
        b.dupX1();
        b.swap();
        b.invokevirtual(ThreadLocal.class.getName(), "set", "(Ljava/lang/Object;)V");
        b.invokestatic(RequestScopedBeanCache.class.getName(), "addItem", "(Ljava/lang/ThreadLocal;)V");
        final BranchEnd endOfIfStatement = b.gotoInstruction();
        b.branchEnd(returnInstruction);
        loadBeanInstance(file, methodInfo, b);
        b.branchEnd(endOfIfStatement);
        b.branchEnd(loadedFromCache);
    }

    private void loadBeanInstance(ClassFile file, MethodInformation methodInfo, CodeAttribute b) {
        b.aload(0);
        b.getfield(file.getName(), "methodHandler", DescriptorUtils.classToStringRepresentation(MethodHandler.class));
        //pass null arguments to methodHandler.invoke
        b.aload(0);
        b.aconstNull();
        b.aconstNull();
        b.aconstNull();

        // now we have all our arguments on the stack
        // lets invoke the method
        b.invokeinterface(MethodHandler.class.getName(), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");

        b.checkcast(methodInfo.getDeclaringClass());
    }

    /**
     * Client proxies use the following hashCode:
     * <code>MyProxyName.class.hashCode()</code>
     */
    @Override
    protected void generateHashCodeMethod(ClassFile proxyClassType) {
        final ClassMethod method =proxyClassType.addMethod(AccessFlag.PUBLIC, "hashCode", "I");
        final CodeAttribute b = method.getCodeAttribute();
        // MyProxyName.class.hashCode()
        b.loadClass(proxyClassType.getName());
        // now we have the class object on top of the stack
        b.invokevirtual("java.lang.Object", "hashCode", "()I");
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
        ClassMethod method = proxyClassType.addMethod(AccessFlag.PUBLIC, "equals", "Z", "Ljava/lang/Object;");
        CodeAttribute b = method.getCodeAttribute();
        b.aload(1);
        b.instanceofInstruction(proxyClassType.getName());
        b.returnInstruction();
    }

    @Override
    protected String getProxyNameSuffix() {
        return CLIENT_PROXY_SUFFIX;
    }

}
