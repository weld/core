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
package org.jboss.weld.tests.unit.hierarchy.discovery.classes;

import static org.jboss.weld.tests.unit.hierarchy.discovery.Types.newParameterizedType;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.tests.unit.hierarchy.discovery.Types;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Test;

public class ClassHierarchyTest {

    @Test
    public void testTypesResolved() {
        Set<Type> expectedTypes = new HashSet<Type>();
        expectedTypes.add(Object.class);
        expectedTypes.add(Baz.class);
        expectedTypes.add(newParameterizedType(Bar.class, Integer.class));
        expectedTypes.add(newParameterizedType(Foo.class, Integer.class));

        HierarchyDiscovery discovery = HierarchyDiscovery.forNormalizedType(Baz.class);
        Types.assertTypeSetMatches(expectedTypes, discovery.getTypeClosure());
    }

    @Test
    public void testMassiveParameterizedType() {
        Set<Type> expectedTypes = new HashSet<Type>();
        expectedTypes.add(Object.class);
        expectedTypes.add(Bravo.class);
        expectedTypes.add(t(
                Alpha.class,
                t(Alpha.class,
                        t(Alpha.class,
                                t(Map.class, t(Alpha.class, String.class),
                                        t(List.class, t(Set.class, t(Comparable.class, Serializable.class))))))));
        HierarchyDiscovery discovery = HierarchyDiscovery.forNormalizedType(Bravo.class);
        Types.assertTypeSetMatches(expectedTypes, discovery.getTypeClosure());
    }

    private Type t(Class<?> rawType, Type... arguments) {
        return newParameterizedType(rawType, arguments);
    }
}
