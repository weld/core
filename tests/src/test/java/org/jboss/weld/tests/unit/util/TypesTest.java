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
package org.jboss.weld.tests.unit.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.util.Types;
import org.testng.annotations.Test;

public class TypesTest {

    private static final String GENERIC_ARRAY_TYPE_ID = "java.util.List<java.util.List<java.lang.Integer>[]>";

    @SuppressWarnings("serial")
    @Test
    public <T> void testIsIllegalBeanType() {
        assertFalse(Types.isIllegalBeanType(new TypeLiteral<List<String>>() {
        }.getType()));
        assertTrue(Types.isIllegalBeanType(new TypeLiteral<List<?>>() {
        }.getType()));
        assertTrue(Types.isIllegalBeanType(new TypeLiteral<List<Set<?>>>() {
        }.getType()));
        assertFalse(Types.isIllegalBeanType(new TypeLiteral<List<Set<T>>>() {
        }.getType()));
        assertFalse(Types.isIllegalBeanType(new TypeLiteral<Map<Integer, T>>() {
        }.getType()));
        assertTrue(Types.isIllegalBeanType(new TypeLiteral<Map<Integer, ?>>() {
        }.getType()));
        assertTrue(Types.isIllegalBeanType(new TypeLiteral<List<?>[]>() {
        }.getType()));
        assertFalse(Types.isIllegalBeanType(new TypeLiteral<Instance<Integer>[]>() {
        }.getType()));
    }

    @SuppressWarnings("serial")
    @Test
    public void testIsMoreSpecific() {
        assertTrue(Types.isMoreSpecific(ArrayList.class, List.class));
        assertFalse(Types.isMoreSpecific(ArrayList.class, ArrayList.class));
        assertTrue(Types.isMoreSpecific(ArrayList.class, Collection.class));
        assertTrue(Types.isMoreSpecific(Integer.class, Number.class));
        assertFalse(Types.isMoreSpecific(Number.class, Integer.class));
        assertTrue(Types.isMoreSpecific(new TypeLiteral<List<Integer>>() {
        }.getType(), new TypeLiteral<Collection<Integer>>() {
        }.getType()));
    }

    @Test
    public void testTypeId() throws NoSuchFieldException {
        assertEquals(Types.getTypeId(Foo.class.getField("lists").getGenericType()), GENERIC_ARRAY_TYPE_ID);
    }

}
