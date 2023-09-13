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
import java.util.ListIterator;

import org.junit.Assert;
import org.junit.Test;

public class ImmutableArrayListTest extends AbstractImmutableListTest {

    private static final String[] DATA = new String[] { "alpha", "bravo", "charlie", "delta", "echo" };

    @Override
    protected String[] getData() {
        return DATA;
    }

    @Test
    public void testIterator2() {
        List<String> list = getCollection();
        ListIterator<String> iterator = list.listIterator(2);
        Assert.assertTrue(iterator.hasPrevious());
        int i = 2;
        for (; i < getData().length;) {
            Assert.assertTrue(iterator.hasNext());
            Assert.assertEquals(getData()[i], iterator.next());
            i++;
            Assert.assertEquals(i, iterator.nextIndex());
            Assert.assertEquals(i - 1, iterator.previousIndex());
        }
        Assert.assertFalse(iterator.hasNext());
        Assert.assertTrue(iterator.hasPrevious());
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
}
