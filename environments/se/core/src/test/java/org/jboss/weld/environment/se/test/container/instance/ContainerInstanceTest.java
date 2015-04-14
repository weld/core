/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.container.instance;

import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ContainerInstanceTest {

    @Test
    public void testDependentInstanceDestroy() {
        Foo.DESTROYED.set(false);
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class).initialize()) {
            // We only use one container - CDI instance is unambiguous
            Foo foo = CDI.current().select(Foo.class).get();
            CDI.current().destroy(foo);
            // We should be able to destroy a dependent bean instance obtained by CDI
            assertTrue(Foo.DESTROYED.get());
        }
    }

    @Test
    public void testDependentInstanceDestroyedDuringShutdown() {
        Foo.DESTROYED.set(false);
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class).initialize()) {
            CDI.current().select(Foo.class).get();
        }
        // Dependent bean instances obtained by CDI should be destroyed correctly during shutdown
        assertTrue(Foo.DESTROYED.get());
    }
}
