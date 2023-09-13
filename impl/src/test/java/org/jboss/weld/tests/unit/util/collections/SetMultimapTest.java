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

import org.jboss.weld.util.collections.SetMultimap;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class SetMultimapTest {

    @Test
    public void testDefaultBehaviour() {

        SetMultimap<String, Integer> setMultimap = SetMultimap.newSetMultimap();

        assertTrue(setMultimap.put("foo", 1));
        assertFalse(setMultimap.put("foo", 1));
        setMultimap.put("foo", 2);
        setMultimap.put("foo", 42);
        setMultimap.put("bar", 42);
        setMultimap.put("baz", 1);

        assertFalse(setMultimap.isEmpty());
        assertEquals(3, setMultimap.size());
        assertTrue(setMultimap.containsKey("foo"));
        assertTrue(setMultimap.containsKey("bar"));

        Set<Integer> fooValues = setMultimap.get("foo");
        assertEquals(3, fooValues.size());
        assertTrue(fooValues.contains(42));

        Set<Integer> bazValues = setMultimap.get("baz");
        assertEquals(1, bazValues.size());
        assertTrue(bazValues.contains(1));

        Set<Integer> barValues = setMultimap.get("bar");
        assertEquals(1, barValues.size());

        setMultimap.putAll("bar", Arrays.asList(1, 5, 6, 7));
        assertEquals(5, barValues.size());
        assertTrue(barValues.containsAll(Arrays.asList(1, 42, 5, 6, 7)));

        Set<Integer> replaced = setMultimap.replaceValues("foo", Collections.singletonList(1000));
        assertEquals(3, replaced.size());
        fooValues = setMultimap.get("foo");
        assertEquals(1, fooValues.size());

        Set<String> keySet = setMultimap.keySet();
        assertEquals(3, keySet.size());

        Set<Integer> uniqueValues = setMultimap.uniqueValues();
        assertEquals(6, uniqueValues.size());

        List<Integer> values = setMultimap.values();
        assertEquals(7, values.size());

        setMultimap.clear();
        assertTrue(setMultimap.isEmpty());
        assertEquals(0, setMultimap.size());
    }

    @Test
    public void testSetMultimapFromMultimap() {
        SetMultimap<String, Integer> setMultimap = SetMultimap.newSetMultimap();
        setMultimap.putAll("foo", Arrays.asList(1, 2, 3, 4));
        assertEquals(4, setMultimap.values().size());
        SetMultimap<String, Integer> copy = SetMultimap.newSetMultimap(setMultimap);
        assertEquals(4, copy.values().size());
        assertTrue(copy.containsKey("foo"));
        setMultimap.clear();
        assertFalse(copy.isEmpty());
    }

}
