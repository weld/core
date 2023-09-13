/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.collections.Arrays2;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ProxiesTest {

    interface Bar {
    }

    interface Baz extends Bar {
    }

    interface SubBaz extends Baz {
    }

    interface Foo extends Bar {
    }

    interface SubFoo extends Foo, Baz, Serializable {
    }

    interface Oops extends Foo, SubBaz {
    }

    interface Incomplete1 extends Foo, Comparable<String> {
    };

    interface Incomplete2 extends Comparable<String> {
    };

    interface Incomplete3 extends Incomplete2 {
    };

    @Test
    public void testSortInterfacesHierarchy() {
        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.add(Incomplete2.class);
        interfaces.add(Foo.class);
        interfaces.add(SubBaz.class);
        interfaces.add(Bar.class);
        interfaces.add(Baz.class);
        interfaces.add(SubFoo.class);
        interfaces.add(Serializable.class);
        interfaces.add(Oops.class);
        interfaces.add(Incomplete1.class);
        interfaces.add(Incomplete3.class);
        Class<?>[] sorted = Proxies.sortInterfacesHierarchy(interfaces).toArray(new Class<?>[] {});
        assertEquals(10, sorted.length);
        assertBefore(sorted, Oops.class, Foo.class, SubBaz.class, Bar.class, Baz.class);
        assertBefore(sorted, SubFoo.class, Foo.class, Bar.class, Baz.class, Serializable.class);
        assertBefore(sorted, Foo.class, Bar.class);
        assertBefore(sorted, Baz.class, Bar.class);
        assertBefore(sorted, SubBaz.class, Baz.class, Bar.class);
        assertBefore(sorted, Incomplete1.class, Foo.class);
        assertBefore(sorted, Incomplete3.class, Incomplete2.class);
        assertTrue(Arrays2.contains(sorted, Incomplete2.class));
    }

    private void assertBefore(Class<?>[] sorted, Class<?> clazz, Class<?>... classes) {
        int idx1 = getIndex(clazz, sorted);
        for (Class<?> testClass : classes) {
            int idx2 = getIndex(testClass, sorted);
            assertTrue(clazz + ":" + idx1 + " not before " + testClass + ":" + idx2, idx1 < idx2);
        }
    }

    private int getIndex(Class<?> clazz, Class<?>[] sorted) {
        for (int i = 0; i < sorted.length; i++) {
            if (sorted[i].equals(clazz)) {
                return i;
            }
        }
        throw new AssertionError(clazz + " not found");
    }

}
