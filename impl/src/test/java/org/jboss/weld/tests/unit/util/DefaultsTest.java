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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jboss.weld.util.Defaults;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class DefaultsTest {

    @Test
    public void testJslDefaultValues() {
        assertNull(Defaults.getJslDefaultValue(String.class));
        assertNull(Defaults.getJslDefaultValue(Long.class));
        assertFalse(Defaults.getJslDefaultValue(boolean.class));
        assertTrue(Defaults.getJslDefaultValue(long.class) == 0L);
        assertTrue(Defaults.getJslDefaultValue(double.class) == 0d);
        assertTrue(Defaults.getJslDefaultValue(int.class) == 0);
        assertTrue(Defaults.getJslDefaultValue(float.class) == 0f);
        assertTrue(Defaults.getJslDefaultValue(short.class) == (short) 0);
        assertTrue(Defaults.getJslDefaultValue(byte.class) == (byte) 0);
        assertTrue(Defaults.getJslDefaultValue(char.class) == '\u0000');
    }

}
