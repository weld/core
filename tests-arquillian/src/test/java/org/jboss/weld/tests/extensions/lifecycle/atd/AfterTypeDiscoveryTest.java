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
package org.jboss.weld.tests.extensions.lifecycle.atd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This only tests operations on {@link AfterTypeDiscovery} alternatives, interceptors and decorators collections.
 *
 * @author Martin Kouba
 * @see WELD-1660
 */
@RunWith(Arquillian.class)
public class AfterTypeDiscoveryTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(AfterTypeDiscoveryTest.class))
                .addPackage(AfterTypeDiscoveryObserver.class.getPackage())
                .addClass(ActionSequence.class).addAsServiceProvider(Extension.class, AfterTypeDiscoveryObserver.class);
    }

    @Inject
    AfterTypeDiscoveryObserver extension;

    @Inject
    BeanManager beanManager;

    @Test
    public void testInitialAlternatives() {
        assertEquals(extension.getInitialAlternatives().size(), 3);
        assertEquals(extension.getInitialAlternatives().get(0), AlphaAlternative.class);
        assertEquals(extension.getInitialAlternatives().get(1), BravoAlternative.class);
        assertEquals(extension.getInitialAlternatives().get(2), EchoAlternative.class);
    }

    @Test
    public void testFinalAlternatives() {
        // AlphaAlternative was removed from the list
        assertTrue(beanManager.getBeans(AlphaAlternative.class).isEmpty());
        // EchoAlternative was removed from the list
        assertTrue(beanManager.getBeans(EchoAlternative.class).isEmpty());
        // CharlieAlternative was enabled
        assertEquals(1, beanManager.getBeans(CharlieAlternative.class).size());
    }

    @Test
    public void testInitialInterceptors() {
        assertTrue(extension.getInitialInterceptors().contains(BravoInterceptor.class));
        assertTrue(extension.getInitialInterceptors().contains(AlphaInterceptor.class));
        assertTrue(extension.getInitialInterceptors().contains(EchoInterceptor.class));
    }

    @Test
    public void testFinalInterceptors(TransactionLogger logger) {
        ActionSequence.reset();
        logger.ping();
        List<String> data = ActionSequence.getSequenceData();
        assertEquals(2, data.size());
        assertEquals(CharlieInterceptor.class.getName(), data.get(0));
        assertEquals(AlphaInterceptor.class.getName(), data.get(1));
    }

    @Test
    public void testInitialDecorators() {
        assertEquals(extension.getInitialDecorators().size(), 3);
        assertEquals(extension.getInitialDecorators().get(0), AlphaDecorator.class);
        assertEquals(extension.getInitialDecorators().get(1), BravoDecorator.class);
        assertEquals(extension.getInitialDecorators().get(2), EchoDecorator.class);
    }

    @Test
    public void testFinalDecorators(TransactionLogger logger) {
        assertEquals("pingbravoalphacharlie", logger.log("ping"));
    }

}
