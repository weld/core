/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.discovery.beansXml.empty.legacy;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests behavior if there is an empty beans.xml along with system property setting for legacy treatment.
 * See {@link Weld#EMPTY_BEANS_XML_DISCOVERY_MODE_ALL}
 *
 * @author Matej Novotny
 */
@RunWith(Arquillian.class)
public class LegacyEmptyBeansXmlTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(JavaArchive.class)
                .addPackage(LegacyEmptyBeansXmlTest.class.getPackage())
                // make sure we add empty beans.xml
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml"))
                .build();
    }

    @Test
    public void testBeanDiscoveryIsAll() {
        try (WeldContainer container = new Weld()
                .property(Weld.EMPTY_BEANS_XML_DISCOVERY_MODE_ALL, true)
                .initialize()) {
            // Foo is annotated and as such should be always discovered
            Assert.assertTrue(container.select(Foo.class).isResolvable());
            // Bar has no annotation and so it should only be picked up in all discovery mode
            Assert.assertTrue(container.select(Bar.class).isResolvable());
        }
    }
}
