/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.instance.enhanced;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.inject.WeldInstance.Handler;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class WeldInstanceTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(WeldInstanceTest.class)).addPackage(WeldInstanceTest.class.getPackage())
            .addClass(ActionSequence.class);
    }

    @Test
    public void testIsResolvable(Client client) {
        ActionSequence.reset();
        assertNotNull(client);
        assertTrue(client.getAlphaInstance().isResolvable());
        assertFalse(client.getBigDecimalInstance().isResolvable());
    }

    @Test
    public void testGetHandler(Client client, BeanManager beanManager) {
        ActionSequence.reset();
        assertNotNull(client);

        Bean<?> alphaBean = beanManager.resolve(beanManager.getBeans(Alpha.class));
        WeldInstance<Alpha> instance = client.getAlphaInstance();

        Handler<Alpha> alpha1 = instance.getHandler();
        assertEquals(alphaBean, alpha1.getBean());
        assertEquals(Dependent.class, alpha1.getBean().getScope());

        String alpha2Id;

        // Test try-with-resource
        try (Handler<Alpha> alpha2 = instance.getHandler()) {
            alpha2Id = alpha2.get().getId();
            assertFalse(alpha1.get().getId().equals(alpha2Id));
        }

        List<String> sequence = ActionSequence.getSequenceData();
        assertEquals(1, sequence.size());
        assertEquals(alpha2Id, sequence.get(0));

        alpha1.destroy();
        // Subsequent invocations are no-op
        alpha1.destroy();

        sequence = ActionSequence.getSequenceData();
        assertEquals(2, sequence.size());

        // Test normal scoped bean is also destroyed
        WeldInstance<Bravo> bravoInstance = client.getInstance().select(Bravo.class);
        String bravoId = bravoInstance.get().getId();
        try (Handler<Bravo> bravo = bravoInstance.getHandler()) {
            assertEquals(bravoId, bravo.get().getId());
            ActionSequence.reset();
        }
        sequence = ActionSequence.getSequenceData();
        assertEquals(1, sequence.size());
        assertEquals(bravoId, sequence.get(0));
    }

    @Test
    public void testGetAfterDestroyingContextualInstance(Client client) {
        ActionSequence.reset();
        assertNotNull(client);

        Handler<Alpha> alphaHandle = client.getAlphaInstance().getHandler();
        // trigger bean creation
        alphaHandle.get();
        // trigger bean destruction
        alphaHandle.destroy();
        // verify that the destruction happened
        List<String> sequence = ActionSequence.getSequenceData();
        assertEquals(1, sequence.size());

        // try to invoke Handle.get() again; this should throw an exception
        try {
            alphaHandle.get();
            fail("Invoking Handle.get() after destroying contextual instance should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testHandlers(WeldInstance<Processor> instance) {
        ActionSequence.reset();
        assertTrue(instance.isAmbiguous());
        for (Handler<Processor> handler : instance.handlers()) {
            handler.get().ping();
            if (handler.getBean().getScope().equals(Dependent.class)) {
                handler.destroy();
            }
        }
        assertEquals(3, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll("firstPing", "secondPing", "firstDestroy");

        ActionSequence.reset();
        assertTrue(instance.isAmbiguous());
        for (Iterator<Handler<Processor>> iterator = instance.handlers().iterator(); iterator.hasNext();) {
            try (Handler<Processor> handler = iterator.next()) {
                handler.get().ping();
            }
        }
        assertEquals(4, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll("firstPing", "secondPing", "firstDestroy", "secondDestroy");
    }
}
