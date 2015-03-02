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
package org.jboss.weld.tests.unit.reflection.nested;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;

/**
 *
 * @see https://issues.jboss.org/browse/WELD-1081
 * @see https://issues.jboss.org/browse/WELD-1884
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public class NestedClassTest {

    @Test
    public void testInnerMemberClass() {
        assertFalse(Reflections.isStaticNestedClass(ClassWithNestedClasses.InnerClass.class));
    }

    @Test
    public void testMethodLocalInnerClass() {
        assertFalse(Reflections.isStaticNestedClass(ClassWithNestedClasses.getMethodLocalClass()));
    }

    @Test
    public void testConstructorLocalInnerClass() {
        assertFalse(Reflections.isStaticNestedClass(new ClassWithNestedClasses().getConstructorLocalClass()));
    }

    @Test
    @SuppressWarnings("serial")
    public void testAnnonymousClass() {
        assertFalse(Reflections.isStaticNestedClass(new Serializable() {
        }.getClass()));
        assertFalse(Reflections.isStaticNestedClass(ClassWithNestedClasses.anonymous.getClass()));
    }

    @Test
    public void testStaticNestedClass() {
        assertTrue(Reflections.isStaticNestedClass(ClassWithNestedClasses.StaticNestedClass.class));
    }

    @Test
    public void testNonNestedClass() {
        assertFalse(Reflections.isStaticNestedClass(ClassWithNestedClasses.class));
        assertTrue(Reflections.isTopLevelOrStaticNestedClass(ClassWithNestedClasses.class));
    }

}
