/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026, Red Hat, Inc. and/or its affiliates, and individual
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * Verifies that {@link MethodInvoker} behaves exactly like {@link Method#invoke(Object, Object...)}: same results, same
 * exception types (and causes) in all the cases, including those where the method handle cannot be used.
 */
public class MethodInvokerTest {

    public static class Target {

        public String echo(String value) {
            return value;
        }

        public int add(int a, int b) {
            return a + b;
        }

        public long widen(long value) {
            return value;
        }

        public Object object(Object value) {
            return value;
        }

        public String noArgs() {
            return "noArgs";
        }

        public void nothing() {
        }

        public Object fail(Throwable throwable) throws Throwable {
            throw throwable;
        }

        private String secret(String value) {
            return "secret " + value;
        }

        private String restricted(String value) {
            return value;
        }

        private String restrictedNoArgs() {
            return "restricted";
        }

        public static String staticMethod(String value) {
            return "static " + value;
        }
    }

    public static class SubTarget extends Target {

        @Override
        public String echo(String value) {
            return "sub " + value;
        }
    }

    private interface Invocation {
        Object invoke() throws Exception;
    }

    private static String outcome(Invocation invocation) {
        try {
            return "result: " + invocation.invoke();
        } catch (InvocationTargetException e) {
            return "InvocationTargetException caused by " + e.getCause();
        } catch (Exception e) {
            return e.getClass().getName();
        }
    }

    private static void assertSameOutcome(Method method, Object target, Object[] args) {
        MethodInvoker invoker = MethodInvoker.of(method);
        String expected = outcome(() -> method.invoke(target, args));
        assertEquals(expected, outcome(() -> invoker.invoke(method, target, args)));
        if (args != null && args.length == 1) {
            assertEquals(expected, outcome(() -> invoker.invoke(method, target, args[0])));
        }
        if (args != null && args.length == 0) {
            assertEquals(expected, outcome(() -> invoker.invoke(method, target)));
        }
    }

    private static Method method(String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return Target.class.getDeclaredMethod(name, parameterTypes);
    }

    @Test
    public void testResults() throws Exception {
        Target target = new Target();
        assertSameOutcome(method("echo", String.class), target, new Object[] { "foo" });
        assertSameOutcome(method("echo", String.class), target, new Object[] { null });
        assertSameOutcome(method("echo", String.class), new SubTarget(), new Object[] { "foo" });
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { 1, 2 });
        assertSameOutcome(method("widen", long.class), target, new Object[] { 1L });
        assertSameOutcome(method("object", Object.class), target, new Object[] { 1 });
        assertSameOutcome(method("noArgs"), target, new Object[0]);
        assertSameOutcome(method("nothing"), target, new Object[0]);
        assertSameOutcome(method("nothing"), target, null);
        assertSameOutcome(method("staticMethod", String.class), target, new Object[] { "foo" });
        assertSameOutcome(method("staticMethod", String.class), null, new Object[] { "foo" });
        Method secret = method("secret", String.class);
        secret.setAccessible(true);
        assertSameOutcome(secret, target, new Object[] { "foo" });
    }

    @Test
    public void testIllegalArguments() throws Exception {
        Target target = new Target();
        // widening primitive conversion (reflection only)
        assertSameOutcome(method("widen", long.class), target, new Object[] { 1 });
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { (short) 1, (byte) 2 });
        // wrong types
        assertSameOutcome(method("echo", String.class), target, new Object[] { 1 });
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { 1L, 2 });
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { "1", 2 });
        // null for a primitive parameter
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { null, 2 });
        // wrong number of arguments
        assertSameOutcome(method("add", int.class, int.class), target, new Object[] { 1 });
        assertSameOutcome(method("echo", String.class), target, new Object[0]);
        assertSameOutcome(method("echo", String.class), target, null);
        assertSameOutcome(method("noArgs"), target, new Object[] { 1 });
        // wrong or null receiver
        assertSameOutcome(method("echo", String.class), "not a target", new Object[] { "foo" });
        assertSameOutcome(method("echo", String.class), null, new Object[] { "foo" });
        assertSameOutcome(method("noArgs"), null, new Object[0]);
    }

    @Test
    public void testExceptionsThrownByTheMethod() throws Exception {
        Target target = new Target();
        Method fail = method("fail", Throwable.class);
        assertSameOutcome(fail, target, new Object[] { new IOException("checked") });
        assertSameOutcome(fail, target, new Object[] { new IllegalStateException("unchecked") });
        assertSameOutcome(fail, target, new Object[] { new IllegalArgumentException("thrown by the method") });
        assertSameOutcome(fail, target, new Object[] { new NullPointerException("thrown by the method") });
        assertSameOutcome(fail, target, new Object[] { new ClassCastException("thrown by the method") });
        assertSameOutcome(fail, target, new Object[] { new AssertionError("error") });
        assertSameOutcome(fail, target,
                new Object[] { new InvocationTargetException(new IOException("wrapped by the method")) });
        assertSameOutcome(fail, target, new Object[] { new Throwable("plain throwable") });
    }

    @Test
    public void testExceptionIdentity() throws Exception {
        IOException exception = new IOException();
        Method fail = method("fail", Throwable.class);
        try {
            MethodInvoker.of(fail).invoke(fail, new Target(), (Object) exception);
        } catch (InvocationTargetException e) {
            assertSame(exception, e.getCause());
            return;
        }
        throw new AssertionError("InvocationTargetException expected");
    }

    @Test
    public void testCachedHandleDoesNotTransferAccessOverride() throws Exception {
        Target target = new Target();
        Method allowed = method("restricted", String.class);
        allowed.setAccessible(true);
        MethodInvoker invoker = MethodInvoker.of(allowed);
        assertEquals("value", invoker.invoke(allowed, target, (Object) "value"));

        Method restricted = method("restricted", String.class);
        assertSame(invoker, MethodInvoker.of(restricted));
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(restricted, target, (Object) "value"));
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(restricted, target, new Object[] { "value" }));

        allowed.setAccessible(false);
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(allowed, target, (Object) "value"));
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(allowed, target, new Object[] { "value" }));
        allowed.setAccessible(true);
        assertEquals("value", invoker.invoke(allowed, target, (Object) "value"));
    }

    @Test
    public void testNoArgumentHandleRespectsAccessOverride() throws Exception {
        Target target = new Target();
        Method allowed = method("restrictedNoArgs");
        allowed.setAccessible(true);
        MethodInvoker invoker = MethodInvoker.of(allowed);
        assertEquals("restricted", invoker.invoke(allowed, target));
        Method restricted = method("restrictedNoArgs");
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(restricted, target));
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(restricted, target, new Object[0]));
        allowed.setAccessible(false);
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(allowed, target));
        assertThrows(IllegalAccessException.class, () -> invoker.invoke(allowed, target, new Object[0]));
    }

    @Test
    public void testCache() throws Exception {
        Method echo = method("echo", String.class);
        assertSame(MethodInvoker.of(echo), MethodInvoker.of(method("echo", String.class)));
        assertNull(MethodInvoker.of(null));
    }
}
