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
package org.jboss.weld.probe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class ResourceTest {

    @Test
    public void testResourceDefinitions() {
        assertEquals(1, Resource.BEANS.getParts().length);
        assertEquals(2, Resource.BEAN.getParts().length);
    }

    @Test
    public void testResourceMatches() {
        assertTrue(Resource.CLIENT_RESOURCE.matches(new String[] { "client", "simple.css" }));
        assertFalse(Resource.CLIENT_RESOURCE.matches(new String[] { "client", "simple" }));
        assertTrue(Resource.BEANS.matches(new String[] { "beans" }));
        assertFalse(Resource.BEANS.matches(new String[] { "beans", "foo", "instance" }));
    }

    @Test
    public void testSplitPath() {
        assertTrue(Arrays.equals(new String[] {}, Resource.splitPath("/")));
        assertTrue(Arrays.equals(new String[] { "client", "probe.css" }, Resource.splitPath("/client/probe.css")));
        assertTrue(Arrays.equals(new String[] { "bean", "12", "instance" }, Resource.splitPath("/bean/12/instance")));
    }

}
