/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.interceptors.lifecycle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Bar
public class BarInterceptor {

    private static boolean postConstructCalled;
    private static boolean preDestroyCalled;

    @PostConstruct
    public void postConstruct(InvocationContext ctx) {
        assertTrue(FooInterceptor.isPostConstructCalled());
        assertFalse(BarInterceptor.isPostConstructCalled());
        assertFalse(BazInterceptor.isPostConstructCalled());
        assertFalse(Donkey.isPostConstructCalled());
        postConstructCalled = true;
        try {
            ctx.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void preDestroy(InvocationContext ctx) {
        assertTrue(FooInterceptor.isPreDestroyCalled());
        assertFalse(BarInterceptor.isPreDestroyCalled());
        assertFalse(BazInterceptor.isPreDestroyCalled());
        assertFalse(Donkey.isPreDestroyCalled());
        preDestroyCalled = true;
        try {
            ctx.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public static boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

    public static void reset() {
        postConstructCalled = false;
        preDestroyCalled = false;
    }
}
