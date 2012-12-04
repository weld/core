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
package org.jboss.weld.tests.unit.hierarchy.discovery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jboss.weld.util.reflection.ParameterizedTypeImpl;

public class Types {

    private Types() {
    }

    public static ParameterizedType newParameterizedType(Class<?> rawType, Type... actualTypeArguments) {
        return new ParameterizedTypeImpl(rawType, actualTypeArguments, null);
    }

    public static void assertTypeSetMatches(Set<Type> expected, Set<Type> actual) {
        assertEquals(expected.size(), actual.size());
        Set<Type> mutableExpected = new HashSet<Type>(expected);
        for (Iterator<Type> iterator = mutableExpected.iterator(); iterator.hasNext();) {
            Type type = iterator.next();
            assertTrue("Expected entry " + type + " not found within " + actual, actual.contains(type));
            iterator.remove();
        }
        assertTrue("The following expected entries were not found " + mutableExpected, mutableExpected.isEmpty());
    }
}
