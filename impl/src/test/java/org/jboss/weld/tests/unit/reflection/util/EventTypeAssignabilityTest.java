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
package org.jboss.weld.tests.unit.reflection.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.EventTypeAssignabilityRules;
import org.junit.Test;


public class EventTypeAssignabilityTest extends BeanTypeAssignabilityTest {

    @Override
    protected AssignabilityRules getRules() {
        return EventTypeAssignabilityRules.instance();
    }

    @Test
    public <E> void testTypeVariableMatchesFoo() throws Exception {
        Type fooType = Foo.class;
        Type variableType = new TypeLiteral<E>(){}.getType();
        Assert.assertTrue("E should be assignable from Foo", EventTypeAssignabilityRules.instance().matches(variableType, fooType));
    }

    @Test
    public <E> void testVariableFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>(){}.getType();
        Type variableFooType = new TypeLiteral<Foo<E>>(){}.getType();
        Assert.assertTrue("Foo<E> should be assignable from Foo<String>", EventTypeAssignabilityRules.instance().matches(variableFooType, stringFooType));
    }

    @Test
    public <E> void testVariableFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>(){}.getType();
        Type variableFooArrayType = new TypeLiteral<Foo<E>[]>(){}.getType();
        Assert.assertTrue("Foo<E>[] should be assignable from Foo<String>[]", EventTypeAssignabilityRules.instance().matches(variableFooArrayType, stringFooArrayType));
    }

    @Test
    public void testWildcardFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<?>>(){}.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<?>", EventTypeAssignabilityRules.instance().matches(type, type));
    }

    @Test
    public void testWildcardFooArrayMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<?>[]>(){}.getType();
        Assert.assertTrue("Foo<?>[] should be assignable from itself", getRules().matches(type, type));
    }

    @Test
    public void testWildcardFooMatchesBoundedWildcardFoo() throws Exception {
        Type boundedWildcardFooType = new TypeLiteral<Foo<? extends Number>>(){}.getType();
        Type wildcardFooType = new TypeLiteral<Foo<?>>(){}.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<? extends Number>", getRules().matches(wildcardFooType, boundedWildcardFooType));
    }

    @Test
    public <F extends Number> void testParameterizedBeanWithBoundedVariableTypeParameter() throws Exception {
        Assert.assertTrue("Foo<F extends Number> should be assignable to Foo",
            getRules().matches(
                Foo.class,
                new TypeLiteral<Foo<F>>() { }.getType()));
    }

    @Test
    public void testFooArrayMatchesItself() throws Exception {
        Type clazz = Foo[].class;
        Type genericArrayType = new TypeLiteral<Foo[]>(){}.getType();
        Assert.assertTrue("array should match itself", getRules().matches(clazz, clazz));
        Assert.assertTrue("array should match itself", getRules().matches(genericArrayType, genericArrayType));
        Assert.assertTrue("array should match itself", getRules().matches(genericArrayType, clazz));
        Assert.assertTrue("array should match itself", getRules().matches(clazz, genericArrayType));
    }

    @Test
    public void testWildcardMatchesParameterizedType() {
        Type eventType = new TypeLiteral<Foo<List<String>>>() {}.getType();
        Type observerType1 = new TypeLiteral<Foo<? extends List>>() {}.getType();
        Type observerType2 = new TypeLiteral<Foo<?>>() {}.getType();
        Type observerType3 = new TypeLiteral<Foo<? extends List<String>>>() {}.getType();
        assertTrue("Foo<? extends List> should be assignable from Foo<List<String>>", getRules().matches(observerType1, eventType));
        assertTrue("Foo<?> should be assignable from Foo<List<String>>", getRules().matches(observerType2, eventType));
        assertTrue("Foo<? extends List<String> should be assignable from Foo<List<String>>", getRules().matches(observerType3, eventType));
    }

    @Test
    @SuppressWarnings("serial")
    public void testWildcardMatchesWildcard() {
        Type eventType1 = new TypeLiteral<Foo<? extends Exception>>() {}.getType();
        Type eventType2 = new TypeLiteral<Foo<? extends RuntimeException>>() {}.getType();
        Type eventType3 = new TypeLiteral<Foo<? extends UnsupportedOperationException>>() {}.getType();
        Type eventType4 = new TypeLiteral<Foo<? super Exception>>() {}.getType();
        Type observerType1 = new TypeLiteral<Foo<? extends Throwable>>() {}.getType();
        Type observerType2 = new TypeLiteral<Foo<? extends RuntimeException>>() {}.getType();
        Type observerType3 = new TypeLiteral<Foo<? super Throwable>>() {}.getType();
        Type observerType4 = new TypeLiteral<Foo<? super RuntimeException>>() {}.getType();

        assertTrue(getRules().matches(observerType1, eventType1));
        assertTrue(getRules().matches(observerType1, eventType2));
        assertTrue(getRules().matches(observerType1, eventType3));
        assertFalse(getRules().matches(observerType1, eventType4));
        assertFalse(getRules().matches(observerType2, eventType1));
        assertTrue(getRules().matches(observerType2, eventType2));
        assertTrue(getRules().matches(observerType2, eventType3));
        assertFalse(getRules().matches(observerType2, eventType4));
        assertFalse(getRules().matches(observerType3, eventType1));
        assertFalse(getRules().matches(observerType3, eventType2));
        assertFalse(getRules().matches(observerType3, eventType3));
        assertTrue(getRules().matches(observerType3, eventType4));
        assertTrue(getRules().matches(observerType4, eventType1));
        assertTrue(getRules().matches(observerType4, eventType2));
        assertFalse(getRules().matches(observerType4, eventType3));
        assertFalse(getRules().matches(observerType4, eventType4));
    }
}
