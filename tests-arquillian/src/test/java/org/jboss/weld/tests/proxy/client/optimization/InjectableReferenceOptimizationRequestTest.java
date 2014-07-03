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
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-1659
 */
@RunWith(Arquillian.class)
public class InjectableReferenceOptimizationRequestTest extends InjectableReferenceOptimizationTestBase {

    @InSequence(2)
    @Test
    public void testRequestScopedBean(Alpha alpha, Golf golf) {
        assertNotNull(alpha);
        assertIsProxy(alpha.getBravo());
        assertIsProxy(alpha.getCharlie());
        assertIsProxy(alpha.getDelta());
        assertIsNotProxy(alpha.getEcho());
        // Optimization allowed, incomplete instance found
        assertIsNotProxy(alpha.getEcho().getAlpha());
        // Optimization allowed, but no incomplete or existing instance found
        assertIsProxy(alpha.getEcho().getBravo());
        assertIsProxy(alpha.getEcho().getCharlie());
        assertIsProxy(alpha.getEcho().getDelta());
        // Don't optimize constructor injection
        assertNotNull(golf);
        assertIsProxy(golf.getGolf());
        assertIsProxy(golf.getGolf().getGolf());
        // Don't optimize custom scopes
        assertNotNull(alpha.getCustom());
        assertIsProxy(alpha.getCustom());
    }

    @Test
    @InSequence(1)
    public void initCustom(Custom custom) {
        assertNotNull(custom);
        // Lazy init @CustomScoped custom
        custom.ping();
    }

}
