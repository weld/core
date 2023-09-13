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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.CovariantTypes;
import org.jboss.weld.util.reflection.GenericArrayTypeImpl;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.jboss.weld.util.reflection.WildcardTypeImpl;
import org.junit.Test;

/**
 * Test for {@link CovariantTypes}.
 *
 * @author Jozef Hartinger
 *
 */
public class CovariantTypesTest {

    /*
     * Raw type
     */
    @Test
    public void testRawTypeAssignableFromRawType() {
        assertTrue(CovariantTypes.isAssignableFrom(Number.class, Integer.class));
        assertTrue(CovariantTypes.isAssignableFrom(Number.class, Number.class));
        assertTrue(CovariantTypes.isAssignableFrom(int.class, int.class));
        assertTrue(CovariantTypes.isAssignableFrom(Object.class, int.class));
        assertFalse(CovariantTypes.isAssignableFrom(Integer.class, Number.class));
    }

    @Test
    public void testRawArrayAssignableFromRawArray() {
        final Type numbers = new Number[0].getClass();
        final Type integers = new Integer[0].getClass();
        final Type ints = new int[0].getClass();
        assertTrue(CovariantTypes.isAssignableFrom(Object.class, numbers));
        assertTrue(CovariantTypes.isAssignableFrom(Object.class, integers));
        assertTrue(CovariantTypes.isAssignableFrom(Object.class, ints));
        assertTrue(CovariantTypes.isAssignableFrom(numbers, numbers));
        assertTrue(CovariantTypes.isAssignableFrom(numbers, integers));
        assertFalse(CovariantTypes.isAssignableFrom(numbers, ints));
        assertTrue(CovariantTypes.isAssignableFrom(integers, integers));
        assertFalse(CovariantTypes.isAssignableFrom(integers, ints));
        assertFalse(CovariantTypes.isAssignableFrom(numbers, Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(integers, Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(ints, Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(integers, numbers));
        assertFalse(CovariantTypes.isAssignableFrom(ints, numbers));
        assertTrue(CovariantTypes.isAssignableFrom(new Number[0][].getClass(), new Number[0][].getClass()));
        assertTrue(CovariantTypes.isAssignableFrom(new Number[0][].getClass(), new Integer[0][].getClass()));
        assertFalse(CovariantTypes.isAssignableFrom(new Integer[0][].getClass(), new Number[0][].getClass()));
    }

    @Test
    public void testRawTypeAssignableFromParameterizedType() {
        assertTrue(
                CovariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(Map.class, String.class, Integer.class)));
        assertTrue(
                CovariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(Map.class, Object.class, Object.class)));
        assertTrue(CovariantTypes.isAssignableFrom(Map.class,
                new ParameterizedTypeImpl(HashMap.class, Object.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(Map.class, new ParameterizedTypeImpl(List.class, Object.class)));
    }

    @Test
    @SuppressWarnings("all")
    public <A, B extends Number, C extends Runnable & CharSequence> void testRawTypeAssignableFromTypeVariable() {
        final Type a = new TypeLiteral<A>() {
        }.getType();
        final Type b = new TypeLiteral<B>() {
        }.getType();
        final Type c = new TypeLiteral<C>() {
        }.getType();

        assertTrue(CovariantTypes.isAssignableFrom(Object.class, a));
        assertFalse(CovariantTypes.isAssignableFrom(Number.class, a));
        assertFalse(CovariantTypes.isAssignableFrom(Runnable.class, a));

        assertTrue(CovariantTypes.isAssignableFrom(Object.class, b));
        assertTrue(CovariantTypes.isAssignableFrom(Number.class, b));
        assertFalse(CovariantTypes.isAssignableFrom(Integer.class, b));
        assertFalse(CovariantTypes.isAssignableFrom(Runnable.class, b));

        assertTrue(CovariantTypes.isAssignableFrom(Object.class, c));
        assertTrue(CovariantTypes.isAssignableFrom(Runnable.class, c));
        assertTrue(CovariantTypes.isAssignableFrom(CharSequence.class, c));
        assertFalse(CovariantTypes.isAssignableFrom(String.class, c));
        assertFalse(CovariantTypes.isAssignableFrom(Integer.class, c));
    }

    @Test
    public void testRawTypeAssignableFromWildcardType() {
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Integer.class),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.defaultInstance())));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Integer.class),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.withUpperBound(Number.class))));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Integer.class),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.withUpperBound(Integer.class))));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Integer.class),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.withLowerBound(Integer.class))));
    }

    @Test
    public void testRawTypeAssignableFromGenericArrayType() {
        assertTrue(CovariantTypes.isAssignableFrom(new List[0].getClass(), new GenericArrayTypeImpl(List.class, Object.class)));
        assertTrue(
                CovariantTypes.isAssignableFrom(new List[0].getClass(), new GenericArrayTypeImpl(List.class, Integer.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new List[0].getClass(),
                new GenericArrayTypeImpl(ArrayList.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ArrayList[0].getClass(),
                new GenericArrayTypeImpl(List.class, Integer.class)));
    }

    /*
     * Parameterized types
     */
    @Test
    public void testParameterizedTypeAssignableFromRawType() {
        assertTrue(
                CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Object.class, Object.class), Map.class));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Runnable.class, Exception.class),
                Map.class));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Object.class, Object.class),
                HashMap.class));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, Runnable.class, Exception.class),
                HashMap.class));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, String.class), Collection.class));

        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, Integer.class), Double.class));
    }

    @Test
    public void testParameterizedTypeAssignableFromParameterizedType() {
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, String.class, Integer.class),
                new ParameterizedTypeImpl(Map.class, String.class, Integer.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Runnable.class),
                new ParameterizedTypeImpl(List.class, Runnable.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class),
                new ParameterizedTypeImpl(List.class, Object.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Map.class, String.class, Integer.class),
                new ParameterizedTypeImpl(HashMap.class, String.class, Integer.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Runnable.class),
                new ParameterizedTypeImpl(ArrayList.class, Runnable.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class),
                new ParameterizedTypeImpl(ArrayList.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class),
                new ParameterizedTypeImpl(List.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class),
                new ParameterizedTypeImpl(List.class, Runnable.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Number.class),
                new ParameterizedTypeImpl(List.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Number.class),
                new ParameterizedTypeImpl(List.class, Runnable.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Integer.class),
                new ParameterizedTypeImpl(List.class, Number.class)));
    }

    @Test
    @SuppressWarnings("all")
    public <A extends Collection<Number>, B extends List<Runnable> & Comparable<CharSequence>, C extends B> void testParameterizedTypeAssignableFromTypeVariable() {
        final Type a = new TypeLiteral<A>() {
        }.getType();
        final Type b = new TypeLiteral<B>() {
        }.getType();
        final Type c = new TypeLiteral<C>() {
        }.getType();

        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Number.class), a));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Iterable.class, Number.class), a));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Number.class), a));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Iterable.class, Object.class), a));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Iterable.class, Integer.class), a));

        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Iterable.class, Runnable.class), b));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Runnable.class), b));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Runnable.class), b));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, CharSequence.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(ArrayList.class, Runnable.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, FutureTask.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, String.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, Object.class), b));

        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Iterable.class, Runnable.class), c));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Collection.class, Runnable.class), c));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Runnable.class), c));
        assertTrue(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, CharSequence.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(ArrayList.class, Runnable.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Object.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, FutureTask.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, String.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(Comparable.class, Object.class), c));
    }

    @Test
    public void testParameterizedTypeAssignableFromWildcardType() {
        assertFalse(CovariantTypes.isAssignableFrom(
                new ParameterizedTypeImpl(Collection.class, new ParameterizedTypeImpl(Collection.class, Integer.class)),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.defaultInstance())));
        assertFalse(CovariantTypes.isAssignableFrom(
                new ParameterizedTypeImpl(Collection.class, new ParameterizedTypeImpl(Collection.class, Integer.class)),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.withUpperBound(Iterable.class))));
        assertFalse(CovariantTypes.isAssignableFrom(
                new ParameterizedTypeImpl(Collection.class, new ParameterizedTypeImpl(Collection.class, Integer.class)),
                new ParameterizedTypeImpl(Collection.class, WildcardTypeImpl.withUpperBound(Collection.class))));
        assertFalse(CovariantTypes.isAssignableFrom(
                new ParameterizedTypeImpl(Collection.class, new ParameterizedTypeImpl(Collection.class, Integer.class)),
                new ParameterizedTypeImpl(Collection.class,
                        WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Integer.class)))));
    }

    @Test
    public void testParameterizedTypeAssignableFromGenericArrayType() {
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Integer.class),
                new GenericArrayTypeImpl(List.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new ParameterizedTypeImpl(List.class, Integer.class),
                new GenericArrayTypeImpl(ArrayList.class, Integer.class)));
    }

    /*
     * Type variables
     */
    @Test
    @SuppressWarnings("serial")
    public <T, S extends Number> void testTypeVariableAssignableFromRawType() {
        Type t = new TypeLiteral<T>() {
        }.getType();
        Type s = new TypeLiteral<S>() {
        }.getType();
        assertFalse(CovariantTypes.isAssignableFrom(t, Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(t, List.class));
        assertFalse(CovariantTypes.isAssignableFrom(s, Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(s, Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(s, Long.class));
    }

    @Test
    @SuppressWarnings("serial")
    public <T, S extends List<Number>> void testTypeVariableAssignableFromParameterizedType() {
        Type t = new TypeLiteral<T>() {
        }.getType();
        Type s = new TypeLiteral<S>() {
        }.getType();
        assertFalse(CovariantTypes.isAssignableFrom(t, new ParameterizedTypeImpl(List.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(t, new ParameterizedTypeImpl(List.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(s, new ParameterizedTypeImpl(List.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(s, new ParameterizedTypeImpl(List.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(s, new ParameterizedTypeImpl(List.class, Long.class)));
    }

    @Test
    @SuppressWarnings("all")
    public <A, B, C extends Number, D extends Integer> void testTypeVariableAssignableFromTypeVariable() {
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
        typeVariables[4] = new TypeLiteral<D[]>() {
        }.getType();

        // type variables should only be assignable if they are equal (unless one extends the other)
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                if (i == j) {
                    assertTrue(CovariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                } else {
                    assertFalse(CovariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                }
            }
        }
    }

    @Test
    @SuppressWarnings("serial")
    public <A, B extends A, C extends A, D extends C, E extends D> void testTypeVariableAssignableFromTypeVariable2() {
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

        // a type variable should only be assignable from itself and from a type variable extending it (even transitively)
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                /*
                 * E (4) extends D (3) extends C (2) extends A (0)
                 * B (1) extends A (0)
                 */
                if (i == j || i == 0 || (i < j && i != 1)) {
                    assertTrue(CovariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                } else {
                    assertFalse(CovariantTypes.isAssignableFrom(typeVariables[i], typeVariables[j]));
                }
            }
        }
    }

    @Test
    @SuppressWarnings("all")
    public <A, B extends Number> void testTypeVariableAssignableFromWildcard() {
        Type a = new TypeLiteral<A>() {
        }.getType();
        Type b = new TypeLiteral<B>() {
        }.getType();
        assertFalse(CovariantTypes.isAssignableFrom(a, WildcardTypeImpl.defaultInstance()));
        assertFalse(CovariantTypes.isAssignableFrom(a, WildcardTypeImpl.withUpperBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(a, WildcardTypeImpl.withLowerBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, WildcardTypeImpl.defaultInstance()));
        assertFalse(CovariantTypes.isAssignableFrom(b, WildcardTypeImpl.withUpperBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, WildcardTypeImpl.withLowerBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, WildcardTypeImpl.withUpperBound(Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, WildcardTypeImpl.withLowerBound(Integer.class)));
    }

    @Test
    @SuppressWarnings("all")
    public <A, B extends List<Integer>> void testTypeVariableAssignableFromGenericArrayType() {
        Type a = new TypeLiteral<A>() {
        }.getType();
        Type b = new TypeLiteral<B>() {
        }.getType();
        assertFalse(CovariantTypes.isAssignableFrom(a, new GenericArrayTypeImpl(List.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(a, new GenericArrayTypeImpl(List.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(a, new GenericArrayTypeImpl(List.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(List.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(List.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(List.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(ArrayList.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(ArrayList.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(b, new GenericArrayTypeImpl(ArrayList.class, Integer.class)));
    }

    /*
     * Wildcard
     */
    @Test
    public void testWildcardAssignableFromRawType() {
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), Number.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Number.class), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Integer.class), Number.class));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class), Number.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class), Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class), Integer.class));
    }

    @Test
    @SuppressWarnings("serial")
    public <A, B extends Number, C extends B, D extends Number & Serializable> void testWildcardWithTypeVariableAssignableFromRawType() {
        final Type a = new TypeLiteral<A>() {
        }.getType();
        final Type b = new TypeLiteral<B>() {
        }.getType();
        final Type c = new TypeLiteral<C>() {
        }.getType();
        final Type d = new TypeLiteral<D>() {
        }.getType();

        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(a), Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(b), Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(b), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(b), Integer.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(c), Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(c), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(c), Integer.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(d), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(d), Serializable.class));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(a), Object.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(a), Number.class));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(b), Object.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(b), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(b), Integer.class));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(c), Object.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(c), Number.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(c), Integer.class));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d), Object.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d), Number.class));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d), Serializable.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d), Integer.class));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d), Runnable.class));
    }

    @Test
    public void testWildcardAssignableFromParameterizedType() {
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                new ParameterizedTypeImpl(Collection.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(List.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(ArrayList.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Object.class)));

        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Iterable.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(List.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(ArrayList.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class)),
                new ParameterizedTypeImpl(Collection.class, Object.class)));
    }

    @Test
    @SuppressWarnings("serial")
    public <A, B extends Number, C extends Runnable & Appendable> void testWildcardAssignableFromTypeVariable() {
        final Type a = new TypeLiteral<A>() {
        }.getType();
        final Type b = new TypeLiteral<B>() {
        }.getType();
        final Type c = new TypeLiteral<C>() {
        }.getType();

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), a));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), b));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), c));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Number.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Integer.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class), b));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Integer.class), b));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Runnable.class), c));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Appendable.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Runnable.class), c));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Appendable.class), c));
    }

    @Test
    @SuppressWarnings("serial")
    public <A, B extends A, C extends A, D extends C, E extends D> void testWildcardWithTypeVariableAssignableFromTypeVariable() {
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

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                /*
                 * E (4) extends D (3) extends C (2) extends A (0)
                 * B (1) extends A (0)
                 */
                if (i == j || i == 0 || (i < j && i != 1)) {
                    assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(typeVariables[i]),
                            typeVariables[j]));
                    assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(typeVariables[j]),
                            typeVariables[i]));
                } else {
                    assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(typeVariables[i]),
                            typeVariables[j]));
                    assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(typeVariables[j]),
                            typeVariables[i]));
                }
            }
        }
    }

    @Test
    public void testWildcardAssignableFromWildcard() {
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(), WildcardTypeImpl.defaultInstance()));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                WildcardTypeImpl.withUpperBound(Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Number.class),
                WildcardTypeImpl.withUpperBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Number.class),
                WildcardTypeImpl.defaultInstance()));

        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Integer.class),
                WildcardTypeImpl.defaultInstance()));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Integer.class),
                WildcardTypeImpl.withUpperBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Integer.class),
                WildcardTypeImpl.withUpperBound(Integer.class)));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                WildcardTypeImpl.withLowerBound(Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Number.class),
                WildcardTypeImpl.withLowerBound(Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Integer.class),
                WildcardTypeImpl.withLowerBound(Integer.class)));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class),
                WildcardTypeImpl.withLowerBound(Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Integer.class),
                WildcardTypeImpl.withLowerBound(Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Number.class),
                WildcardTypeImpl.withLowerBound(Integer.class)));
    }

    @Test
    @SuppressWarnings("serial")
    public <A extends Throwable, B extends A, C extends B, D extends Exception> void testWildcardAssignableFromWildcard2() {
        // (both) wildcards bounded by a type variable
        final int count = 4;
        Type[] typeVariables = new Type[count];
        final Type a = typeVariables[0] = new TypeLiteral<A>() {
        }.getType();
        final Type b = typeVariables[1] = new TypeLiteral<B>() {
        }.getType();
        final Type c = typeVariables[2] = new TypeLiteral<C>() {
        }.getType();
        final Type d = typeVariables[3] = new TypeLiteral<D>() {
        }.getType();

        for (int i = 0; i < count; i++) {
            // unbounded wildcard should be assignable from anything
            assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                    WildcardTypeImpl.withUpperBound(typeVariables[i])));
            assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                    WildcardTypeImpl.withLowerBound(typeVariables[i])));
            for (int j = 0; j < count; j++) {
                // wildcard with an upper bound is UNassignable from a wildcard with a lower bound and vice versa
                assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(typeVariables[i]),
                        WildcardTypeImpl.withLowerBound(typeVariables[j])));
                assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(typeVariables[i]),
                        WildcardTypeImpl.withUpperBound(typeVariables[j])));
                if (i == j || (i < j && j != 3)) {
                    assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(typeVariables[i]),
                            WildcardTypeImpl.withUpperBound(typeVariables[j])));
                    assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(typeVariables[j]),
                            WildcardTypeImpl.withLowerBound(typeVariables[i])));
                } else {
                    assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(typeVariables[i]),
                            WildcardTypeImpl.withUpperBound(typeVariables[j])));
                    assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(typeVariables[j]),
                            WildcardTypeImpl.withLowerBound(typeVariables[i])));
                }
            }
        }

        // one wildcard bounded by a class and the other bounded by a type variable
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Exception.class),
                WildcardTypeImpl.withUpperBound(d)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(RuntimeException.class),
                WildcardTypeImpl.withUpperBound(d)));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Serializable.class),
                WildcardTypeImpl.withUpperBound(b)));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Throwable.class),
                WildcardTypeImpl.withUpperBound(b)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Exception.class),
                WildcardTypeImpl.withUpperBound(b)));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(a),
                WildcardTypeImpl.withLowerBound(Throwable.class)));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(c),
                WildcardTypeImpl.withLowerBound(Throwable.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(a),
                WildcardTypeImpl.withLowerBound(Exception.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(c),
                WildcardTypeImpl.withLowerBound(Exception.class)));

        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d),
                WildcardTypeImpl.withLowerBound(Throwable.class)));
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d),
                WildcardTypeImpl.withLowerBound(Exception.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d),
                WildcardTypeImpl.withLowerBound(RuntimeException.class)));

        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(RuntimeException.class),
                WildcardTypeImpl.withLowerBound(a)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Throwable.class),
                WildcardTypeImpl.withLowerBound(a)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(RuntimeException.class),
                WildcardTypeImpl.withLowerBound(d)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Throwable.class),
                WildcardTypeImpl.withLowerBound(d)));

        // mix upper bounds with lower bounds
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(Exception.class),
                WildcardTypeImpl.withLowerBound(d)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(Exception.class),
                WildcardTypeImpl.withUpperBound(d)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withLowerBound(d),
                WildcardTypeImpl.withUpperBound(Exception.class)));
        assertFalse(CovariantTypes.isAssignableFrom(WildcardTypeImpl.withUpperBound(d),
                WildcardTypeImpl.withLowerBound(Exception.class)));
    }

    @Test
    public void testWildcardAssignableFromGenericArrayType() {
        assertTrue(CovariantTypes.isAssignableFrom(WildcardTypeImpl.defaultInstance(),
                new GenericArrayTypeImpl(List.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(List.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(ArrayList.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withUpperBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(Collection.class, Number.class)));

        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(List.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(Collection.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(
                WildcardTypeImpl.withLowerBound(new GenericArrayTypeImpl(List.class, Number.class)),
                new GenericArrayTypeImpl(ArrayList.class, Number.class)));
    }

    /*
     * GenericArrayType
     */
    @Test
    public void testGenericArrayTypeAssignableFromRawType() {
        assertTrue(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class), new List[0].getClass()));
        assertTrue(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new ArrayList[0].getClass()));
        assertFalse(
                CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class), new Object[0].getClass()));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class), new Set[0].getClass()));
    }

    @Test
    public void testGenericArrayTypeAssignableFromParameterizedType() {
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new ParameterizedTypeImpl(List.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class),
                new ParameterizedTypeImpl(List.class, Number.class)));
    }

    @Test
    @SuppressWarnings("serial")
    public <A, B extends List<Number>> void testGenericArrayTypeAssignableFromTypeVariable() {
        Type a = new TypeLiteral<A>() {
        }.getType();
        Type b = new TypeLiteral<B>() {
        }.getType();
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class), a));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(List.class, Number.class), b));
    }

    @Test
    @SuppressWarnings("all")
    public <A, B extends Number> void testGenericArrayTypeAssignableFromWildcard() {
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                WildcardTypeImpl.defaultInstance()));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Number.class))));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Number.class))));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                WildcardTypeImpl.withUpperBound(new ParameterizedTypeImpl(Collection.class, Integer.class))));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                WildcardTypeImpl.withLowerBound(new ParameterizedTypeImpl(Collection.class, Integer.class))));
    }

    @Test
    public void testGenericArrayTypeAssignableFromGenericArrayType() {
        assertTrue(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(Collection.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(List.class, Number.class)));
        assertTrue(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(ArrayList.class, Number.class)));
        assertTrue(
                CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, WildcardTypeImpl.defaultInstance()),
                        new GenericArrayTypeImpl(ArrayList.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(Iterable.class, Number.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(Collection.class, Object.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(Collection.class, Integer.class)));
        assertFalse(CovariantTypes.isAssignableFrom(new GenericArrayTypeImpl(Collection.class, Number.class),
                new GenericArrayTypeImpl(Collection.class, Double.class)));
    }
}
