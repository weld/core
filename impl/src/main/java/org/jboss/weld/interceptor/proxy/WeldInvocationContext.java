/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.interceptor.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.interceptor.InvocationContext;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.util.ForwardingInvocationContext;

/**
 * Weld's {@link InvocationContext} implementation. This is a forwarding implementation that delegates most method calls to an underlying
 * {@link InvocationContext}. This allows multiple interceptor chains to be combined.
 *
 * A call to {@link #proceed()} invokes the chain of intercepors in the given order. Once the chain finishes, the {@link #proceed()} method of the delegate is
 * invoked which results in the target method being invoked in case of {@link SimpleInvocationContext}. Otherwise, the delegate chain is run.
 *
 * @author Jozef Hartinger
 *
 */
public class WeldInvocationContext extends ForwardingInvocationContext {

    private abstract class RunInInterceptionContext {

        protected abstract Object doWork() throws Exception;

        public Object run() throws Exception {
            if (currentInterceptionContext == null) {
                return doWork();
            }
            /*
             * Make sure that the right interception context is on top of the stack before invoking the component or next interceptor. See WELD-1538 for details
             */
            final boolean pushed = InterceptionDecorationContext.startIfNotOnTop(currentInterceptionContext);
            try {
                return doWork();
            } finally {
                if (pushed) {
                    InterceptionDecorationContext.endInterceptorContext();
                }
            }
        }
    }

    private int position;
    private final List<InterceptorMethodInvocation> chain;
    private final CombinedInterceptorAndDecoratorStackMethodHandler currentInterceptionContext;
    private final InvocationContext delegate;

    public WeldInvocationContext(Constructor<?> constructor, Object[] parameters, Map<String, Object> contextData, List<InterceptorMethodInvocation> chain) {
        this(new SimpleInvocationContext(constructor, parameters, contextData), chain);
    }

    public WeldInvocationContext(Object target, Method targetMethod, Method proceed, Object[] parameters, List<InterceptorMethodInvocation> chain) {
        this(new SimpleInvocationContext(target, targetMethod, proceed, parameters), chain);
    }

    public WeldInvocationContext(InvocationContext delegate, List<InterceptorMethodInvocation> chain) {
        this.delegate = delegate;
        this.chain = chain;
        this.currentInterceptionContext = InterceptionDecorationContext.peekIfNotEmpty();
    }

    @Override
    protected InvocationContext delegate() {
        return delegate;
    }

    public boolean hasNextInterceptor() {
        return position < chain.size();
    }

    protected Object invokeNext() throws Exception {
        return new RunInInterceptionContext() {
            @Override
            protected Object doWork() throws Exception {
                int oldCurrentPosition = position;
                try {
                    InterceptorMethodInvocation nextInterceptorMethodInvocation = chain.get(position++);
                    InterceptorLogger.LOG.invokingNextInterceptorInChain(nextInterceptorMethodInvocation);
                    if (nextInterceptorMethodInvocation.expectsInvocationContext()) {
                        return nextInterceptorMethodInvocation.invoke(WeldInvocationContext.this);
                    } else {
                        nextInterceptorMethodInvocation.invoke(null);
                        while (hasNextInterceptor()) {
                            nextInterceptorMethodInvocation = chain.get(position++);
                            nextInterceptorMethodInvocation.invoke(null);
                        }
                        return null;
                    }
                } finally {
                    position = oldCurrentPosition;
                }
            }
        }.run();
    }

    private Object finish() throws Exception {
        return new RunInInterceptionContext() {
            @Override
            protected Object doWork() throws Exception {
                return interceptorChainCompleted();
            }
        }.run();
    }

    protected Object interceptorChainCompleted() throws Exception {
        return delegate().proceed();
    }

    @Override
    public Object proceed() throws Exception {
        try {
            if (hasNextInterceptor()) {
                return invokeNext();
            } else {
                return finish();
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new RuntimeException(cause);
        }
    }
}
