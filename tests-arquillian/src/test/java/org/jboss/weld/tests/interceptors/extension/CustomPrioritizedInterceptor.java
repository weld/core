/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.extension;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.tests.interceptors.extension.FooInterceptorBinding.FooInterceptorBindingLiteral;

public class CustomPrioritizedInterceptor extends AbstractInterceptor<CustomPrioritizedInterceptor>
        implements PassivationCapable, Prioritized {

    private final int priority;

    public CustomPrioritizedInterceptor() {
        this(1000);
    }

    public CustomPrioritizedInterceptor(int priority) {
        this.priority = priority;
    }

    public Set<Annotation> getInterceptorBindings() {
        return Collections.<Annotation> singleton(FooInterceptorBindingLiteral.INSTANCE);
    }

    public boolean intercepts(InterceptionType type) {
        return InterceptionType.AROUND_INVOKE.equals(type);
    }

    public Object intercept(InterceptionType type, CustomPrioritizedInterceptor instance, InvocationContext ctx)
            throws Exception {
        ActionSequence.addAction(CustomPrioritizedInterceptor.class.getName());
        return ctx.proceed();
    }

    public Class<?> getBeanClass() {
        return CustomPrioritizedInterceptor.class;
    }

    public String getId() {
        return toString();
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
