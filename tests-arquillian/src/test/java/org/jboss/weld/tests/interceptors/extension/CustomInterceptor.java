/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.tests.interceptors.extension.FooInterceptorBinding.FooInterceptorBindingLiteral;

/**
 * Extension-provided implementation of {@link Interceptor}. Delegates to FooInterceptor (which in real world could represent a
 * legacy-style interceptor - e.g. Seam 2 interceptor)
 *
 * @author <a href="http://community.jboss.org/people/jharting">Jozef Hartinger</a>
 */
public class CustomInterceptor extends AbstractInterceptor<FooInterceptor> implements PassivationCapable {

    private static boolean invoked = false;

    public Set<Annotation> getInterceptorBindings() {
        return Collections.<Annotation> singleton(FooInterceptorBindingLiteral.INSTANCE);
    }

    public boolean intercepts(InterceptionType type) {
        return InterceptionType.AROUND_INVOKE.equals(type);
    }

    public Object intercept(InterceptionType type, FooInterceptor instance, InvocationContext ctx) {
        invoked = true;
        try {
            return instance.intercept(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getBeanClass() {
        return FooInterceptor.class;
    }

    public static boolean isInvoked() {
        return invoked;
    }

    public static void reset() {
        invoked = false;
    }

    public String getId() {
        return toString();
    }
}
