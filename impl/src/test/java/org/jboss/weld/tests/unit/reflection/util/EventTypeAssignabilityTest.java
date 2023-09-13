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

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.EventTypeAssignabilityRules;
import org.jboss.weld.util.reflection.GenericArrayTypeImpl;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("serial")
public class EventTypeAssignabilityTest {

    protected AssignabilityRules getRules() {
        return EventTypeAssignabilityRules.instance();
    }

    @Test
    public <E> void testTypeVariableMatchesFoo() throws Exception {
        Type fooType = Foo.class;
        Type variableType = new TypeLiteral<E>() {
        }.getType();
        Assert.assertTrue("E should be assignable from Foo", getRules().matches(variableType, fooType));
    }

    @Test
    public <E> void testVariableFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Type variableFooType = new TypeLiteral<Foo<E>>() {
        }.getType();
        Assert.assertTrue("Foo<E> should be assignable from Foo<String>", getRules().matches(variableFooType, stringFooType));
    }

    @Test
    public <E> void testVariableFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type variableFooArrayType = new TypeLiteral<Foo<E>[]>() {
        }.getType();
        Assert.assertTrue("Foo<E>[] should be assignable from Foo<String>[]",
                getRules().matches(variableFooArrayType, stringFooArrayType));
    }

    @Test
    public <F extends Number> void testParameterizedBeanWithBoundedVariableTypeParameter() throws Exception {
        Assert.assertTrue("Foo<F extends Number> should be assignable to Foo",
                getRules().matches(
                        Foo.class,
                        new TypeLiteral<Foo<F>>() {
                        }.getType()));
    }

    @Test
    public void testFooArrayMatchesItself() throws Exception {
        Type clazz = Foo[].class;
        Type genericArrayType = new TypeLiteral<Foo[]>() {
        }.getType();
        Assert.assertTrue("array should match itself", getRules().matches(clazz, clazz));
        Assert.assertTrue("array should match itself", getRules().matches(genericArrayType, genericArrayType));
        Assert.assertTrue("array should match itself", getRules().matches(genericArrayType, clazz));
        Assert.assertTrue("array should match itself", getRules().matches(clazz, genericArrayType));
    }

    @Test
    public void testWildcardMatchesParameterizedType() {
        Type eventType = new TypeLiteral<Foo<List<String>>>() {
        }.getType();
        Type observerType1 = new TypeLiteral<Foo<? extends List>>() {
        }.getType();
        Type observerType2 = new TypeLiteral<Foo<?>>() {
        }.getType();
        Type observerType3 = new TypeLiteral<Foo<? extends List<String>>>() {
        }.getType();
        assertTrue("Foo<? extends List> should be assignable from Foo<List<String>>",
                getRules().matches(observerType1, eventType));
        assertTrue("Foo<?> should be assignable from Foo<List<String>>", getRules().matches(observerType2, eventType));
        assertTrue("Foo<? extends List<String> should be assignable from Foo<List<String>>",
                getRules().matches(observerType3, eventType));
    }

    @Test
    public void testParameterizedTypes() {
        Type observerType = new TypeLiteral<Foo<Number>>() {
        }.getType();
        Type eventType1 = new TypeLiteral<Foo<Number>>() {
        }.getType();
        Type eventType2 = new TypeLiteral<Foo<Integer>>() {
        }.getType();
        assertTrue("Foo<Number> should be assignable to Foo<Number>", getRules().matches(observerType, eventType1));
        assertFalse("Foo<Integer> should not be assignable to Foo<Number>", getRules().matches(observerType, eventType2));
    }

    @Test
    public void testArrayCovariance1() {
        Type type1 = new Number[0].getClass();
        Type type2 = new Integer[0].getClass();
        assertTrue(getRules().matches(type1, type2));
    }

    @Test
    public void testArrayCovariance2() {
        Type type1 = new GenericArrayTypeImpl(new TypeLiteral<List<?>>() {
        }.getType());
        Type type2 = new List[0].getClass();
        assertTrue(getRules().matches(type2, type1));
    }

    @Test
    public void testBoxingNotAppliedOnArrays() {
        Type type1 = new int[0].getClass();
        Type type2 = new Integer[0].getClass();
        assertFalse(getRules().matches(type1, type2));
    }

    @Test
    public void testWildcardFooArrayMatchesStringFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>() {
        }.getType();
        Assert.assertTrue("Foo<?>[] should not be assignable from Foo<String>[]",
                getRules().matches(wildcardFooArrayType, stringFooArrayType));
    }

    @Test
    public void testIntegerFooMatchesItself() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>>() {
        }.getType();
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testIntegerFooDoesNotMatchStringFoo() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>>() {
        }.getType();
        Type type2 = new TypeLiteral<Foo<String>>() {
        }.getType();
        Assert.assertFalse("Foo<Integer> should not match Foo<String>", getRules().matches(type1, type2));
    }

    @Test
    public void testFooMatchesItself() throws Exception {
        Type type = Foo.class;
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testParameterizedArrayDoesNotMatchComponentOfArray() throws Exception {
        Type arrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type componentType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Assert.assertFalse("array type should not match its component type", getRules().matches(arrayType, componentType));
    }

    @Test
    public void testParameterizedArrayMatches() throws Exception {
        Type type = new TypeLiteral<Foo<Integer>[]>() {
        }.getType();
        Assert.assertTrue("type should match itself", getRules().matches(type, type));
    }

    @Test
    public void testArraysDontMatch() throws Exception {
        Type type1 = new TypeLiteral<Foo<Integer>[]>() {
        }.getType();
        Type type2 = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Assert.assertFalse("Foo<Integer>[] should not match Foo<String>[]", getRules().matches(type1, type2));
    }

    @Test
    public void testWildcardFooMatchesStringFoo() throws Exception {
        Type stringFooType = new TypeLiteral<Foo<String>>() {
        }.getType();
        Type wildcardFooType = new TypeLiteral<Foo<?>>() {
        }.getType();
        Assert.assertTrue("Foo<?> should be assignable from Foo<String>", getRules().matches(wildcardFooType, stringFooType));
    }

    @Test
    public void testStringFooArrayDoesNotMatchWildcardFooArray() throws Exception {
        Type stringFooArrayType = new TypeLiteral<Foo<String>[]>() {
        }.getType();
        Type wildcardFooArrayType = new TypeLiteral<Foo<?>[]>() {
        }.getType();
        Assert.assertFalse("Foo<String>[] should not be assignable from Foo<?>[]",
                getRules().matches(stringFooArrayType, wildcardFooArrayType));
    }

    @Test
    public void testRawRequiredTypeMatchesParameterizedBeanWithObjectTypeParameter() throws Exception {
        Assert.assertTrue("Foo<Object> should be assignable to Foo",
                getRules().matches(Foo.class, new TypeLiteral<Foo<Object>>() {
                }.getType()));
    }

    @Test
    public <T, S extends Integer> void testArrays() {
        Assert.assertTrue("int[][] should be assignable to int[][]",
                getRules().matches(new int[0][].getClass(), new int[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to Integer[][]",
                getRules().matches(new Integer[0][].getClass(), new Integer[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to Number[][]",
                getRules().matches(new Number[0][].getClass(), new Integer[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to T[]", getRules().matches(new TypeLiteral<T[]>() {
        }.getType(), new Integer[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to T[][]", getRules().matches(new TypeLiteral<T[][]>() {
        }.getType(), new Integer[0][].getClass()));
        Assert.assertFalse("Integer[][] should not be assignable to S[] where S extends Integer",
                getRules().matches(new TypeLiteral<S[]>() {
                }.getType(), new Integer[0][].getClass()));
        Assert.assertTrue("Integer[][] should be assignable to S[][] where S extends Integer",
                getRules().matches(new TypeLiteral<S[][]>() {
                }.getType(), new Integer[0][].getClass()));
        Assert.assertFalse("Number[][] should not be assignable to S[][] where S extends Integer",
                getRules().matches(new TypeLiteral<S[][]>() {
                }.getType(), new Number[0][].getClass()));
    }

    @Test
    public void testArrayBoxing() {
        /*
         * This is not explicitly said in the CDI spec however Java SE does not support array boxing so neither should CDI.
         */
        Assert.assertFalse("Integer[] should not be assignable to int[]",
                getRules().matches(new int[0].getClass(), new Integer[0].getClass()));
        Assert.assertFalse("int[] should not be assignable to Integer[]",
                getRules().matches(new Integer[0].getClass(), new int[0].getClass()));
    }

    @Test
    public <T1 extends Number, T2 extends T1> void testTypeVariableWithTypeVariableBound() {
        Assert.assertTrue("Number should be assignable to T2 extends T1 extends Number",
                getRules().matches(new TypeLiteral<T2>() {
                }.getType(), Number.class));
        Assert.assertFalse("Number should not be assignable to T2 extends T1 extends Runnable",
                getRules().matches(new TypeLiteral<T2>() {
                }.getType(), Runnable.class));
    }

    @Test
    public <T1 extends Number, T2 extends T1> void testWildcardWithTypeVariableBound() {
        Assert.assertTrue("List<Number> should be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Number>>() {
                }.getType()));
        Assert.assertTrue("List<Integer> should be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Integer>>() {
                }.getType()));
        Assert.assertFalse("List<Object> should not be assignable to List<? extends T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? extends T2>>() {
                }.getType(), new TypeLiteral<List<Object>>() {
                }.getType()));

        Assert.assertTrue("List<Number> should be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Number>>() {
                }.getType()));
        Assert.assertFalse("List<Integer> should not be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Integer>>() {
                }.getType()));
        Assert.assertTrue("List<Object> should be assignable to List<? super T2 extends T1 extends Number>",
                getRules().matches(new TypeLiteral<List<? super T2>>() {
                }.getType(), new TypeLiteral<List<Object>>() {
                }.getType()));
    }
}
