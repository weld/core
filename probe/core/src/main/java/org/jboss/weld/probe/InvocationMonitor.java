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
package org.jboss.weld.probe;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * An invocation monitor interceptor.
 *
 * @author Martin Kouba
 */
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@Monitored
@Interceptor
@Dependent
public class InvocationMonitor implements Serializable {

    private static final long serialVersionUID = -5245789370968148511L;

    private static final ThreadLocal<Invocation.Builder> invocations = new ThreadLocal<Invocation.Builder>();

    private static final AtomicInteger invocationIdGenerator = new AtomicInteger(0);

    @Intercepted
    @Inject
    private Bean<?> interceptedBean;

    @Inject
    private BeanManagerImpl beanManager;

    private volatile Probe probe = null;

    @AroundInvoke
    public Object monitor(InvocationContext ctx) throws Exception {

        if(probe == null) {
            synchronized (this) {
                if(probe == null) {
                    probe = beanManager.getServices().get(Probe.class);
                }
            }
        }

        Invocation.Builder builder = invocations.get();
        boolean entryPoint = false;

        try {
            if (builder == null) {
                entryPoint = true;
                builder = Invocation.Builder.newBuilder(invocationIdGenerator.incrementAndGet());
                invocations.set(builder);
            } else {
                builder = builder.newChild();
                invocations.set(builder);
            }
            if (interceptedBean != null) {
                builder.setInterceptedBean(interceptedBean);
            } else {
                builder.setDeclaringClassName(ctx.getMethod().getDeclaringClass().getName());
            }
            builder.guessType(ctx);
            builder.setStart(System.currentTimeMillis());
            builder.setMethodName(ctx.getMethod().getName());
            long start = System.nanoTime();

            Object result = ctx.proceed();

            builder.setDuration(System.nanoTime() - start);
            if (entryPoint) {
                probe.addInvocation(builder.build());
            } else {
                invocations.set(builder.getParent());
            }
            return result;

        } finally {
            if (entryPoint) {
                invocations.remove();
            }
        }
    }

}
