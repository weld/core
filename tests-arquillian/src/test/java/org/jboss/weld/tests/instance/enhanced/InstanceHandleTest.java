/*
 * JBoss, Home of Professional Open Source
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InstanceHandleTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceHandleTest.class))
                .addClasses(Alpha.class, Bravo.class, Client.class, FirstProcessor.class, Processor.class,
                        SecondProcessor.class, Juicy.class)
                .addPackage(WeldInstanceTest.class.getPackage())
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
    public void testGetHandle(Client client, BeanManager beanManager) {
        ActionSequence.reset();
        assertNotNull(client);

        Bean<?> alphaBean = beanManager.resolve(beanManager.getBeans(Alpha.class));
        Instance<Alpha> instance = client.getAlphaInstance();

        Instance.Handle<Alpha> alpha1 = instance.getHandle();
        assertEquals(alphaBean, alpha1.getBean());
        assertEquals(Dependent.class, alpha1.getBean().getScope());

        String alpha2Id;

        // Test try-with-resource
        try (Instance.Handle<Alpha> alpha2 = instance.getHandle()) {
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
        Instance<Bravo> bravoInstance = client.getInstance().select(Bravo.class);
        String bravoId = bravoInstance.get().getId();
        try (Instance.Handle<Bravo> bravo = bravoInstance.getHandle()) {
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

        Instance.Handle<Alpha> alphaHandle = client.getAlphaInstance().getHandle();
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
            Assert.fail("Invoking Handle.get() after destroying contextual instance should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testHandles(Instance<Processor> instance) {
        ActionSequence.reset();
        assertTrue(instance.isAmbiguous());
        for (Instance.Handle<Processor> handle : instance.handles()) {
            handle.get().ping();
            if (handle.getBean().getScope().equals(Dependent.class)) {
                handle.destroy();
            }
        }
        assertEquals(3, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll("firstPing", "secondPing", "firstDestroy");

        ActionSequence.reset();
        assertTrue(instance.isAmbiguous());
        for (Iterator<? extends Instance.Handle<Processor>> iterator = instance.handles().iterator(); iterator.hasNext();) {
            try (Instance.Handle<Processor> handle = iterator.next()) {
                handle.get().ping();
            }
        }
        assertEquals(4, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll("firstPing", "secondPing", "firstDestroy", "secondDestroy");
    }
}
