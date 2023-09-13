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
package org.jboss.weld.tests.event.async.complex;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
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

/*
 * More complex testcase for {@link ExperimentalEvent#fireAsync(Object)} inspired by Akka's tutorial
 * Besides testing the code it also showcases several usecases:
 *
 * 1) Using fireAsync() for a mutable message which is modified by observers. The state of the message is safely propagated between threads
 *    therefore no synchronization is needed by observers
 *
 * 2) Using fireAsync().thenAccept() to trigger an action once an asynchronously fire message is delivered and processed by all observers
 *
 * 3) General communication of non-blocking workers using fireAsync()
 *
 * 4) The fact that while the state of a message is safely propagated, the state of an observer needs to be properly guarded
 *    by concurrency constructs.
 *
 * 5) Performance of Weld - remove @Vetoed from HighPrecisionCalculation to see some numbers
 *
 */
@RunWith(Arquillian.class)
@ApplicationScoped
public class ComplexAsyncEventTest {

    // this is only static because JUnit won't use the @ApplicationScoped instance
    private static final BlockingQueue<PiApproximation> RESULT = new LinkedBlockingQueue<PiApproximation>();

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ComplexAsyncEventTest.class))
                .addPackage(ComplexAsyncEventTest.class.getPackage());
    }

    @Inject
    private Event<CalculationConfiguration> event;
    @Inject
    private Master master;

    @Test
    public void test() throws InterruptedException {
        // make it possible for configuration to be altered by observers
        event.fireAsync(new CalculationConfiguration()).thenAccept(master::compute);

        PiApproximation result = RESULT.poll(15, TimeUnit.SECONDS);
        Assert.assertTrue(3.140D < result.getPi() && result.getPi() < 3.145D);
    }

    public void observeResult(@Observes PiApproximation result) {
        RESULT.add(result);
    }
}
