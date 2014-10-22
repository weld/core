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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.weld.util.collections.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Common tests for Weld {@link Set} implementations.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 */
public abstract class AbstractImmutableTinySetTest {

    protected abstract String[] getData();

    protected Set<String> dataAsSet() {
        Set<String> result = new LinkedHashSet<>();
        for (String item : getData()) {
            result.add(item);
        }
        return result;
    }

    @Test
    public void testSize() {
        Assert.assertEquals(getData().length, ImmutableSet.of(getData()).size());
    }

    @Test
    public void testContains() {
        Set<String> set = ImmutableSet.of(getData());
        for (String data : getData()) {
            Assert.assertTrue(set.contains(data));
        }
        Assert.assertFalse(set.contains("qux"));
    }

    @Test
    public void testToArray() {
        Object[] array = ImmutableSet.of(getData()).toArray();
        Assert.assertEquals(getData().length, array.length);
        Assert.assertTrue(Arrays.equals(getData(), array));
    }

    @Test
    public void testToArray2() {
        Set<String> set = ImmutableSet.of(getData());
        String[] array = new String[5];
        Arrays.fill(array, "qux");
        array = set.toArray(array);
        Assert.assertEquals(5, array.length);
        for (int i = 0; i < getData().length; i++) {
            Assert.assertEquals(getData()[i], array[i]);
        }
        Assert.assertEquals(null, array[getData().length]);
    }

    @Test
    public void testToArray3() {
        Set<String> set = ImmutableSet.of(getData());
        String[] array = set.toArray(new String[2]);
        Assert.assertEquals(Math.max(2, getData().length), array.length);
        Assert.assertArrayEquals(getData(), Arrays.copyOf(array, getData().length));
    }

    @Test
    public void testIterator() {
        Set<String> set = ImmutableSet.of(getData());
        Iterator<String> iterator = set.iterator();
        for (String data : getData()) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(data, iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(dataAsSet().hashCode(), ImmutableSet.of(getData()).hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(ImmutableSet.of(getData()), dataAsSet());
    }

    @Test
    public void testToString() {
        Assert.assertEquals(dataAsSet().toString(), ImmutableSet.of(getData()).toString());
    }

    @Test
    public void testSerialization() throws ClassNotFoundException, IOException {
        Set<String> set = ImmutableSet.of(getData());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(set);
        @SuppressWarnings("unchecked")
        Set<String> deserializedSet = (Set<String>) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        Assert.assertEquals(set, deserializedSet);
    }
}
