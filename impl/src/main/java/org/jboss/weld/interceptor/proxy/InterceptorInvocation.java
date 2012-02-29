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

package org.jboss.weld.interceptor.proxy;

import java.util.ArrayList;
import java.util.Collection;

import javax.interceptor.InvocationContext;

import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
 * @author Marius Bogoevici
 */
public class InterceptorInvocation<T> {
    private final T instance;

    private final InterceptorMetadata<?> interceptorMetadata;

    private final InterceptionType interceptionType;

    public InterceptorInvocation(T instance, InterceptorMetadata<?> interceptorMetadata, InterceptionType interceptionType) {
        this.instance = instance;
        this.interceptorMetadata = interceptorMetadata;
        this.interceptionType = interceptionType;
    }

    public Collection<InterceptorMethodInvocation> getInterceptorMethodInvocations() {
        Collection<InterceptorMethodInvocation> interceptorMethodInvocations = new ArrayList<InterceptorMethodInvocation>();
        for (MethodMetadata method : interceptorMetadata.getInterceptorMethods(interceptionType)) {
            interceptorMethodInvocations.add(new InterceptorMethodInvocation(instance, method));
        }
        return interceptorMethodInvocations;
    }

    public class InterceptorMethodInvocation {
        final T instance;

        final MethodMetadata method;

        InterceptorMethodInvocation(T instance, MethodMetadata method) {
            this.instance = instance;
            this.method = method;
        }

        Object invoke(InvocationContext invocationContext) throws Exception {
            if (invocationContext != null)
                return method.getJavaMethod().invoke(instance, invocationContext);
            else
                return method.getJavaMethod().invoke(instance);
        }

        public MethodMetadata getMethod() {
            return method;
        }
    }
}
