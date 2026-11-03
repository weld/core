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

package org.jboss.weld.interceptor.reader;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.interceptor.InvocationContext;

import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorMethodInvocation;
import org.jboss.weld.interceptor.proxy.MethodInvoker;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.util.collections.ImmutableList;

/**
 * @author Marius Bogoevici
 */
class SimpleInterceptorInvocation implements InterceptorInvocation {

    private final List<InterceptorMethodInvocation> interceptorMethodInvocations;
    private final Object instance;
    private final boolean targetClass;
    private final InterceptionType interceptionType;

    public SimpleInterceptorInvocation(Object instance, InterceptionType interceptionType, List<Method> interceptorMethods,
            boolean targetClass) {
        this(instance, interceptionType, interceptorMethods, null, targetClass);
    }

    /**
     * @param interceptorMethodInvokers the invokers of the given interceptor methods (same order), may be null
     */
    SimpleInterceptorInvocation(Object instance, InterceptionType interceptionType, List<Method> interceptorMethods,
            MethodInvoker[] interceptorMethodInvokers, boolean targetClass) {
        this.instance = instance;
        this.interceptionType = interceptionType;
        this.targetClass = targetClass;

        if (interceptorMethods.size() == 1) {
            // Very often there will be only one interceptor method
            interceptorMethodInvocations = ImmutableList
                    .<InterceptorMethodInvocation> of(new SimpleMethodInvocation(interceptorMethods.get(0),
                            invoker(interceptorMethodInvokers, 0, interceptorMethods)));
        } else {
            ImmutableList.Builder<InterceptorMethodInvocation> builder = ImmutableList.builder();
            for (int i = 0; i < interceptorMethods.size(); i++) {
                builder.add(new SimpleMethodInvocation(interceptorMethods.get(i),
                        invoker(interceptorMethodInvokers, i, interceptorMethods)));
            }
            interceptorMethodInvocations = builder.build();
        }
    }

    private static MethodInvoker invoker(MethodInvoker[] invokers, int i, List<Method> interceptorMethods) {
        return invokers != null ? invokers[i] : MethodInvoker.of(interceptorMethods.get(i));
    }

    @Override
    public List<InterceptorMethodInvocation> getInterceptorMethodInvocations() {
        return interceptorMethodInvocations;
    }

    class SimpleMethodInvocation implements InterceptorMethodInvocation {

        private final Method method;
        private final MethodInvoker invoker;

        SimpleMethodInvocation(Method method, MethodInvoker invoker) {
            this.method = method;
            this.invoker = invoker;
        }

        @Override
        public Object invoke(InvocationContext invocationContext) throws Exception {
            // same semantics as method.invoke(instance, invocationContext) / method.invoke(instance)
            if (invocationContext != null) {
                return invoker.invoke(method, instance, invocationContext);
            } else {
                return invoker.invoke(method, instance);
            }
        }

        @Override
        public boolean expectsInvocationContext() {
            return !targetClass || !interceptionType.isLifecycleCallback();
        }

        @Override
        public String toString() {
            return "SimpleMethodInvocation [method=" + method + ']';
        }
    }
}
