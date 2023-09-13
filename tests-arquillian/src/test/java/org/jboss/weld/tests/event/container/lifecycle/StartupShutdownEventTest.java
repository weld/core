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

package org.jboss.weld.tests.event.container.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Startup;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple test observing {@link Startup} and {@link jakarta.enterprise.event.Shutdown} events.
 * Note that we cannot properly verify shutdown events in these tests because the entirety of the test happens
 * before container attempts shutdown.
 */
@RunWith(Arquillian.class)
public class StartupShutdownEventTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(StartupShutdownEventTest.class))
                .addPackage(StartupShutdownEventTest.class.getPackage());
    }

    @Test
    public void testEventsObserved() {
        Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.size() == 2);
        Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.get(0).equals(ApplicationScoped.class.getSimpleName()));
        Assert.assertTrue(ObservingBean.OBSERVED_STARTING_EVENTS.get(1).equals(Startup.class.getSimpleName()));

        // Note that we cannot assert that shutdown event was invoked because entirety of this test class
        // happens before shutdown
        Assert.assertTrue(ObservingBean.OBSERVED_SHUTDOWN_EVENTS.isEmpty());
    }
}
