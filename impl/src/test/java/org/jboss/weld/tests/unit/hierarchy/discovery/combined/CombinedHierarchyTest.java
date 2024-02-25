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
package org.jboss.weld.tests.unit.hierarchy.discovery.combined;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import org.jboss.weld.tests.unit.hierarchy.discovery.Types;
import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Test;

public class CombinedHierarchyTest {

    @Test
    public void testInterfaceTypesResolved() {
        Set<Type> expectedTypes = new HashSet<Type>();
        expectedTypes.add(Object.class);
        expectedTypes.add(Serializable.class);
        expectedTypes.add(RandomAccess.class);
        expectedTypes.add(Cloneable.class);
        expectedTypes.add(Types.newParameterizedType(AbstractCollection.class, Integer.class));
        expectedTypes.add(Types.newParameterizedType(Collection.class, Integer.class));
        expectedTypes.add(Types.newParameterizedType(AbstractList.class, Integer.class));
        expectedTypes.add(Types.newParameterizedType(List.class, Integer.class));
        expectedTypes.add(Types.newParameterizedType(ArrayList.class, Integer.class));
        expectedTypes.add(Types.newParameterizedType(Iterable.class, Integer.class));

        // JDK 21 adds interface java.util.SequencedCollection<class java.lang.Integer>
        try {
            Class<?> seqCollectionClazz = Class.forName("java.util.SequencedCollection");
            expectedTypes.add(Types.newParameterizedType(seqCollectionClazz, Integer.class));
        } catch (ClassNotFoundException e) {
            // this just means we are running on older JDK
        }
        HierarchyDiscovery discovery = new HierarchyDiscovery(Types.newParameterizedType(ArrayList.class, Integer.class));
        Types.assertTypeSetMatches(expectedTypes, discovery.getTypeClosure());
    }
}
