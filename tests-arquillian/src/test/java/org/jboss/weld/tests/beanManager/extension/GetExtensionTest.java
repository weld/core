/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.beanManager.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @see CDI-99
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class GetExtensionTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(GetExtensionTest.class))
                .addPackage(GetExtensionTest.class.getPackage())
                .addAsServiceProvider(Extension.class, AlphaExtension.class, BravoExtension.class, CharlieExtension.class,
                        VerifyingExtension.class);
    }

    @Test
    public void testTheSameInstanceAlwaysReturned(AlphaExtension alpha) {
        assertEquals(VerifyingExtension.STATE, alpha.getState());
        assertEquals(VerifyingExtension.STATE, manager.getExtension(AlphaExtension.class).getState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoInstanceAvailable() {
        manager.getExtension(InactiveExtension.class);
    }

    @Test
    public void testAmbiguousExtensions() {
        assertTrue(manager.getExtension(BravoExtension.class) instanceof BravoExtension);
        assertFalse(manager.getExtension(BravoExtension.class) instanceof CharlieExtension);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithNull() {
        manager.getExtension(null);
    }
}
