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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.interceptor.InvocationContext;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.interceptor.WeldInvocationContext;

/**
 * The non-terminal {@link InvocationContext} in the interception chain. This implementation is used for the first n-1
 * interceptors of a interception chain of
 * n. When {@link #proceed()} is called this class invokes the next interceptor in the chain either passing in a next
 * {@link NonTerminalAroundInvokeInvocationContext} or {@link TerminalAroundInvokeInvocationContext} if the interceptor is the
 * last one in the chain.
 *
 * @author Jozef Hartinger
 * @see TerminalAroundInvokeInvocationContext
 * @see AroundInvokeInvocationContext
 *
 */
class NonTerminalAroundInvokeInvocationContext extends AroundInvokeInvocationContext {

    private final int position;
    private final List<InterceptorMethodInvocation> chain;

    public NonTerminalAroundInvokeInvocationContext(Object target, Method method, Method proceed, Object[] parameters,
            Set<Annotation> interceptorBindings,
            List<InterceptorMethodInvocation> chain, CombinedInterceptorAndDecoratorStackMethodHandler currentHandler) {
        this(target, method, proceed, parameters, newContextData(interceptorBindings), interceptorBindings, 0, chain,
                currentHandler);
    }

    public NonTerminalAroundInvokeInvocationContext(NonTerminalAroundInvokeInvocationContext ctx) {
        this(ctx.getTarget(), ctx.getMethod(), ctx.getProceed(), ctx.getParameters(), ctx.contextData,
                ctx.getInterceptorBindings(), ctx.position + 1,
                ctx.chain, ctx.currentHandler);
    }

    private NonTerminalAroundInvokeInvocationContext(Object target, Method method, Method proceed, Object[] parameters,
            Map<String, Object> contextData,
            Set<Annotation> interceptorBindings, int position, List<InterceptorMethodInvocation> chain,
            CombinedInterceptorAndDecoratorStackMethodHandler currentHandler) {
        super(target, method, proceed, parameters, contextData, interceptorBindings, currentHandler);
        this.position = position;
        this.chain = chain;
    }

    @Override
    public Object proceedInternal() throws Exception {
        WeldInvocationContext ctx = createNextContext();
        return chain.get(position + 1).invoke(ctx);
    }

    private WeldInvocationContext createNextContext() {
        if (position + 2 == chain.size()) {
            return new TerminalAroundInvokeInvocationContext(this);
        } else {
            return new NonTerminalAroundInvokeInvocationContext(this);
        }
    }

    @Override
    public String toString() {
        return "NonTerminalAroundInvokeInvocationContext [method=" + method + ", interceptor=" + chain.get(position) + ']';
    }
}
