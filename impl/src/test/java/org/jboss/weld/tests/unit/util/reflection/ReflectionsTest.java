/*
 * JBoss, Home of Professional Open Source
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.unit.util.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Set;

import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.ActualBar;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.AnotherBarInterface;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.BarInterface;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.BaseInterface;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.Foo;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.FooInterface;
import org.jboss.weld.tests.unit.util.reflection.interfaceClosure.SecondaryFooInterface;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionsTest {

    @Test
    public void decaptalizeTest() {
        assertNull(Reflections.decapitalize(null));
        assertEquals("", Reflections.decapitalize(""));
        assertEquals("foo", Reflections.decapitalize("foo"));
        assertEquals("foo", Reflections.decapitalize("Foo"));
        assertEquals("FOO", Reflections.decapitalize("FOO"));
        assertEquals("fooBar", Reflections.decapitalize("FooBar"));
    }

    @Test
    public void getInterfaceClosureTest() {
        Set<Class<?>> fooInterfaceClosure = Reflections.getInterfaceClosure(Foo.class);
        Assert.assertTrue(fooInterfaceClosure.contains(FooInterface.class));
        Assert.assertTrue(fooInterfaceClosure.contains(SecondaryFooInterface.class));
        Assert.assertTrue(fooInterfaceClosure.contains(BaseInterface.class));
        Assert.assertEquals(3, fooInterfaceClosure.size());

        Set<Class<?>> barInterfaceClosure = Reflections.getInterfaceClosure(ActualBar.class);
        Assert.assertTrue(barInterfaceClosure.contains(BarInterface.class));
        Assert.assertTrue(barInterfaceClosure.contains(AnotherBarInterface.class));
        Assert.assertTrue(barInterfaceClosure.contains(BaseInterface.class));
        assertEquals(3, barInterfaceClosure.size());

    }
}
