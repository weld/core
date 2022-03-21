/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.environment.se.test.event.container.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.event.Startup;
import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class StartupShutdownTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder().add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(StartupShutdownTest.class))
                .addPackage(StartupShutdownTest.class.getPackage())).build();
    }

    @Test
    public void testEvents() {
        // assert initial state
        Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.isEmpty());
        Assert.assertTrue(ObservingBean.OBSERVED_SHUTDOWN_EVENTS.isEmpty());

        try (WeldContainer container = new Weld().initialize()) {
            Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.size() == 2);
            Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.get(0).equals(ApplicationScoped.class.getSimpleName()));
            Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.get(1).equals(Startup.class.getSimpleName()));
        }

        // we can only assert these events after we shutdown Weld container
        Assert.assertTrue(ObservingBean.OBSERVED_SHUTDOWN_EVENTS.size() == 2);
        Assert.assertTrue(ObservingBean.OBSERVED_SHUTDOWN_EVENTS.get(0).equals(Shutdown.class.getSimpleName()));
        Assert.assertTrue(ObservingBean.OBSERVED_SHUTDOWN_EVENTS.get(1).equals(ApplicationScoped.class.getSimpleName()));

    }
}
