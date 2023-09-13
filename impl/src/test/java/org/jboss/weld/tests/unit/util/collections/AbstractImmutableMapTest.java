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

import java.util.Map;

import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Common tests for Weld's Map implementations
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractImmutableMapTest {

    private final Object[] keys;
    private final Object[] values;

    private final Map<?, ?> immutableMap;

    protected AbstractImmutableMapTest(Object[] keys, Object[] values) {
        Preconditions.checkArgument(keys.length == values.length, values);
        this.keys = keys;
        this.values = values;
        final ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
        for (int i = 0; i < keys.length; i++) {
            builder.put(keys[i], values[i]);
        }
        this.immutableMap = builder.build();
    }

    @Test
    public void testSize() {
        Assert.assertEquals(keys.length, immutableMap.size());
    }

    @Test
    public void testContains() {
        for (Object key : keys) {
            Assert.assertTrue(immutableMap.containsKey(key));
        }
        Assert.assertFalse(immutableMap.containsKey(AbstractImmutableMapTest.class.getName()));
    }

    @Test
    public void testGet() {
        for (int i = 0; i < keys.length; i++) {
            Assert.assertEquals(values[i], immutableMap.get(keys[i]));
        }
        Assert.assertNull(immutableMap.get(AbstractImmutableMapTest.class.getName()));
    }
}
