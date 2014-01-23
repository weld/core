/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.interceptor.chain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.interceptor.InvocationContext;

import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorMethodInvocation;
import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.context.InterceptionChain;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.logging.InterceptorLogger;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 *
 */
public abstract class AbstractInterceptionChain implements InterceptionChain {

    private int currentPosition;

    private final List<InterceptorMethodInvocation> interceptorMethodInvocations;

    private static Collection<InterceptorInvocation> buildInterceptorMethodInvocations(Object instance, Method method, Object[] args, InterceptionType interceptionType, InterceptionContext ctx) {
        List<? extends InterceptorClassMetadata<?>> interceptorList = ctx.getInterceptionModel().getInterceptors(interceptionType, method);
        Collection<InterceptorInvocation> interceptorInvocations = new ArrayList<InterceptorInvocation>(interceptorList.size());
        for (InterceptorClassMetadata<?> interceptorMetadata : interceptorList) {
            interceptorInvocations.add(interceptorMetadata.getInterceptorInvocation(ctx.getInterceptorInstance(interceptorMetadata), interceptionType));
        }
        TargetClassInterceptorMetadata targetClassInterceptorMetadata = ctx.getInterceptionModel().getTargetClassInterceptorMetadata();
        if (targetClassInterceptorMetadata != null && targetClassInterceptorMetadata.isEligible(interceptionType)) {
            interceptorInvocations.add(targetClassInterceptorMetadata.getInterceptorInvocation(instance, interceptionType));
        }
        return interceptorInvocations;
    }

    protected AbstractInterceptionChain(Object instance, Method method, Object[] args, InterceptionType interceptionType, InterceptionContext ctx) {
        this(buildInterceptorMethodInvocations(instance, method, args, interceptionType, ctx));
    }

    protected AbstractInterceptionChain(Collection<InterceptorInvocation> interceptorInvocations) {
        this.currentPosition = 0;
        interceptorMethodInvocations = new ArrayList<InterceptorMethodInvocation>(interceptorInvocations.size());
        for (InterceptorInvocation interceptorInvocation : interceptorInvocations) {
            interceptorMethodInvocations.addAll(interceptorInvocation.getInterceptorMethodInvocations());
        }
    }

    protected AbstractInterceptionChain(InterceptorInvocation interceptorInvocation) {
        this.currentPosition = 0;
        interceptorMethodInvocations = new ArrayList<InterceptorMethodInvocation>(interceptorInvocation.getInterceptorMethodInvocations());
    }

    @Override
    public Object invokeNextInterceptor(InvocationContext invocationContext) throws Exception {

        try {
            if (hasNextInterceptor()) {
                return invokeNext(invocationContext);
            } else {
                return interceptorChainCompleted(invocationContext);
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

    protected Object invokeNext(InvocationContext invocationContext) throws Exception {
        int oldCurrentPosition = currentPosition;
        try {
            InterceptorMethodInvocation nextInterceptorMethodInvocation = interceptorMethodInvocations.get(currentPosition++);
            InterceptorLogger.LOG.invokingNextInterceptorInChain(nextInterceptorMethodInvocation.toString());
            if (nextInterceptorMethodInvocation.expectsInvocationContext()) {
                return nextInterceptorMethodInvocation.invoke(invocationContext);
            } else {
                nextInterceptorMethodInvocation.invoke(null);
                while (hasNextInterceptor()) {
                    nextInterceptorMethodInvocation = interceptorMethodInvocations.get(currentPosition++);
                    nextInterceptorMethodInvocation.invoke(null);
                }
                return null;
            }
        } finally {
            currentPosition = oldCurrentPosition;
        }
    }

    protected abstract Object interceptorChainCompleted(InvocationContext invocationContext) throws Exception;

    @Override
    public boolean hasNextInterceptor() {
        return currentPosition < interceptorMethodInvocations.size();
    }
}
