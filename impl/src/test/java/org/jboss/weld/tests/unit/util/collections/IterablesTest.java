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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.Iterables;
import org.junit.Test;

public class IterablesTest {

    @Test
    public void testAddAll() {
        List<String> target = new ArrayList<String>();
        List<String> toAdd = new ArrayList<String>();
        toAdd.add("foo");
        toAdd.add("baz");
        Iterables.addAll(target, toAdd);
        assertTrue(target.contains(toAdd.get(0)));
        assertTrue(target.contains(toAdd.get(1)));
    }

    @Test
    public void testConcat() {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        list1.add("bla");
        list2.add("foo");
        list2.add("baz");

        Iterable<String> iterable = Iterables.concat(ImmutableList.of(list2, list1));

        Iterator<String> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(list2.get(0), iterator.next());
        assertEquals(list2.get(1), iterator.next());
        assertEquals(list1.get(0), iterator.next());
        assertFalse(iterator.hasNext());

        iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(list2.get(0), iterator.next());
        assertEquals(list2.get(1), iterator.next());
        iterator.remove();
        assertEquals(list1.get(0), iterator.next());
        assertFalse(iterator.hasNext());

        assertEquals(1, list2.size());
        assertEquals(1, list1.size());
    }

    @Test
    public void testIterators() {
        List<String> list1 = new ArrayList<String>();
        List<String> list2 = new ArrayList<String>();
        list1.add("bla");
        list2.add("foo");
        list2.add("baz");
        Iterator<Iterator<? extends String>> iterablesIterator = Iterables.iterators(ImmutableList.of(list2, list1));
        assertTrue(iterablesIterator.hasNext());
        Iterator<? extends String> iterator = iterablesIterator.next();
        assertEquals(list2.get(0), iterator.next());
        assertEquals(list2.get(1), iterator.next());
        assertFalse(iterator.hasNext());
        iterator = iterablesIterator.next();
        assertEquals(list1.get(0), iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterablesIterator.hasNext());
    }

    @Test
    public void testToMultiRowString() {
        assertEquals("\n  - Foo,\n  - Bar,\n  - Baz", Iterables.toMultiRowString(Arrays.asList("Foo", "Bar", "Baz")));
    }

}
