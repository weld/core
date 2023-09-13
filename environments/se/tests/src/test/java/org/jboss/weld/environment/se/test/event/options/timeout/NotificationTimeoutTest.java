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
package org.jboss.weld.environment.se.test.event.options.timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.enterprise.event.NotificationOptions;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.events.WeldNotificationOptions;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class NotificationTimeoutTest {

    // has to be CLEARED in the beginning of each test method where you use it
    public static final List<String> SUCCESSION_OF_EVENTS = new CopyOnWriteArrayList<String>();

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NotificationTimeoutTest.class))
                        .addPackage(NotificationTimeoutTest.class.getPackage())
                        .addClass(IncompleteCustomExecutorServices.class))
                .build();
    }

    @Test
    public void testMultipleObserverNotificationTimeout() throws InterruptedException {
        SUCCESSION_OF_EVENTS.clear();
        try (WeldContainer container = new Weld().initialize()) {
            BlockingQueue<Throwable> synchronizer = new LinkedBlockingQueue<>();
            CountDownLatch countdown = new CountDownLatch(2);
            container.event().select(CountDownLatch.class)
                    .fireAsync(countdown, NotificationOptions.of(WeldNotificationOptions.TIMEOUT, 2000l))
                    .exceptionally((Throwable t) -> {
                        // handle TimeoutException we got here
                        SUCCESSION_OF_EVENTS.add("Timeout");
                        synchronizer.add(t);
                        return null;
                    });
            Throwable fromSynchronizer = synchronizer.poll(5, TimeUnit.SECONDS);
            Assert.assertNotNull(fromSynchronizer);
            Assert.assertTrue(fromSynchronizer.getCause().toString().contains(TimeoutException.class.getSimpleName()));
            // wait for the observers to finish their jobs
            countdown.await();
            // assert contents AND order of events
            Assert.assertTrue(SUCCESSION_OF_EVENTS.equals(getExpectedListOfEvents()));
        }
    }

    @Test
    public void testMultipleObserverParallelNotificationTimeout() throws InterruptedException {
        SUCCESSION_OF_EVENTS.clear();
        try (WeldContainer container = new Weld().initialize()) {
            BlockingQueue<Throwable> synchronizer = new LinkedBlockingQueue<>();
            CountDownLatch countdown = new CountDownLatch(2);
            NotificationOptions options = NotificationOptions.builder().set(WeldNotificationOptions.TIMEOUT, "2000")
                    .set(WeldNotificationOptions.MODE, WeldNotificationOptions.NotificationMode.PARALLEL).build();
            container.event().select(CountDownLatch.class)
                    .fireAsync(countdown, options)
                    .exceptionally((Throwable t) -> {
                        // handle TimeoutException we got here
                        SUCCESSION_OF_EVENTS.add("Timeout");
                        synchronizer.add(t);
                        return null;
                    });
            Throwable fromSynchronizer = synchronizer.poll(5, TimeUnit.SECONDS);
            Assert.assertNotNull(fromSynchronizer);
            Assert.assertTrue(fromSynchronizer.getCause().toString().contains(TimeoutException.class.getSimpleName()));
            // wait for the observers to finish their jobs
            countdown.await();
            // with parallel execution, we can only assert that all observers were notified (the order might differ)
            Assert.assertTrue(SUCCESSION_OF_EVENTS.size() == 3);
            Assert.assertTrue(SUCCESSION_OF_EVENTS.containsAll(getExpectedListOfEvents()));
        }
    }

    @Test
    public void testBadTimeoutInputThrowsException() throws InterruptedException {
        SUCCESSION_OF_EVENTS.clear();
        CountDownLatch latch = new CountDownLatch(1);
        try (WeldContainer container = new Weld().initialize()) {
            container.event().select(CountDownLatch.class)
                    .fireAsync(latch, NotificationOptions.of(WeldNotificationOptions.TIMEOUT, 1.2345));
            Assert.fail("Bad input valut should throw IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            // expected, should throw IAE
            // assert that no observers were notified
            Assert.assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
            Assert.assertTrue(SUCCESSION_OF_EVENTS.isEmpty());
        }
    }

    @Test
    public void testTimeoutNotReached() throws InterruptedException {
        // this deployment omits the SLOW observer so as to AVOID timeout - e.g. this deployment has only one observer
        final String success = "Success";
        Weld weld = new Weld().disableDiscovery().addBeanClasses(HardworkingObserver.class);
        try (WeldContainer container = weld.initialize()) {
            BlockingQueue<String> synchronizer = new LinkedBlockingQueue<>();
            CountDownLatch countdown = new CountDownLatch(1);
            container.event().select(CountDownLatch.class)
                    .fireAsync(countdown, NotificationOptions.of(WeldNotificationOptions.TIMEOUT, new Long(2000l)))
                    .thenRun(() -> synchronizer.add(success))
                    .exceptionally((Throwable t) -> { // only executes if the notification ended with exception
                        synchronizer.add("Exception");
                        return null;
                    });
            // wait for the observer to finish the job
            countdown.await();
            // assert that BlockingQueue has exactly one item indicating success
            String fromSynchronizer = synchronizer.poll(3, TimeUnit.SECONDS);
            Assert.assertNotNull(fromSynchronizer);
            Assert.assertTrue(synchronizer.isEmpty());
            Assert.assertEquals(success, fromSynchronizer);
        }
    }

    @Test
    public void testSingleObserverNotificationTimeout() throws InterruptedException {
        SUCCESSION_OF_EVENTS.clear();
        // this deployment omits the FAST observer so as to FORCE timeout - e.g. this deployment has only one observer
        Weld weld = new Weld().disableDiscovery().addBeanClasses(LazyObserver.class);
        try (WeldContainer container = weld.initialize()) {
            BlockingQueue<Throwable> synchronizer = new LinkedBlockingQueue<>();
            CountDownLatch countdown = new CountDownLatch(1);
            container.event().select(CountDownLatch.class)
                    .fireAsync(countdown, NotificationOptions.of(WeldNotificationOptions.TIMEOUT, new Long(1000l)))
                    .exceptionally((Throwable t) -> {
                        SUCCESSION_OF_EVENTS.add("Timeout");
                        synchronizer.add(t);
                        return null;
                    });
            Throwable fromSynchronizer = synchronizer.poll(5, TimeUnit.SECONDS);
            Assert.assertNotNull(fromSynchronizer);
            Assert.assertTrue(fromSynchronizer.getCause().toString().contains(TimeoutException.class.getSimpleName()));
            // wait for the observer to finish the jobs
            countdown.await();
            // assert that two events happened and timeout came first
            Assert.assertTrue(SUCCESSION_OF_EVENTS.size() == 2);
            Assert.assertTrue(SUCCESSION_OF_EVENTS.get(0).equals("Timeout"));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCustomExecutorServiceNotImplementingTimerExecutor() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Weld weld = new Weld().disableDiscovery().addBeanClasses(HardworkingObserver.class)
                .addServices(new IncompleteCustomExecutorServices());
        try (WeldContainer container = weld.initialize()) {
            try {
                container.event().select(CountDownLatch.class).fireAsync(latch,
                        NotificationOptions.of(WeldNotificationOptions.TIMEOUT, "2000"));
            } catch (IllegalArgumentException e) {
                // expected, should throw IAE
                Assert.assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
            }
        }
    }

    private List<String> getExpectedListOfEvents() {
        List<String> expected = new ArrayList<String>();
        expected.add("Work");
        expected.add("Timeout");
        expected.add("Coffee");
        return expected;
    }
}
