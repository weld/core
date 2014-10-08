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
package org.jboss.weld.tests.unit.util.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.weld.util.collections.Sets;
import org.junit.Test;

public class SetsTest {

    @Test
    public void testNewHashSet() {
        Set<String> set = Sets.newHashSet("foo", "bar");
        assertEquals(2, set.size());
        assertTrue(set.contains("foo"));
        assertTrue(set.contains("bar"));
    }

    @Test
    public void testUnion() {

        Set<String> set1 = new LinkedHashSet<>();
        Set<String> set2 = new LinkedHashSet<>();

        set1.add("foo");
        set1.add("bar");
        set1.add("baz");

        set2.add("bar");
        set2.add("qux");

        Set<String> union = Sets.union(set1, set2);

        assertEquals(4, union.size());
        assertFalse(union.isEmpty());
        assertTrue(union.contains("baz"));
        assertTrue(union.contains("bar"));
        assertTrue(union.contains("qux"));
        Iterator<String> iterator = union.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("foo", iterator.next());
        assertEquals("bar", iterator.next());
        assertEquals("baz", iterator.next());
        assertEquals("qux", iterator.next());
        try {
            iterator.remove();
            fail();
        } catch (UnsupportedOperationException e) {
            // Expected
        }
        assertFalse(iterator.hasNext());

        assertTrue(Sets.union(new HashSet<>(), new HashSet<>()).isEmpty());
    }

}
