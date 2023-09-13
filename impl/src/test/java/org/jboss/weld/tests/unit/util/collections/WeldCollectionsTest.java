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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jboss.weld.util.collections.WeldCollections;
import org.junit.Test;

/**
 * @author Martin Kouba
 */
public class WeldCollectionsTest {

    @Test
    public void testToMultiRowString() {
        assertEquals("\n  - Foo,\n  - Bar,\n  - Baz", WeldCollections.toMultiRowString(Arrays.asList("Foo", "Bar", "Baz")));
    }

    @Test
    public void testSorting() {
        List<String> list = new ArrayList<>();
        list.add("ab");
        list.add("a");
        list.add("abc");
        List<String> sortedList = WeldCollections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() != o2.length() ? (o1.length() < o2.length() ? -1 : 1) : 0;
            }
        });
        assertEquals(list.size(), sortedList.size());
        assertEquals(sortedList.get(0), "a");
        assertEquals(sortedList.get(1), "ab");
        assertEquals(sortedList.get(2), "abc");

    }

}
