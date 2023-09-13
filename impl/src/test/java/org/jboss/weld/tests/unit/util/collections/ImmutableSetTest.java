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

import java.util.Set;
import java.util.stream.Stream;

import org.jboss.weld.util.collections.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableSetTest {

    @Test
    public void testBuilder() {
        Set<String> set = ImmutableSet.<String> builder().add("foo").add("bar").add("baz").build();
        Assert.assertEquals(3, set.size());
        Assert.assertTrue(set.contains("foo"));
        Assert.assertTrue(set.contains("bar"));
        Assert.assertTrue(set.contains("baz"));
    }

    @Test
    public void testCollector() {
        Set<String> set = Stream.of("foo", "bar", "baz", "foo", "bar").map((string) -> string + string)
                .collect(ImmutableSet.collector());
        Assert.assertEquals(3, set.size());
        Assert.assertTrue(set.contains("foofoo"));
        Assert.assertTrue(set.contains("barbar"));
        Assert.assertTrue(set.contains("bazbaz"));
    }

    @Test
    public void testAddAll() {
        Set<String> set = ImmutableSet.<String> builder().addAll(new String[] { "foo", "bar", "baz" }).build();
        Assert.assertEquals(3, set.size());
        Assert.assertTrue(set.contains("foo"));
        Assert.assertTrue(set.contains("bar"));
        Assert.assertTrue(set.contains("baz"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNull() {
        String foo = null;
        ImmutableSet.<String> builder().add(foo).build();
    }
}
