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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;

@Foo
@Bar
@Baz
@Dependent
public class Donkey {

    private static boolean postConstructCalled;
    private static boolean preDestroyCalled;

    @PostConstruct
    public void postConstruct() {
        assertTrue(FooInterceptor.isPostConstructCalled());
        assertTrue(BarInterceptor.isPostConstructCalled());
        assertTrue(BazInterceptor.isPostConstructCalled());
        assertFalse(Donkey.isPostConstructCalled());
        postConstructCalled = true;
    }

    @PreDestroy
    public void preDestroy() {
        assertTrue(FooInterceptor.isPreDestroyCalled());
        assertTrue(BarInterceptor.isPreDestroyCalled());
        assertTrue(BazInterceptor.isPreDestroyCalled());
        assertFalse(Donkey.isPreDestroyCalled());
        preDestroyCalled = true;
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
