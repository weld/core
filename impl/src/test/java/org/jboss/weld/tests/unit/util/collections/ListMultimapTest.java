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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.weld.util.collections.ListMultimap;
import org.jboss.weld.util.collections.Multimap;
import org.jboss.weld.util.collections.SetMultimap;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ListMultimapTest {

    @Test
    public void testDefaultBehaviour() {

        ListMultimap<String, Integer> listMultimap = new ListMultimap<>();

        assertTrue(listMultimap.put("foo", 1));
        assertTrue(listMultimap.put("foo", 1));
        listMultimap.put("foo", 2);
        listMultimap.put("foo", 42);
        listMultimap.put("bar", 42);
        listMultimap.put("baz", 1);

        assertFalse(listMultimap.isEmpty());
        assertEquals(3, listMultimap.size());
        assertTrue(listMultimap.containsKey("foo"));
        assertTrue(listMultimap.containsKey("bar"));

        List<Integer> fooValues = listMultimap.get("foo");
        assertEquals(4, fooValues.size());
        assertTrue(fooValues.contains(42));

        List<Integer> bazValues = listMultimap.get("baz");
        assertEquals(1, bazValues.size());
        assertTrue(bazValues.contains(1));

        List<Integer> barValues = listMultimap.get("bar");
        assertEquals(1, barValues.size());

        listMultimap.putAll("bar", Arrays.asList(42, 1, 5, 6, 7));
        assertEquals(6, barValues.size());
        assertTrue(barValues.containsAll(Arrays.asList(1, 42, 5, 6, 7)));

        List<Integer> replaced = listMultimap.replaceValues("foo", Collections.singletonList(1000));
        assertEquals(4, replaced.size());
        fooValues = listMultimap.get("foo");
        assertEquals(1, fooValues.size());

        Set<String> keySet = listMultimap.keySet();
        assertEquals(3, keySet.size());

        Set<Integer> uniqueValues = listMultimap.uniqueValues();
        assertEquals(6, uniqueValues.size());

        List<Integer> values = listMultimap.values();
        assertEquals(8, values.size());

        listMultimap.clear();
        assertTrue(listMultimap.isEmpty());
        assertEquals(0, listMultimap.size());
    }

    @Test
    public void testMultimapFromMultimap() {
        Multimap<String, Integer> multimap = SetMultimap.newSetMultimap();
        multimap.putAll("foo", Arrays.asList(1, 2, 3, 4));
        assertEquals(4, multimap.values().size());
        ListMultimap<String, Integer> copy = new ListMultimap<>(multimap);
        assertEquals(4, copy.values().size());
        assertTrue(copy.containsKey("foo"));
        multimap.clear();
        assertFalse(copy.isEmpty());
    }

}
