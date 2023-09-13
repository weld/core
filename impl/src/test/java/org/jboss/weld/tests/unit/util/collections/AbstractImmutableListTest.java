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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.jboss.weld.util.collections.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

/**
 * Common tests for Weld {@link List} implementations.
 *
 * @author Jozef Hartinger
 * @see WELD-1753
 *
 */
public abstract class AbstractImmutableListTest extends AbstractImmutableCollectionTest<List<String>> {

    @Override
    protected List<String> getCollection() {
        return ImmutableList.of(getData());
    }

    @Override
    protected List<String> getDefaultCollection() {
        List<String> result = new ArrayList<>();
        Collections.addAll(result, getData());
        return result;
    }

    @Test
    @Override
    public void testIterator() {
        List<String> list = getCollection();
        ListIterator<String> iterator = list.listIterator();
        Assert.assertFalse(iterator.hasPrevious());
        int i = 0;
        for (String data : getData()) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(data, iterator.next());
            i++;
            Assert.assertEquals(i, iterator.nextIndex());
            Assert.assertEquals(i - 1, iterator.previousIndex());
        }
        Assert.assertFalse(iterator.hasNext());
        Assert.assertEquals(!list.isEmpty(), iterator.hasPrevious()); // empty lists do not have previous value
        Assert.assertEquals(list.size(), iterator.nextIndex());
        Assert.assertEquals(list.size() - 1, iterator.previousIndex());
        Assert.assertEquals(list.size(), i);
        while (iterator.hasPrevious()) {
            Assert.assertTrue(iterator.hasPrevious());
            i--;
            Assert.assertEquals(getData()[i], iterator.previous());
            Assert.assertEquals(i, iterator.nextIndex());
            Assert.assertEquals(i - 1, iterator.previousIndex());
        }
        Assert.assertFalse(iterator.hasPrevious());
        Assert.assertEquals(0, i);
        Assert.assertEquals(0, iterator.nextIndex());
        Assert.assertEquals(-1, iterator.previousIndex());
    }

    @Test
    public void testGet() {
        List<String> list = getCollection();
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(getData()[i], list.get(i));
        }
    }

    @Test
    public void testIndexOf() {
        List<String> list = getCollection();
        for (int i = 0; i < list.size(); i++) {
            Assert.assertEquals(i, list.indexOf(getData()[i]));
            Assert.assertEquals(i, list.lastIndexOf(getData()[i]));
        }
        Assert.assertEquals(-1, list.indexOf("qux"));
        Assert.assertEquals(-1, list.lastIndexOf("qux"));
    }

    @Test
    public void testSubList() {
        List<String> subList = getCollection().subList(1, getData().length);
        for (int i = 1; i < getData().length; i++) {
            Assert.assertEquals(getData()[i], subList.get(i - 1));
        }
    }
}
