/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.util.Beans;
import org.testng.annotations.Test;

public class BeansTest {

    private static final String SIGNATURE = "[java.lang.Object,java.lang.Object,java.util.List<java.lang.String>,java.util.Map<java.lang.Integer,java.lang.String>,java.util.Map<java.lang.Integer,java.lang.String>,javax.enterprise.inject.Instance<java.lang.Integer>]";

    @SuppressWarnings("serial")
    @Test
    public void testTypeCollectionSignature() {
        Collection<Type> types = new ArrayList<Type>();
        types.add(Object.class);
        types.add(new TypeLiteral<List<String>>() {
        }.getType());
        types.add(new TypeLiteral<Map<Integer, String>>() {
        }.getType());
        types.add(new TypeLiteral<Map<Integer, String>>() {
        }.getType());
        types.add(new TypeLiteral<Instance<Integer>[]>() {
        }.getType());
        types.add(Object.class);
        assertEquals(SIGNATURE, Beans.createTypeCollectionId(types));
    }

    @Test
    public void testDeclaredBeanType() {
        assertEquals(Beans.getDeclaredBeanType(Foo.class).getTypeName(), String.class.getSimpleName());
    }

}
