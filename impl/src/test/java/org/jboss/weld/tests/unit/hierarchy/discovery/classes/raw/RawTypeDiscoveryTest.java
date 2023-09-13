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
package org.jboss.weld.tests.unit.hierarchy.discovery.classes.raw;

import java.lang.reflect.Type;
import java.util.Map;

import org.jboss.weld.util.reflection.HierarchyDiscovery;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testcase for WELD-1740
 *
 * @author Jozef Hartinger
 *
 */
public class RawTypeDiscoveryTest {

    @Test
    public void test() {
        Map<Class<?>, Type> map = new HierarchyDiscovery(Qux.class).getTypeMap();
        Assert.assertEquals(5, map.size());
        Assert.assertEquals(Qux.class, map.get(Qux.class));
        Assert.assertEquals(Baz.class, map.get(Baz.class));
        /*
         * The superclasses (respectively, superinterfaces) of a raw type are
         * the erasures of the superclasses (superinterfaces) of any of its parameterized invocations.
         */
        Assert.assertEquals(Foo.class, map.get(Foo.class));
        Assert.assertEquals(Bar.class, map.get(Bar.class));
    }
}
