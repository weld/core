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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.interceptor.InvocationContext;

import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;

/**
 * The terminal {@link InvocationContext} in the interception chain. It is passed to the last interceptor in the chain and
 * calling {@link #proceed()} invokes
 * the underlying business method.
 *
 * @author Jozef Hartinger
 * @see NonTerminalAroundInvokeInvocationContext
 * @see AroundInvokeInvocationContext
 *
 */
class TerminalAroundInvokeInvocationContext extends AroundInvokeInvocationContext {

    public TerminalAroundInvokeInvocationContext(Object target, Method method, Method proceed, Object[] parameters,
            Map<String, Object> contextData,
            Set<Annotation> interceptorBindings, CombinedInterceptorAndDecoratorStackMethodHandler currentHandler) {
        super(target, method, proceed, parameters, (contextData == null) ? null : new HashMap<String, Object>(contextData),
                interceptorBindings, currentHandler);
    }

    public TerminalAroundInvokeInvocationContext(NonTerminalAroundInvokeInvocationContext ctx) {
        super(ctx.getTarget(), ctx.getMethod(), ctx.getProceed(), ctx.getParameters(), ctx.contextData,
                ctx.getInterceptorBindings(), ctx.currentHandler);
    }

    @Override
    public Object proceedInternal() throws Exception {
        return getProceed().invoke(getTarget(), getParameters());
    }

    @Override
    public String toString() {
        return "TerminalAroundInvokeInvocationContext [method=" + method + ']';
    }
}
