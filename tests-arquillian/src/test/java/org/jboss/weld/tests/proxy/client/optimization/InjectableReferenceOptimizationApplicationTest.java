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
package org.jboss.weld.tests.proxy.client.optimization;

import static org.junit.Assert.assertNotNull;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1659
 */
@RunWith(Arquillian.class)
public class InjectableReferenceOptimizationApplicationTest extends InjectableReferenceOptimizationTestBase {

    @Test
    public void testApplicationScopedBean(Bravo bravo, Foxtrot foxtrot, Hotel hotel) {
        assertNotNull(bravo);
        assertIsProxy(bravo.getAlpha());
        assertIsProxy(bravo.getCharlie());
        assertIsProxy(bravo.getDelta());
        assertIsNotProxy(bravo.getEcho());
        // Optimization allowed, incomplete instance found
        assertIsNotProxy(bravo.getEcho().getBravo());
        // Optimization not allowed for other scopes
        assertIsProxy(bravo.getEcho().getAlpha());
        assertIsProxy(bravo.getEcho().getCharlie());
        assertIsProxy(bravo.getEcho().getDelta());
        // Don't optimize constructor injection
        assertNotNull(foxtrot);
        assertIsProxy(foxtrot.getFoxtrot());
        assertIsProxy(foxtrot.getFoxtrot().getFoxtrot());
        assertNotNull(hotel);
        assertIsProxy(hotel.getIndia().getHotel());
    }

}
