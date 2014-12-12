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
public class InjectableReferenceOptimizationDependentTest extends InjectableReferenceOptimizationTestBase {

    @Test
    @InSequence(1)
    public void testDependentScopedBean(Echo echo) {
        assertNotNull(echo);

        assertIsProxy(echo.getAlpha());
        // Lazy init alpha proxy -> existing alpha found
        assertIsNotProxy(echo.getAlpha().getEcho().getAlpha());
        // Optimization allowed, but no incomplete or existing instance found
        assertIsProxy(echo.getAlpha().getEcho().getBravo());
        assertIsProxy(echo.getAlpha().getEcho().getCharlie());
        assertIsProxy(echo.getAlpha().getEcho().getDelta());

        assertIsProxy(echo.getBravo());
        // Lazy init bravo proxy -> existing bravo found
        assertIsNotProxy(echo.getBravo().getEcho().getBravo());
        // Optimization not allowed for other scopes
        assertIsProxy(echo.getBravo().getEcho().getAlpha());
        assertIsProxy(echo.getBravo().getEcho().getCharlie());
        assertIsProxy(echo.getBravo().getEcho().getDelta());

        assertIsProxy(echo.getCharlie());
        // Optimization not allowed for @SessionScoped
        assertIsProxy(echo.getCharlie().getEcho().getAlpha());
        assertIsProxy(echo.getCharlie().getEcho().getBravo());
        assertIsProxy(echo.getCharlie().getEcho().getCharlie());
        assertIsProxy(echo.getCharlie().getEcho().getDelta());

        assertIsProxy(echo.getDelta());
        // Optimization not allowed for @ConversationScoped
        assertIsProxy(echo.getDelta().getEcho().getAlpha());
        assertIsProxy(echo.getDelta().getEcho().getBravo());
        assertIsProxy(echo.getDelta().getEcho().getCharlie());
        assertIsProxy(echo.getDelta().getEcho().getDelta());
    }

    @Test
    @InSequence(2)
    public void testDependentScopedBeanExistingApplicationScoped(Echo echo) {
        assertNotNull(echo);
        assertIsProxy(echo.getBravo());
        // Lazy init alpha proxy
        // Optimization allowed - alpha is @RequestScoped and existing bravo found (due to previous test method)
        assertIsNotProxy(echo.getAlpha().getEcho().getBravo());
    }

}
