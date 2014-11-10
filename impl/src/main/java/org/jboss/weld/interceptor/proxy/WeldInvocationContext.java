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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.InvocationContext;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;
import org.jboss.weld.experimental.ExperimentalInvocationContext;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.util.ForwardingInvocationContext;
import org.jboss.weld.util.Preconditions;

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
public class WeldInvocationContext extends ForwardingInvocationContext implements ExperimentalInvocationContext {

    private int position;
    private final List<InterceptorMethodInvocation> chain;
    private final CombinedInterceptorAndDecoratorStackMethodHandler interceptionContext;
    private final InvocationContext delegate;
    private final Set<Annotation> interceptorBindings;
    private final Stack stack;

    public WeldInvocationContext(Constructor<?> constructor, Object[] parameters, Map<String, Object> contextData, List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings) {
        this(new SimpleInvocationContext(constructor, parameters, contextData), chain, interceptorBindings, null);
    }

    public WeldInvocationContext(Object target, Method targetMethod, Method proceed, Object[] parameters, List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings, Stack stack) {
        this(new SimpleInvocationContext(target, targetMethod, proceed, parameters), chain, interceptorBindings, stack);
    }

    public WeldInvocationContext(InvocationContext delegate, List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings, Stack stack) {
        this.delegate = delegate;
        this.chain = chain;
        this.stack = stack;
        this.interceptionContext = (stack == null) ? null : stack.peek();
        if (interceptorBindings == null) {
            this.interceptorBindings = Collections.<Annotation>emptySet();
        } else {
            this.interceptorBindings = interceptorBindings;
        }
        getContextData().put(InterceptorMethodHandler.INTERCEPTOR_BINDINGS_KEY, interceptorBindings);
    }

    @Override
    protected InvocationContext delegate() {
        return delegate;
    }

    public boolean hasNextInterceptor() {
        return position < chain.size();
    }

    protected Object invokeNext() throws Exception {
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

    protected Object interceptorChainCompleted() throws Exception {
        return delegate().proceed();
    }

    @Override
    public Object proceed() throws Exception {
        boolean pushed = false;
        /*
         * No need to push the context for the first interceptor as the current context
         * was set by CombinedInterceptorAndDecoratorStackMethodHandler
         */
        if (stack != null && position != 0) {
            pushed = stack.startIfNotOnTop(interceptionContext);
        }

        try {
            if (hasNextInterceptor()) {
                return invokeNext();
            } else {
                return interceptorChainCompleted();
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
        } finally {
            if (pushed) {
                stack.end();
            }
        }
    }

    @Override
    @java.lang.SuppressWarnings("unchecked")
    public <T extends Annotation> Set<T> getInterceptorBindingsByType(Class<T> annotationType) {
        Preconditions.checkArgumentNotNull(annotationType, "annotationType");
        Set<T> result = new HashSet<>();
        for (Annotation interceptorBinding : interceptorBindings) {
            if (interceptorBinding.annotationType().equals(annotationType)) {
                result.add((T) interceptorBinding);
            }
        }
        return result;
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return interceptorBindings;
    }
}
