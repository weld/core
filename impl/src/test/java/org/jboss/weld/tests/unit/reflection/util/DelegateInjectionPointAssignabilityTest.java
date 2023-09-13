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
package org.jboss.weld.tests.unit.reflection.util;

import java.util.List;

import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.resolution.AssignabilityRules;
import org.jboss.weld.resolution.DelegateInjectionPointAssignabilityRules;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Section 8.3.1 of the CDI specification.
 *
 * @author Jozef Hartinger
 *
 */
@SuppressWarnings("serial")
public class DelegateInjectionPointAssignabilityTest {

    protected AssignabilityRules getRules() {
        return DelegateInjectionPointAssignabilityRules.instance();
    }

    /*
     * the delegate type parameter and the bean type parameter are both type variables and the upper
     * bound of the bean type parameter is assignable to the upper bound, if any, of the delegate type
     * parameter, or
     */
    @Test
    public <A, B, C extends Number, D extends Integer, E extends C, F extends Number & Comparable<Integer>, G extends Double> void testTypeVariableParameterWithTypeVariableParameter() {
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<A>>() {
        }.getType(), new TypeLiteral<List<A>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<A>>() {
        }.getType(), new TypeLiteral<List<B>>() {
        }.getType()));

        Assert.assertTrue(getRules().matches(new TypeLiteral<List<C>>() {
        }.getType(), new TypeLiteral<List<C>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<C>>() {
        }.getType(), new TypeLiteral<List<D>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<E>>() {
        }.getType(), new TypeLiteral<List<D>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<E>>() {
        }.getType(), new TypeLiteral<List<E>>() {
        }.getType()));

        Assert.assertFalse(getRules().matches(new TypeLiteral<List<D>>() {
        }.getType(), new TypeLiteral<List<E>>() {
        }.getType()));
        Assert.assertFalse(getRules().matches(new TypeLiteral<List<D>>() {
        }.getType(), new TypeLiteral<List<B>>() {
        }.getType()));

        Assert.assertTrue(getRules().matches(new TypeLiteral<List<F>>() {
        }.getType(), new TypeLiteral<List<D>>() {
        }.getType()));
        Assert.assertFalse(getRules().matches(new TypeLiteral<List<F>>() {
        }.getType(), new TypeLiteral<List<G>>() {
        }.getType()));
    }

    /*
     * the delegate type parameter is a type variable, the bean type parameter is an actual type, and
     * the actual type is assignable to the upper bound, if any, of the type variable.
     */
    @Test
    public <A, B extends Number, C extends B, D extends Number & Comparable<Integer>> void testTypeVariableParameterWithActualTypeParameter() {
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<A>>() {
        }.getType(), new TypeLiteral<List<Number>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<A>>() {
        }.getType(), new TypeLiteral<List<Object>>() {
        }.getType()));

        Assert.assertTrue(getRules().matches(new TypeLiteral<List<B>>() {
        }.getType(), new TypeLiteral<List<Number>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<B>>() {
        }.getType(), new TypeLiteral<List<Integer>>() {
        }.getType()));
        Assert.assertFalse(getRules().matches(new TypeLiteral<List<B>>() {
        }.getType(), new TypeLiteral<List<String>>() {
        }.getType()));

        Assert.assertTrue(getRules().matches(new TypeLiteral<List<C>>() {
        }.getType(), new TypeLiteral<List<Number>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<C>>() {
        }.getType(), new TypeLiteral<List<Integer>>() {
        }.getType()));
        Assert.assertFalse(getRules().matches(new TypeLiteral<List<C>>() {
        }.getType(), new TypeLiteral<List<String>>() {
        }.getType()));

        Assert.assertFalse(getRules().matches(new TypeLiteral<List<D>>() {
        }.getType(), new TypeLiteral<List<Number>>() {
        }.getType()));
        Assert.assertTrue(getRules().matches(new TypeLiteral<List<D>>() {
        }.getType(), new TypeLiteral<List<Integer>>() {
        }.getType()));
    }
}
