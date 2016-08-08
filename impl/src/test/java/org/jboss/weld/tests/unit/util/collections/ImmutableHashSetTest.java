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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.util.collections.ImmutableHashSet;
import org.jboss.weld.util.collections.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for Weld's {@link ImmutableHashSet}.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 */
public class ImmutableHashSetTest extends AbstractImmutableSetTest {

    private static final String[] DATA = new String[] { "alpha", "bravo", "charlie", "delta", "echo" };

    @Override
    protected String[] getData() {
        return DATA;
    }

    @Test
    @Override
    public void testToArray() {
        Object[] array = ImmutableSet.of(getData()).toArray();
        Arrays.sort(array);
        Assert.assertEquals(getData().length, array.length);
        Assert.assertTrue(Arrays.equals(getData(), array));
    }

    @Test
    @Override
    public void testToArray2() {
        Set<String> set = ImmutableSet.of(getData());
        String[] array = new String[10];
        Arrays.fill(array, "qux");
        array = set.toArray(array);
        Assert.assertEquals(10, array.length);
        for (int i = 0; i < getData().length; i++) {
            Assert.assertNotNull(array[i]);
        }
        Assert.assertNull(array[getData().length]);
    }

    @Test
    @Override
    public void testToArray3() {
        Set<String> set = ImmutableSet.of(getData());
        String[] array = set.toArray(new String[2]);
        Assert.assertEquals(getData().length, array.length);
    }

    @Test
    @Override
    public void testIterator() {
        Set<String> set = ImmutableSet.of(getData());
        Iterator<String> iterator = set.iterator();
        for (int i = 0; i < getData().length; i++) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertNotNull(iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testToString() {
        String string = ImmutableSet.of(getData()).toString();
        for (String data : getData()) {
            Assert.assertTrue(string.contains(data));
        }
    }

}
