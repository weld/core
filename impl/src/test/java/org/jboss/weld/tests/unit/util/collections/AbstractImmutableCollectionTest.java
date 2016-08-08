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
import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractImmutableCollectionTest<C extends Collection<String>> {

    protected abstract String[] getData();

    protected abstract C getCollection();

    protected abstract C getDefaultCollection();

    @Test
    public void testSize() {
        Assert.assertEquals(getData().length, getCollection().size());
    }

    @Test
    public void testContains() {
        C collection = getCollection();
        for (String data : getData()) {
            Assert.assertTrue(collection.contains(data));
        }
        Assert.assertFalse(collection.contains("qux"));
    }

    @Test
    public void testToArray() {
        Object[] array = getCollection().toArray();
        Assert.assertEquals(getData().length, array.length);
        Assert.assertTrue(Arrays.equals(getData(), array));
    }

    @Test
    public void testToArray2() {
        C collection = getCollection();
        String[] array = new String[10];
        Arrays.fill(array, "qux");
        array = collection.toArray(array);
        Assert.assertEquals(10, array.length);
        for (int i = 0; i < getData().length; i++) {
            Assert.assertEquals(getData()[i], array[i]);
        }
        Assert.assertEquals(null, array[getData().length]);
    }

    @Test
    public void testToArray3() {
        C collection = getCollection();
        String[] array = collection.toArray(new String[2]);
        Assert.assertEquals(Math.max(2, getData().length), array.length);
        Assert.assertArrayEquals(getData(), Arrays.copyOf(array, getData().length));
        Assert.assertEquals(String.class, array.getClass().getComponentType());
    }

    @Test
    public void testIterator() {
        C collection = getCollection();
        Iterator<String> iterator = collection.iterator();
        for (String data : getData()) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(data, iterator.next());
        }
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(getDefaultCollection().hashCode(), getCollection().hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(getCollection(), getDefaultCollection());
        Assert.assertEquals(getDefaultCollection(), getCollection());
    }

    @Test
    public void testToString() {
        Assert.assertEquals(getDefaultCollection().toString(), getCollection().toString());
    }

    @Test
    public void testSerialization() throws ClassNotFoundException, IOException {
        C collection = getCollection();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new ObjectOutputStream(bytes).writeObject(collection);
        @SuppressWarnings("unchecked")
        C deserializedCollection = (C) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
        Assert.assertEquals(collection, deserializedCollection);
    }
}
