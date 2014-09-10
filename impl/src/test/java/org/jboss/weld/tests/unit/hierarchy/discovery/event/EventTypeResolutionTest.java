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
package org.jboss.weld.tests.unit.hierarchy.discovery.event;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.EventObjectTypeResolverBuilder;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.jboss.weld.util.reflection.TypeResolver;
import org.junit.Test;

@SuppressWarnings("serial")
public class EventTypeResolutionTest {

    public static Type resolveType(Type selectedType, Class<?> eventObjectType) {
        HierarchyDiscovery selectedTypeHierarchy = new HierarchyDiscovery(selectedType);
        HierarchyDiscovery eventTypeHierarchy = HierarchyDiscovery.forNormalizedType(eventObjectType);
        TypeResolver resolver = new EventObjectTypeResolverBuilder(selectedTypeHierarchy.getResolver()
                .getResolvedTypeVariables(), eventTypeHierarchy.getResolver().getResolvedTypeVariables()).build();
        return resolver.resolveType(Types.getCanonicalType(eventObjectType));
    }

    @Test
    public void testWithClasses1() {
        Type selectedType = new TypeLiteral<Alpha<List<String>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<String>>() {
        }.getType(), type);
    }

    @Test
    public void testWithClasses2() {
        Type selectedType = new TypeLiteral<Delta<Comparable<Byte>, Set<Character>>>() {
        }.getType();
        Type type = resolveType(selectedType, Echo.class);
        assertEquals(new TypeLiteral<Echo<Byte, Character>>() {
        }.getType(), type);
    }

    @Test
    public void testWithClasses3() {
        Type selectedType = new TypeLiteral<Alpha<Comparable<List<String>>>>() {
        }.getType();
        Type type = resolveType(selectedType, Foxtrot.class);
        assertEquals(new TypeLiteral<Foxtrot<String>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces1() {
        Type selectedType = new TypeLiteral<Interface1<Set<Integer>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<Integer>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces2() {
        Type selectedType = new TypeLiteral<Interface2<Set<Integer>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<Integer>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces3() {
        Type selectedType = new TypeLiteral<Interface3<Set<Integer>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<Integer>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces4() {
        Type selectedType = new TypeLiteral<Interface4<Set<Integer>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<Integer>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces5() {
        Type selectedType = new TypeLiteral<Interface4<Comparable<Set<Integer>>>>() {
        }.getType();
        Type type = resolveType(selectedType, Foxtrot.class);
        assertEquals(new TypeLiteral<Foxtrot<Integer>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces6() {
        Type selectedType = new TypeLiteral<Interface5<Map<Double, Set<Long>>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(new TypeLiteral<Charlie<Long>>() {
        }.getType(), type);
    }

    @Test
    public void testWithInterfaces6Broken1() {
        Type selectedType = new TypeLiteral<Interface5<Set<Set<Long>>>>() {
        }.getType();
        Type type = resolveType(selectedType, Charlie.class);
        assertEquals(Types.getCanonicalType(Charlie.class), type);
    }
}
