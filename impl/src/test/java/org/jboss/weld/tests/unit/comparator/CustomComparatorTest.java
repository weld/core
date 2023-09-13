/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.unit.comparator;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.SimpleCDI;
import org.jboss.weld.util.CustomClassComparator;
import org.junit.Assert;
import org.junit.Test;

import foo.bar.Foo;
import foo.bar.quax.Quax;

/**
 * Tests sorting collections with {@link CustomClassComparator}.
 */
public class CustomComparatorTest {

    @Test
    public void testComparator() {
        List<Class<?>> testedList = prepareList(new ArrayList<>(), false);
        List<Class<?>> expectedList = prepareList(new ArrayList<>(), true);
        // sort the list
        Collections.sort(testedList, new CustomClassComparator());
        // assert
        assertListsEqual(expectedList, testedList);
    }

    private List<Class<?>> prepareList(List<Class<?>> list, boolean sorted) {
        Class<?> clazz1 = Principal.class;
        Class<?> clazz2 = CustomClassComparator.class;
        Class<?> clazz3 = CustomComparatorTest.class;
        Class<?> clazz4 = Class.class;
        Class<?> clazz5 = SimpleCDI.class;
        Class<?> clazz6 = Foo.class;
        Class<?> clazz7 = Quax.class;

        if (sorted) {
            list.add(clazz6);
            list.add(clazz7);
            list.add(clazz5);
            list.add(clazz3);
            list.add(clazz2);
            list.add(clazz4);
            list.add(clazz1);
        } else {
            list.add(clazz1);
            list.add(clazz2);
            list.add(clazz3);
            list.add(clazz4);
            list.add(clazz5);
            list.add(clazz6);
            list.add(clazz7);
        }
        return list;
    }

    // compare based on class names as class itself doesn't implement equals
    private void assertListsEqual(List<Class<?>> expected, List<Class<?>> actual) {
        Assert.assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(expected.get(i).getName(), actual.get(i).getName());
        }
    }
}
