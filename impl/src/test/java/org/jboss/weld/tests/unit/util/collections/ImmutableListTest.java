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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import org.jboss.weld.util.collections.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableListTest {

    @Test
    public void testBuilder() {
        List<String> list = ImmutableList.<String> builder().add("foo").add("bar").add("baz").build();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("foo", list.get(0));
        Assert.assertEquals("bar", list.get(1));
        Assert.assertEquals("baz", list.get(2));
    }

    @Test
    public void testCollector() {
        List<String> list = Stream.of("foo", "bar", "baz").map((string) -> string + string).collect(ImmutableList.collector());
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("foofoo", list.get(0));
        Assert.assertEquals("barbar", list.get(1));
        Assert.assertEquals("bazbaz", list.get(2));
    }

    @Test
    public void testAddAll() {
        List<String> list = ImmutableList.<String> builder().addAll(new String[] { "foo", "bar", "baz" }).build();
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("foo", list.get(0));
        Assert.assertEquals("bar", list.get(1));
        Assert.assertEquals("baz", list.get(2));

        List<String> list2 = ImmutableList.<String> builder().addAll(list).build();
        Assert.assertEquals(list, list2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNull() {
        String foo = null;
        ImmutableList.<String> builder().add(foo).build();
    }

    @Test
    public void testCopy() {
        List<String> list = ImmutableList.copyOf(new String[] { "foo", "bar", "baz" });
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("foo", list.get(0));
        Assert.assertEquals("bar", list.get(1));
        Assert.assertEquals("baz", list.get(2));

        List<String> list2 = ImmutableList.copyOf(list);
        Assert.assertEquals(list, list2);
    }

    @Test
    public void testListIterator() {
        List<String> list = ImmutableList.of("foo", "bar", "buu");
        List<String> expected = new ArrayList<>(list);
        Collections.reverse(expected);
        List<String> result = new ArrayList<>();
        for (ListIterator<String> iterator = list.listIterator(list.size()); iterator.hasPrevious();) {
            result.add(iterator.previous());
        }
        assertEquals(expected, result);
    }
}
