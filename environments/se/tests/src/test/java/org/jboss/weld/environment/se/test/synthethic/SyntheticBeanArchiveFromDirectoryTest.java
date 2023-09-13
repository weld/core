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
package org.jboss.weld.environment.se.test.synthethic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.synthethic.testpackage.AlphaFromDirectory;
import org.jboss.weld.environment.se.test.synthethic.testpackage.nested.BetaFromDirectory;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SyntheticBeanArchiveFromDirectoryTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class).addClasses(SyntheticBeanArchiveFromDirectoryTest.class))
                .addDirectory("alpha-dir")
                .addClass(AlphaFromDirectory.class).addClass(BetaFromDirectory.class)
                .buildAndUp().build();
    }

    @Test
    public void testAddPackageFromDirectory() {
        try (WeldContainer container = new Weld()
                .disableDiscovery()
                .addPackages(AlphaFromDirectory.class.getPackage())
                .initialize()) {
            AlphaFromDirectory alpha = container.select(AlphaFromDirectory.class).get();
            Instance<BetaFromDirectory> betaInstance = container.select(BetaFromDirectory.class);
            assertTrue(betaInstance.isUnsatisfied());
            assertNotNull(alpha);
            assertEquals(1, alpha.ping());
        }
    }

    @Test
    public void testAddPackageFromDirectoryRecursively() {
        try (WeldContainer container = new Weld()
                .disableDiscovery()
                .addPackages(true, AlphaFromDirectory.class.getPackage())
                .initialize()) {
            AlphaFromDirectory alpha = container.select(AlphaFromDirectory.class).get();
            Instance<BetaFromDirectory> betaInstance = container.select(BetaFromDirectory.class);
            assertFalse(betaInstance.isUnsatisfied());
            assertNotNull(alpha);
            assertNotNull(betaInstance.get());
            assertEquals(1, alpha.ping());
            assertEquals(1, betaInstance.get().ping());
        }
    }

}
