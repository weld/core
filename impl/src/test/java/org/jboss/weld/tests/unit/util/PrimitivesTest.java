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
package org.jboss.weld.tests.unit.util;

import static org.junit.Assert.assertEquals;

import org.jboss.weld.util.Primitives;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class PrimitivesTest {

    @Test
    public void testWrap() {
        assertEquals(String.class, Primitives.wrap(String.class));
        assertEquals(Integer.class, Primitives.wrap(Integer.class));
        assertEquals(Boolean.class, Primitives.wrap(boolean.class));
        assertEquals(Character.class, Primitives.wrap(char.class));
        assertEquals(Short.class, Primitives.wrap(short.class));
        assertEquals(Integer.class, Primitives.wrap(int.class));
        assertEquals(Long.class, Primitives.wrap(long.class));
        assertEquals(Double.class, Primitives.wrap(double.class));
        assertEquals(Float.class, Primitives.wrap(float.class));
        assertEquals(Byte.class, Primitives.wrap(byte.class));
    }

    @Test
    public void testUnwrap() {
        assertEquals(String.class, Primitives.unwrap(String.class));
        assertEquals(boolean.class, Primitives.unwrap(Boolean.class));
        assertEquals(char.class, Primitives.unwrap(Character.class));
        assertEquals(short.class, Primitives.unwrap(Short.class));
        assertEquals(int.class, Primitives.unwrap(Integer.class));
        assertEquals(long.class, Primitives.unwrap(Long.class));
        assertEquals(double.class, Primitives.unwrap(Double.class));
        assertEquals(float.class, Primitives.unwrap(Float.class));
        assertEquals(byte.class, Primitives.unwrap(Byte.class));
    }

}
