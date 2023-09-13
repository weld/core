/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.resolution;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.InvariantTypes;
import org.jboss.weld.util.reflection.GenericArrayTypeImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jboss.weld.util.reflection.WildcardTypeImpl;
import org.junit.Test;

/**
 * Test for {@link InvariantTypes}
 *
 * @author Jozef Hartinger
 *
 */
public class InvariantTypesTest {

    /*
     * Raw type
     */
    @Test
    public void testRawTypeAssignableFromRawType() {
        assertTrue(InvariantTypes.isAssignableFrom(Number.class, Number.class));
        assertTrue(InvariantTypes.isAssignableFrom(Integer.class, Integer.class));
        assertTrue(InvariantTypes.isAssignableFrom(int.class, int.class));
        assertFalse(InvariantTypes.isAssignableFrom(Integer.class, Number.class));
        assertFalse(InvariantTypes.isAssignableFrom(Number.class, Integer.class));
    }

    @Test
    public void testRawArrayAssignableFromRawArray() {
        final Type numbers = new Number[0].getClass();
        final Type integers = new Integer[0].getClass();
        final Type ints = new int[0].getClass();
        assertFalse(InvariantTypes.isAssignableFrom(Object.class, numbers));
        assertFalse(InvariantTypes.isAssignableFrom(Object.class, integers));
        assertFalse(InvariantTypes.isAssignableFrom(Object.class, ints));
        assertTrue(InvariantTypes.isAssignableFrom(numbers, numbers));
        assertFalse(InvariantTypes.isAssignableFrom(numbers, integers));
        assertFalse(InvariantTypes.isAssignableFrom(numbers, ints));
        assertTrue(InvariantTypes.isAssignableFrom(integers, integers));
        assertFalse(InvariantTypes.isAssignableFrom(integers, ints));
        assertFalse(InvariantTypes.isAssignableFrom(numbers, Object.class));
        assertFalse(InvariantTypes.isAssignableFrom(integers, Object.class));
        assertFalse(InvariantTypes.isAssignableFrom(ints, Object.class));
        assertFalse(InvariantTypes.isAssignableFrom(integers, numbers));
        assertFalse(InvariantTypes.isAssignableFrom(ints, numbers));

        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), numbers));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), integers));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), ints));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(numbers), numbers));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(numbers), integers));
        assertFalse(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(numbers), ints));
        assertFalse(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(integers), numbers));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(integers), integers));
        assertFalse(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(integers), ints));
        assertFalse(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(ints), numbers));
        assertFalse(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(ints), integers));
        assertTrue(InvariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(ints), ints));
    }

    @Test
    public void testRawTypeAssignableFromParameterizedType() {
        assertFalse(
                InvariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(Map.class, String.class, Integer.class)));
        assertFalse(
                InvariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(Map.class, Object.class, Object.class)));
        assertFalse(InvariantTypes.isAssignableFrom(Map.class,
                new ParameterizedTypeImpl(HashMap.class, Object.class, Object.class)));
        assertFalse(InvariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(List.class, Object.class)));
    }

    @Test
    public void testRawTypeAssignableFromGenericArrayType() {
        assertFalse(
                InvariantTypes.isAssignableFrom(new List[0].getClass(), new GenericArrayTypeImpl(List.class, Object.class)));
        assertFalse(
                InvariantTypes.isAssignableFrom(new List[0].getClass(), new GenericArrayTypeImpl(List.class, Integer.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new List[0].getClass(),
                new GenericArrayTypeImpl(ArrayList.class, Integer.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new ArrayList[0].getClass(),
                new GenericArrayTypeImpl(List.class, Integer.class)));
    }

    /*
     * Parameterized types
     */
    @Test
    public void testParameterizedTypeAssignableFromParameterizedType() {
        assertTrue(InvariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Number.class, String.class),
                new ParameterizedTypeImpl(Map.class, Number.class, String.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Number.class, String.class),
                new ParameterizedTypeImpl(HashMap.class, Number.class, String.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Number.class, String.class),
                new ParameterizedTypeImpl(Map.class, Integer.class, String.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Number.class, String.class),
                new ParameterizedTypeImpl(Map.class, Number.class, Object.class)));
    }

    /*
     * Type variables
     */
    @Test
    @SuppressWarnings("all")
    public <A, B, C extends Number, D extends Integer, E extends A> void testTypeVariableAssignableFromTypeVariable() {
        final int count = 5;
        Type[] typeVariables = new Type[count];
        typeVariables[0] = new TypeLiteral<A>() {
        }.getType();
        typeVariables[1] = new TypeLiteral<B>() {
        }.getType();
        typeVariables[2] = new TypeLiteral<C>() {
        }.getType();
        typeVariables[3] = new TypeLiteral<D>() {
        }.getType();
        typeVariables[4] = new TypeLiteral<E>() {
        }.getType();

        // type variables should only be assignable if they are equal
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (i == j) {
                    assertTrue(InvariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                } else {
                    assertFalse(InvariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                }
            }
        }
    }

    /*
     * Generic array types
     */
    @Test
    public void testGenericArrayTypeAssignableGenericArrayType() {
        assertTrue(InvariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new GenericArrayTypeImpl(List.class, Number.class)));
        assertTrue(InvariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Map.class, String.class, Runnable.class),
                new GenericArrayTypeImpl(Map.class, String.class, Runnable.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new GenericArrayTypeImpl(List.class, Integer.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new GenericArrayTypeImpl(List.class, Object.class)));
        assertFalse(InvariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Map.class, Object.class, Object.class),
                new GenericArrayTypeImpl(Map.class, String.class, Runnable.class)));
    }
}
