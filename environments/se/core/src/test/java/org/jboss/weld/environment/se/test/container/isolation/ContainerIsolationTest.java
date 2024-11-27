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
package org.jboss.weld.environment.se.test.container.isolation;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that Weld containers that running at once do not interfere.
 *
 * @see WELD-1619
 *
 * @author Jozef Hartinger
 *
 */
public class ContainerIsolationTest {

    @Test
    public void testContainerIsolation() {

        Weld weld1 = new Weld("1");
        WeldContainer weldContainer1 = weld1.initialize();
        Foo foo1 = weldContainer1.instance().select(Foo.class).get();

        Weld weld2 = new Weld("2");
        WeldContainer weldContainer2 = weld2.initialize();
        Foo foo2 = weldContainer2.select(Foo.class).get();

        foo1.setValue(1);
        foo2.setValue(2);

        Assert.assertEquals(1, foo1.getValue());
        Assert.assertEquals(2, foo2.getValue());

        weld1.shutdown();

        Assert.assertEquals(2, foo2.getValue());
        weld2.shutdown();
    }
}
