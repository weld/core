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

package org.jboss.weld.environment.se.test.instance.enhanced;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

/**
 * Similar to {@link WeldInstanceTest} but uses purely CDI APIs
 *
 * @author Matej Novotny
 */
@RunWith(Arquillian.class)
public class InstanceHandleTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InstanceHandleTest.class))
                .addPackage(InstanceHandleTest.class.getPackage()).addClass(ActionSequence.class)).build();
    }

    @Test
    public void testIsResolvable() {
        try (WeldContainer container = new Weld().initialize()) {
            assertTrue(container.select(Alpha.class).isResolvable());
            assertFalse(container.select(BigDecimal.class, Juicy.Literal.INSTANCE).isResolvable());
        }
    }

    @Test
    public void testGetHandle() {
        ActionSequence.reset();
        try (WeldContainer container = new Weld().initialize()) {

            Bean<?> alphaBean = container.getBeanManager().resolve(container.getBeanManager().getBeans(Alpha.class));
            Instance<Alpha> instance = container.select(Alpha.class);

            Instance.Handle<Alpha> alpha1 = instance.getHandle();
            assertEquals(alphaBean, alpha1.getBean());
            assertEquals(Dependent.class, alpha1.getBean().getScope());
            // Contextual reference is obtained lazily
            assertNull(ActionSequence.getSequenceData());

            String alpha2Id;

            // Test try-with-resource
            try (Instance.Handle<Alpha> alpha2 = instance.getHandle()) {
                assertNull(ActionSequence.getSequenceData());
                alpha2Id = alpha2.get().getId();
                assertFalse(alpha1.get().getId().equals(alpha2Id));
            }

            List<String> sequence = ActionSequence.getSequenceData();
            assertEquals(3, sequence.size());
            assertEquals("c" + alpha2Id, sequence.get(0));
            assertEquals("c" + alpha1.get().getId(), sequence.get(1));
            assertEquals("d" + alpha2Id, sequence.get(2));

            alpha1.destroy();
            // Alpha1 destroyed
            sequence = ActionSequence.getSequenceData();
            assertEquals(4, sequence.size());
            // Subsequent invocations are no-op
            alpha1.destroy();

            // Test normal scoped bean is also destroyed
            Instance<Bravo> bravoInstance = container.select(Bravo.class);
            String bravoId = bravoInstance.get().getId();
            try (Instance.Handle<Bravo> bravo = bravoInstance.getHandle()) {
                assertEquals(bravoId, bravo.get().getId());
                ActionSequence.reset();
            }
            sequence = ActionSequence.getSequenceData();
            assertEquals(1, sequence.size());
            assertEquals("d" + bravoId, sequence.get(0));
        }
    }

    @Test
    public void testGetAfterDestroyingContextualInstance() {
        ActionSequence.reset();
        try (WeldContainer container = new Weld().initialize()) {
            WeldClient client = container.select(WeldClient.class).get();
            assertNotNull(client);

            Instance.Handle<Alpha> alphaHandle = client.getAlphaInstance().getHandle();
            // trigger bean creation and assert
            alphaHandle.get();
            List<String> sequence = ActionSequence.getSequenceData();
            assertEquals(1, sequence.size());
            // trigger bean destruction
            alphaHandle.destroy();
            // verify that the destruction happened
            sequence = ActionSequence.getSequenceData();
            assertEquals(2, sequence.size());

            // try to invoke Handle.get() again; this should throw an exception
            try {
                alphaHandle.get();
                Assert.fail("Invoking Handle.get() after destroying contextual instance should throw an exception.");
            } catch (IllegalStateException e) {
                // expected
            }
        }
    }

    @Test
    public void testHandles() {
        ActionSequence.reset();
        try (WeldContainer container = new Weld().initialize()) {
            Instance<Processor> instance = container.select(Processor.class);
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
            for (Iterator<Instance.Handle<Processor>> iterator = instance.handles().iterator(); iterator.hasNext(); ) {
                try (Instance.Handle<Processor> handle = iterator.next()) {
                    handle.get().ping();
                }
            }
            assertEquals(4, ActionSequence.getSequenceSize());
            ActionSequence.assertSequenceDataContainsAll("firstPing", "secondPing", "firstDestroy", "secondDestroy");
        }

    }

    @Test
    public void testHandlesStream() {
        ActionSequence.reset();
        try (WeldContainer container = new Weld().initialize()) {

            Instance.Handle<Processor> processor = container.select(Processor.class).handlesStream().filter(h -> Dependent.class.equals(h.getBean().getScope()))
                    .findFirst().orElse(null);
            assertNull(ActionSequence.getSequenceData());
            assertNotNull(processor);
            assertEquals(FirstProcessor.class, processor.getBean().getBeanClass());
            assertEquals(FirstProcessor.class.getName(), processor.get().getId());
            processor.destroy();
            List<String> sequence = ActionSequence.getSequenceData();
            assertEquals(1, sequence.size());
            assertEquals("firstDestroy", sequence.get(0));

            Instance.Handle<WithPriority> withPriority = container.select(WithPriority.class).handlesStream().sorted(container.getHandlePriorityComparator()).findFirst()
                    .orElse(null);
            assertNotNull(withPriority);
            assertEquals(Priority3.class, withPriority.getBean().getBeanClass());
            withPriority.get();
            sequence = ActionSequence.getSequenceData();
            assertEquals(2, sequence.size());
            assertEquals("firstDestroy", sequence.get(0));
            assertEquals("c" + Priority3.class.getName(), sequence.get(1));
        }
    }
}
