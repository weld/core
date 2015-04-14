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
package org.jboss.weld.environment.se.test.container.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.events.ContainerShutdown;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class ContainerEventsTest {

    @Test
    public void testEventsFired() {
        String id = "EVENT";
        ActionSequence.reset();
        try (WeldContainer container = new Weld(id).disableDiscovery().beanClasses(ContainerObserver.class).initialize()) {
            assertFalse(container.select(ContainerObserver.class).isUnsatisfied());
        }
        List<String> sequenceData = ActionSequence.getSequenceData();
        assertEquals(4, sequenceData.size());
        assertTrue(sequenceData.contains(ContainerInitialized.class.getName() + id));
        assertTrue(sequenceData.contains(ContainerInitialized.class.getName() + ApplicationScoped.class.getName() + id));
        assertTrue(sequenceData.contains(ContainerShutdown.class.getName() + id));
        assertTrue(sequenceData.contains(ContainerShutdown.class.getName() + ApplicationScoped.class.getName() + id));
    }

}
