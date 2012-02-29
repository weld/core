/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

package org.jboss.interceptor.proxy;

import java.lang.reflect.Constructor;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.jboss.interceptor.spi.context.InvocationContextFactory;
import org.jboss.interceptor.spi.instance.InterceptorInstantiator;
import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.model.InterceptionModel;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.interceptor.util.ReflectionFactoryUtils;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class InterceptorProxyCreatorImpl implements InterceptorProxyCreator {

    private InvocationContextFactory invocationContextFactory;
    private InterceptionModel<ClassMetadata<?>, ?> interceptionModel;

    private InterceptorInstantiator<?, ?> interceptorInstantiator;

    public InterceptorProxyCreatorImpl(InterceptorInstantiator<?, ?> interceptorInstantiator, InvocationContextFactory invocationContextFactory, InterceptionModel<ClassMetadata<?>, ?> interceptionModel) {
        this.interceptorInstantiator = interceptorInstantiator;
        this.invocationContextFactory = invocationContextFactory;
        this.interceptionModel = interceptionModel;
    }

    public <T> T createAdvisedSubclassInstance(ClassMetadata<T> proxifiedClass, Class<?>[] constructorParameterTypes, Object[] constructorArguments) {
        try {
            Class<T> clazz = InterceptionUtils.createProxyClass(((Class<T>) proxifiedClass.getJavaClass()), true);
            Constructor<T> constructor = clazz.getConstructor(constructorParameterTypes);
            return constructor.newInstance(constructorArguments);
        } catch (Exception e) {
            throw new InterceptorException(e);
        }
    }

    public <T> T createProxyInstance(Class<T> proxyClass, MethodHandler interceptorMethodHandler) {
        Constructor<T> constructor = null;
        try {
            constructor = getNoArgConstructor(proxyClass);
            if (constructor == null) {
                constructor = ReflectionFactoryUtils.getReflectionFactoryConstructor(proxyClass);
            }
        } catch (Exception e) {
            throw new InterceptorException(e);
        }
        if (constructor == null)
            throw new InterceptorException("Cannot found a constructor for the proxy class: " + proxyClass + ". " +
                    "No no-arg constructor is available, and sun.reflect.ReflectionFactory is not accessible");
        try {
            T proxyObject = constructor.newInstance();
            if (interceptorMethodHandler != null) {
                ((ProxyObject) proxyObject).setHandler(interceptorMethodHandler);
            }
            return proxyObject;
        } catch (Exception e) {
            throw new InterceptorException(e);
        }
    }

    public <T> MethodHandler createMethodHandler(Object target, ClassMetadata<T> proxyClass) {
        return new InterceptorMethodHandler(target, proxyClass, interceptionModel, interceptorInstantiator, invocationContextFactory);
    }

    public <T> MethodHandler createSubclassingMethodHandler(Object targetInstance, ClassMetadata<T> proxyClass) {
        return new InterceptorMethodHandler(targetInstance, proxyClass, interceptionModel, interceptorInstantiator, invocationContextFactory);
    }

    private <T> Constructor<T> getNoArgConstructor(Class<T> clazz) {
        Constructor<T> constructor;
        try {
            constructor = clazz.getConstructor(new Class[]{});
        } catch (NoSuchMethodException e) {
            return null;
        }
        return constructor;
    }

}


