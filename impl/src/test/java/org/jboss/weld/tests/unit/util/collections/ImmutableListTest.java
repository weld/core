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

import java.util.List;
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
}
