/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.implicit.merging;

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

/**
 * Simulates a situation where there is implicit discovery (javax.enterprise.inject.scan.implicit)
 * and no org.jboss.weld.se.archive.isolation and there are multiple archives on the classpath.
 * This triggers beans.xml merging.
 * <p>
 * The test controls deployment flow so as to be able to set system variables.
 */
@RunWith(Arquillian.class)
public class BeansXmlMergingTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        JavaArchive archive01 = ShrinkWrap
                .create(JavaArchive.class)
                // no beans.xml here but still should be discovered
                .addClasses(Foo.class);
        JavaArchive archive02 = ShrinkWrap
                .create(JavaArchive.class)
                .addAsManifestResource(new BeansXml(BeanDiscoveryMode.ALL),
                        "beans.xml")
                .addClasses(Bar.class, BeansXmlMergingTest.class);
        return ClassPath.builder().add(archive01).add(archive02).build();
    }

    @Test
    public void testArchivesCanBeDeployedAndDiscoveryWorks() {
        try (WeldContainer container = new Weld()
                .property(Weld.JAVAX_ENTERPRISE_INJECT_SCAN_IMPLICIT, Boolean.TRUE)
                .property(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, false)
                .initialize()) {
            assertTrue(container.select(Foo.class).isResolvable());
            assertTrue(container.select(Bar.class).isResolvable());
        }
    }
}