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
import static org.junit.Assert.fail;

import java.util.Map;

import org.jboss.weld.exceptions.IllegalArgumentException;
import org.junit.Test;

public class QueriesTest {

    @Test
    public void testParseFilters() {
        Map<String, String> result = Queries.Filters.parseFilters("foo:\"bar\"");
        assertEquals(1, result.size());
        assertEquals("bar", result.get("foo"));
        result = Queries.Filters
                .parseFilters("foo:\"bar\" and:\"this\" or:\"1 2 3\" colonInValue:\"o: b:\" quotationMarkInValue:\"And now the \"quotation marks\"\"");
        assertEquals(5, result.size());
        assertEquals("bar", result.get("foo"));
        assertEquals("this", result.get("and"));
        assertEquals("1 2 3", result.get("or"));
        assertEquals("o: b:", result.get("colonInValue"));
        assertEquals("And now the \"quotation marks\"", result.get("quotationMarkInValue"));
        try {
            result = Queries.Filters.parseFilters("foo:\"bar");
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

}
