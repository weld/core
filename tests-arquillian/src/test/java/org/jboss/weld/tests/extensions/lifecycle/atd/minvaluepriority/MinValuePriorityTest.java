/*
 * JBoss, Home of Professional Open Source
 * Copyright 2020, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions.lifecycle.atd.minvaluepriority;

import static org.junit.Assert.assertEquals;

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
 * This tests the sorting of alternatives by priority when one of the priorities is {@link Integer.MIN_VALUE}.
 * This previously caused an integer overflow in the sorting, and resulted in an incorrect ordering.
 *
 * @author Karl von Randow
 * @see WELD-2628
 */
@RunWith(Arquillian.class)
public class MinValuePriorityTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(MinValuePriorityTest.class))
                .addPackage(AfterTypeDiscoveryObserver.class.getPackage())
                .addAsServiceProvider(Extension.class, AfterTypeDiscoveryObserver.class);
    }

    @Inject
    AfterTypeDiscoveryObserver extension;

    @Test
    public void testInitialAlternatives() {
        assertEquals(extension.getInitialAlternatives().size(), 2);
        assertEquals(extension.getInitialAlternatives().get(0), MinValuePriorityAlternative.class);
        assertEquals(extension.getInitialAlternatives().get(1), NormalAlternative.class);
    }

}
