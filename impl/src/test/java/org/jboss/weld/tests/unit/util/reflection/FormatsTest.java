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
package org.jboss.weld.tests.unit.util.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.util.reflection.Formats;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class FormatsTest {

    /**
     * This test will FAIL with IBM JVM, because Apache BCEL classes are not present there. Therefore util method
     * Formats.getLineNumber always returns 0 as line number.
     * Apache BCEL classes are used to look up the line numbers in bytecode.
     */
    @Test
    public void testGetLineNumber() throws NoSuchMethodException, SecurityException {

        Class<Bean> beanClass = Bean.class;

        // Initializers
        assertLineFound(findMethod(beanClass, "init", Vanilla.class));
        assertLineFound(findMethod(beanClass, "foo", Vanilla.class, BeanManager.class, List.class));
        // Constructors
        assertLineFound(findConstructor(beanClass, Vanilla.class));
    }

    @SuppressWarnings("serial")
    @Test
    public void testFormatWildcardType() {
        assertEquals("List<?>", Formats.formatType(new TypeLiteral<List<?>>() {
        }.getType()));
        assertEquals("java.util.List<?>", Formats.formatType(new TypeLiteral<List<?>>() {
        }.getType(), false));
        assertEquals("List<? extends Number>", Formats.formatType(new TypeLiteral<List<? extends Number>>() {
        }.getType()));
        // We always use simple names for actual type arguments
        assertEquals("java.util.List<? extends Number>", Formats.formatType(new TypeLiteral<List<? extends Number>>() {
        }.getType(), false));
        assertEquals("List<? super Number>", Formats.formatType(new TypeLiteral<List<? super Number>>() {
        }.getType()));
        assertEquals("java.util.List<? super Number>", Formats.formatType(new TypeLiteral<List<? super Number>>() {
        }.getType(), false));
    }

    void assertLineFound(Member member) {
        int line = Formats.getLineNumber(member);
        // We can't test the exact line as it doesn't seem to be precise and portable
        // E.g. for methods it's declaration line + 1 and for constructors it's declaration line
        assertTrue("Line: " + line, line > 0);
        // System.out.println(member + " :" + line);
    }

    Constructor<?> findConstructor(Class<?> javaClass, Class<?>... parameterTypes) {
        Class<?> clazz = javaClass;
        while (clazz != Object.class && clazz != null) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
                    return constructor;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    Method findMethod(Class<?> javaClass, String name, Class<?>... parameterTypes) {
        Class<?> clazz = javaClass;
        while (clazz != Object.class && clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

}
