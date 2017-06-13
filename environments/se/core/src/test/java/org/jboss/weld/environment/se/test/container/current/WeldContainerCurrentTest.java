/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.container.current;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class WeldContainerCurrentTest {

    @Test
    public void testCorrectContainerReturned() {
        String containerId = "007";
        try (WeldContainer container = new Weld(containerId).disableDiscovery().addBeanClass(DumbBean.class).initialize()) {
            Assert.assertTrue(container.isRunning());
            Assert.assertTrue(WeldContainer.current().getId().equals(containerId));
        }
    }

    @Test
    public void testExceptionThrownWithMultipleContainers() {
        String containerIdOne = "007";
        String containerIdTwo = "008";
        WeldContainer containerOne = new Weld(containerIdOne).disableDiscovery().addBeanClass(DumbBean.class).initialize();
        WeldContainer containerTwo = new Weld(containerIdTwo).disableDiscovery().addBeanClass(DumbBean.class).initialize();
        try {
            WeldContainer.current();
        } catch (IllegalStateException e) {
            // expected
            return;
        } finally {
            containerOne.close();
            containerTwo.close();
        }
        Assert.fail();
    }

    @Test
    public void testExceptionThrownWithNoContainer() {
        // do not start container
        try {
            WeldContainer.current();
        } catch (IllegalStateException e) {
            // expected
            return;
        }
        Assert.fail();
    }
}
