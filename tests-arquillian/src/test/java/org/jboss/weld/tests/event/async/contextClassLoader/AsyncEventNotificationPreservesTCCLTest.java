/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.event.async.contextClassLoader;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

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
 * Tests that async observer notification preserves TCCL that the original application had.
 * Test is mainly aimed at WFLY as there the TCCL means you can use CDI.current() from within the observer method (which
 * is what this test does as well). However, it makes sense to be sure this doesn't break anything in SE either.
 *
 * @author Matej Novotny
 */
@RunWith(Arquillian.class)
public class AsyncEventNotificationPreservesTCCLTest {

    @Inject
    Event<Message> event;

    @Inject
    Observer observer;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(AsyncEventNotificationPreservesTCCLTest.class))
                .addPackage(AsyncEventNotificationPreservesTCCLTest.class.getPackage());
    }

    @Test
    public void testAsyncEventHasSameTccl() {
        final CountDownLatch latch = new CountDownLatch(1);
        ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
        event.fireAsync(() -> latch.countDown());
        boolean latchWait = false;
        try {
            latchWait = latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assert.fail("Interrupted while waiting for latch countdown.");
        }
        if (!latchWait) {
            Assert.fail("CountDownLatch didn't reach 0 in time limit.");
        }
        // assert both TCCL are the same
        Assert.assertEquals(originalCl, observer.getTccl());
    }
}
