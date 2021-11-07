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
package org.jboss.weld.tests.unit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.jboss.weld.util.reflection.DeclaredMemberIndexer;
import org.junit.Test;

public class DeclaredMemberIndexerTest {

    @Test
    public void testFields() {
        verifyFields(DeclaredMemberIndexer.getDeclaredFields(TestContainer.class), "bar", "baz", "foo", "qux");
        try {
            DeclaredMemberIndexer.getFieldForIndex(1000, TestContainer.class);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testConstructors() {
        List<Constructor<?>> constructors = DeclaredMemberIndexer.getDeclaredConstructors(TestContainer.class);
        assertEquals(5, constructors.size());
        verifyConstructor(constructors, 0);
        verifyConstructor(constructors, 1, String.class);
        verifyConstructor(constructors, 2, String.class, Integer.class);
        verifyConstructor(constructors, 3, String.class, String.class);
        verifyConstructor(constructors, 4, Integer.class, String.class, BigDecimal.class);
        try {
            DeclaredMemberIndexer.getConstructorForIndex(1000, TestContainer.class);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testMethods() {
        List<Method> methods = DeclaredMemberIndexer.getDeclaredMethods(TestContainer.class);
        assertEquals(5, methods.size());
        verifyMethod(methods, 0, "bar", Integer.class, String.class);
        verifyMethod(methods, 1, "bar", String.class, Integer.class);
        verifyMethod(methods, 2, "bar", String.class, Integer.class, Boolean.class);
        verifyMethod(methods, 3, "foo", Integer.class);
        verifyMethod(methods, 4, "noParam");
        try {
            DeclaredMemberIndexer.getMethodForIndex(1000, TestContainer.class);
            fail();
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    private void verifyFields(List<Field> fields, String... names) {
        assertEquals(names.length, fields.size());
        for (int i = 0; i < names.length; i++) {
            assertEquals(names[i], fields.get(i).getName());
            assertEquals(i, DeclaredMemberIndexer.getIndexForField(fields.get(i)));
            assertEquals(fields.get(i), DeclaredMemberIndexer.getFieldForIndex(i, TestContainer.class));
        }
    }

    private void verifyConstructor(List<Constructor<?>> constructors, int index, Class<?>... expectedParamTypes) {
        Constructor<?> constructor = constructors.get(index);
        assertEquals(constructor.getParameterCount(), expectedParamTypes.length);
        for (int i = 0; i < expectedParamTypes.length; i++) {
            if (!constructor.getParameterTypes()[i].getName().equals(expectedParamTypes[i].getName())) {
                fail(constructor.getParameterTypes()[i].getName() + " ne " + expectedParamTypes[i].getName());
            }
        }
        assertEquals(index, DeclaredMemberIndexer.getIndexForConstructor(constructor));
        assertEquals(constructor, DeclaredMemberIndexer.getConstructorForIndex(index, TestContainer.class));
    }

    private void verifyMethod(List<Method> methods, int index, String expectedName, Class<?>... expectedParamTypes) {
        Method method = methods.get(index);
        assertEquals(expectedName, method.getName());
        assertEquals(method.getParameterCount(), expectedParamTypes.length);
        for (int i = 0; i < expectedParamTypes.length; i++) {
            if (!method.getParameterTypes()[i].getName().equals(expectedParamTypes[i].getName())) {
                fail(method.getParameterTypes()[i].getName() + " ne " + expectedParamTypes[i].getName());
            }
        }
        assertEquals(index, DeclaredMemberIndexer.getIndexForMethod(method));
        assertEquals(method, DeclaredMemberIndexer.getMethodForIndex(index, TestContainer.class));
    }

    @SuppressWarnings("unused")
    private static class TestContainer extends Container {

        String foo;

        private Integer bar;

        public Date baz;

        protected Boolean qux;

        private TestContainer() {
        }

        protected TestContainer(String foo) {
        }

        TestContainer(String foo, Integer bar) {
        }

        public TestContainer(String bar, String foo) {
        }

        private TestContainer(Integer bar, String foo, BigDecimal qux) {
        }

        public void noParam() {
        }

        protected void foo(Integer p1) {
        }

        public void bar(Integer p1, String p2) {
        }

        private void bar(String p1, Integer p2) {
        }

        public void bar(String p1, Integer p2, Boolean p3) {
        }

    }

    @SuppressWarnings("unused")
    private static class Container {

        protected String inherited;

        protected Container() {
        }

        public Container(String[] foos) {
        }

        public String alpha() {
            return null;
        }

    }

}
