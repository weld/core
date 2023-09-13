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
package org.jboss.weld.environment.se.test.implicit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The same as {@link ImplicitScanSmokeTest} but using a system property to enable the implicit scan.
 *
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class ImplicitScanSystemPropertySmokeTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        final JavaArchive bda1 = ShrinkWrap.create(JavaArchive.class).addClasses(Foo.class, Bar.class)
                .addAsManifestResource(new BeansXml(), "beans.xml");
        final JavaArchive bda2 = ShrinkWrap.create(JavaArchive.class).addClasses(ImplicitScanSystemPropertySmokeTest.class,
                Baz.class);
        return ClassPath.builder().add(bda1).add(bda2).addSystemProperty(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, "true")
                .build();
    }

    @Test
    public void testDiscovery() {
        try (WeldContainer container = new Weld().initialize()) {
            Foo foo = container.select(Foo.class).get();
            assertNotNull(foo);
            assertEquals(1, foo.ping());
            Baz baz = container.select(Baz.class).get();
            assertNotNull(baz);
            assertEquals(1, baz.ping());
        }
    }

}
