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
package org.jboss.weld.environment.se.test.container.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class WeldSEProviderTest {

    @Test
    public void testNoContainer() {
        try {
            CDI.current();
            fail();
        } catch (IllegalStateException expected) {
            // OK
        }
    }

    @Test
    public void testSingleContainer() {
        try (WeldContainer weldContainer = new Weld().disableDiscovery().beanClasses(Foo.class).initialize()) {
            assertCdi(CDI.current(), weldContainer.getId());
            assertCdi(weldContainer.select(Foo.class).get().getCurrent(), weldContainer.getId());
        }
    }

    @Test
    public void testMultipleContainers() {
        Weld weld = new Weld().disableDiscovery();
        try {
            WeldContainer weldContainer1 = weld.containerId("foo").beanClasses(Foo.class).initialize();
            WeldContainer weldContainer2 = weld.containerId("bar").beanClasses(Bar.class).initialize();
            // The caller cannot be used to determine the right container - the first one is returned
            assertCdi(CDI.current(), weldContainer1.getId());
            // Foo is caller
            assertCdi(weldContainer1.select(Foo.class).get().getCurrent(), weldContainer1.getId());
            // Bar is caller
            assertCdi(weldContainer2.select(Bar.class).get().getCurrent(), weldContainer2.getId());
        } finally {
            weld.shutdown();
        }
    }

    @Test
    public void testExtension() {
        TestExtension.reset();
        try (WeldContainer weldContainer = new Weld().disableDiscovery().beanClasses(Foo.class)
                .addExtension(new TestExtension()).initialize()) {
            BeanManager beanManager = TestExtension.beanManagerReference.get();
            assertNotNull(beanManager);
            Bean<?> fooBean = TestExtension.fooBeanReference.get();
            assertNotNull(fooBean);
        }
    }

    @Test
    public void testDependentInstanceDestroyedDuringShutdown() {
        Baz.DISPOSED.set(false);
        try (WeldContainer weldContainer = new Weld().disableDiscovery().beanClasses(Baz.class).initialize()) {
            assertTrue(CDI.current().select(Baz.class).get().ping());
            assertFalse(Baz.DISPOSED.get());
        }
        assertTrue(Baz.DISPOSED.get());
    }

    private void assertCdi(CDI<Object> cdi, String id) {
        assertTrue(cdi instanceof WeldContainer);
        assertEquals(id, ((WeldContainer) cdi).getId());
    }
}
