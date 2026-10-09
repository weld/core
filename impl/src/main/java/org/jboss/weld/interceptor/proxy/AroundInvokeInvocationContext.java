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

import static org.jboss.weld.util.reflection.Reflections.unwrapInvocationTargetException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.interceptor.InvocationContext;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext;
import org.jboss.weld.bean.proxy.InterceptionDecorationContext.Stack;

/**
 * For AroundInvoke interception type we use a special type of InvocationContext. Unlike the default one, this one is does not
 * track
 * the position of the interception in a mutable field but is instead immutable.
 *
 * This allows:
 * <ul>
 * <li>interception to be repeatable - e.g. an interceptor may call InvocationContext.proceed() multiple times</li>
 * <li>interception to continue in a different thread - implementing {@link jakarta.ejb.Asynchronous} with interceptors</li>
 * </ul>
 *
 * This however also requires that for each interceptor in the chain we create a new instance of
 * {@link AroundInvokeInvocationContext}.
 * Context data and method parameters are mutable. We do not guard them anyhow - the expectation for them is to be effectively
 * immutable
 * by only being modified before or after dispatch. We also assume that the dispatch safely propagates the state of
 * {@link InvocationContext}
 * from one thread to the other.
 *
 * @author Jozef Hartinger
 * @see TerminalAroundInvokeInvocationContext
 * @see NonTerminalAroundInvokeInvocationContext
 *
 */
abstract class AroundInvokeInvocationContext extends AbstractInvocationContext {

    public static AroundInvokeInvocationContext create(Object instance, Method method, Method proceed, Object[] args,
            List<InterceptorMethodInvocation> chain,
            Set<Annotation> interceptorBindings, Stack stack) {
        return create(instance, method, proceed, MethodInvoker.of(proceed), args, chain, interceptorBindings, stack);
    }

    static AroundInvokeInvocationContext create(Object instance, Method method, Method proceed, MethodInvoker proceedInvoker,
            Object[] args, List<InterceptorMethodInvocation> chain, Set<Annotation> interceptorBindings, Stack stack) {
        CombinedInterceptorAndDecoratorStackMethodHandler currentHandler = (stack == null) ? null : stack.peek();
        if (chain.size() == 1) {
            return new TerminalAroundInvokeInvocationContext(instance, method, proceed, proceedInvoker, args, null,
                    interceptorBindings, currentHandler, stack);
        } else {
            return new NonTerminalAroundInvokeInvocationContext(instance, method, proceed, proceedInvoker, args,
                    interceptorBindings, chain, currentHandler, stack);
        }
    }

    final CombinedInterceptorAndDecoratorStackMethodHandler currentHandler;
    // invokes the proceed method
    final MethodInvoker proceedInvoker;
    // the stack of the thread that created this context (may be null), avoids the thread-local lookup upon proceed()
    final Stack creatorStack;

    AroundInvokeInvocationContext(Object target, Method method, Method proceed, MethodInvoker proceedInvoker,
            Object[] parameters, Map<String, Object> contextData,
            Set<Annotation> interceptorBindings, CombinedInterceptorAndDecoratorStackMethodHandler currentHandler,
            Stack stack) {
        super(target, method, proceed, parameters, contextData, interceptorBindings);
        this.proceedInvoker = proceedInvoker;
        this.currentHandler = currentHandler;
        this.creatorStack = stack;
    }

    @Override
    public Object proceed() throws Exception {
        final Stack stack = InterceptionDecorationContext.startIfNotOnTop(creatorStack, currentHandler);
        try {
            return proceedInternal();
        } catch (InvocationTargetException e) {
            throw unwrapInvocationTargetException(e);
        } finally {
            if (stack != null) {
                stack.end();
            }
        }
    }

    abstract Object proceedInternal() throws Exception;
}
