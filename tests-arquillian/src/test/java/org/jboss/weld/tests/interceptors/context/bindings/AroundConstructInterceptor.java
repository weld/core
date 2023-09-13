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
package org.jboss.weld.tests.interceptors.context.bindings;

import java.lang.annotation.Annotation;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.interceptor.WeldInvocationContext;

@Priority(value = Interceptor.Priority.APPLICATION)
@Interceptor
@FooBinding(secret = "nonbinding")
public class AroundConstructInterceptor {

    private static Set<Annotation> contextDataBindings;

    private static Set<Annotation> contextBindings;

    @SuppressWarnings("unchecked")
    @AroundConstruct
    void intercept(InvocationContext ctx) throws Exception {
        contextDataBindings = (Set<Annotation>) ctx.getContextData().get(WeldInvocationContext.INTERCEPTOR_BINDINGS_KEY);
        if (ctx instanceof WeldInvocationContext) {
            contextBindings = ((WeldInvocationContext) ctx).getInterceptorBindings();
        }
        ctx.proceed();
    }

    static void reset() {
        contextDataBindings = null;
        contextBindings = null;
    }

    static Set<Annotation> getContextDataBindings() {
        return contextDataBindings;
    }

    static Set<Annotation> getContextBindings() {
        return contextBindings;
    }
}