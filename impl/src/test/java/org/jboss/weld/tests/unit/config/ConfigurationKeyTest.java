/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jboss.weld.config.ConfigurationKey;
import org.junit.Test;

public class ConfigurationKeyTest {

    @Test
    public void testFromString() {
        for (ConfigurationKey key : ConfigurationKey.values()) {
            assertEquals(key, ConfigurationKey.fromString(key.get()));
        }
    }

    @Test
    public void testIsValueTypeSupported() {
        assertTrue(ConfigurationKey.isValueTypeSupported(Integer.class));
        assertTrue(ConfigurationKey.isValueTypeSupported(Long.class));
        assertTrue(ConfigurationKey.isValueTypeSupported(String.class));
        assertTrue(ConfigurationKey.isValueTypeSupported(Boolean.class));
        assertFalse(ConfigurationKey.isValueTypeSupported(List.class));
    }

    @Test
    public void testConvertValue() {
        assertEquals(Boolean.TRUE, ConfigurationKey.CONCURRENT_DEPLOYMENT.convertValue("true"));
        assertEquals(Integer.valueOf(10), ConfigurationKey.PRELOADER_THREAD_POOL_SIZE.convertValue("10"));
        assertEquals(Long.valueOf(10), ConfigurationKey.EXECUTOR_THREAD_POOL_KEEP_ALIVE_TIME.convertValue("10"));
        assertEquals("foo", ConfigurationKey.PROXY_DUMP.convertValue("foo"));
    }

    @Test
    public void testIsValidValue() {
        assertFalse(ConfigurationKey.CONCURRENT_DEPLOYMENT.isValidValue(10));
        assertTrue(ConfigurationKey.CONCURRENT_DEPLOYMENT.isValidValue(false));
    }

    @Test
    public void testIsValidValueType() {
        assertFalse(ConfigurationKey.CONCURRENT_DEPLOYMENT.isValidValueType(Integer.class));
        assertTrue(ConfigurationKey.CONCURRENT_DEPLOYMENT.isValidValueType(Boolean.class));
    }

}
