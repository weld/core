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
package org.jboss.weld.environment.se.test.implicit.discovery.none;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.BeansXml;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ImplicitScanBeanDiscoveryModeNoneTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(JavaArchive.class).addClasses(Bong.class)
                        .addAsManifestResource(new BeansXml(BeanDiscoveryMode.NONE), "beans.xml"))
                .add(ShrinkWrap.create(JavaArchive.class).addClasses(ImplicitScanBeanDiscoveryModeNoneTest.class, Bang.class))
                .build();
    }

    @Test
    public void testDiscovery() {
        try (WeldContainer container = new Weld().property(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, Boolean.TRUE)
                .initialize()) {
            assertTrue(container.select(Bong.class).isUnsatisfied());
            Bang bang = container.select(Bang.class).get();
            assertNotNull(bang);
            assertEquals(1, bang.getVal());
        }
    }

}
