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

import org.jboss.weld.probe.Json.JsonArrayBuilder;
import org.jboss.weld.probe.Json.JsonObjectBuilder;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class JsonTest {

    @Test
    public void testJsonArrayBuilder() {
        assertEquals("[\"foo\",\"bar\",[\"baz\"]]", Json.arrayBuilder().add("foo").add("bar").add(Json.arrayBuilder().add("baz")).build());
    }

    @Test
    public void testJsonObjectBuilder() {
        assertEquals("{\"foo\":\"bar\",\"baz\":[\"qux\"]}", Json.objectBuilder().add("foo", "bar").add("baz", Json.arrayBuilder().add("qux")).build());
    }

    @Test
    public void testIgnoreEmptyBuilders() {
        assertEquals("[true]", Json.arrayBuilder(true).add(true).add(Json.objectBuilder(true).add("foo", Json.objectBuilder())).build());

        JsonObjectBuilder objectBuilder = Json.objectBuilder();
        JsonArrayBuilder arrayBuilder = Json.arrayBuilder().add(objectBuilder);
        objectBuilder.add("foo", "bar");
        assertEquals("[{\"foo\":\"bar\"}]", arrayBuilder.build());
    }

    @Test
    public void testABitMoreComplexStructure() {
        JsonObjectBuilder builder = Json.objectBuilder().add("items", Json.arrayBuilder().add(1).add(2)).add("name", "Foo").add("parent",
                Json.objectBuilder(true).add("name", "Martin").add("age", 100).add("active", true).add("children",
                        Json.arrayBuilder(true).add(Json.objectBuilder())));
        assertFalse(builder.isEmpty());
        assertEquals("{\"items\":[1,2],\"name\":\"Foo\",\"parent\":{\"name\":\"Martin\",\"age\":100,\"active\":true}}", builder.build());
    }

    @Test
    public void testEscaping() {
        assertEquals("{\"foo\":\"bar=\\\"baz\\u000a and \\u0009 F\\\"\"}", Json.objectBuilder().add("foo", "bar=\"baz\n and \t F\"").build());
    }

}
