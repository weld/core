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
package org.jboss.weld.environment.se.test.event.options.mode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.event.NotificationOptions;

import org.jboss.arquillian.container.se.api.ClassPath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.event.options.mode.PriorityObservers.PriorityMessage;
import org.jboss.weld.events.WeldNotificationOptions;
import org.jboss.weld.events.WeldNotificationOptions.NotificationMode;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class NotificationModeTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ClassPath.builder()
                .add(ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(NotificationModeTest.class))
                        .addPackage(NotificationModeTest.class.getPackage()))
                .build();
    }

    @Test
    public void testObserversNotifiedSerially() throws InterruptedException {
        try (WeldContainer container = createWeld()) {
            CountDownLatch latch = new CountDownLatch(4);
            Set<String> threadNames = new CopyOnWriteArraySet<>();
            List<String> observers = new CopyOnWriteArrayList<>();
            container.event().select(PriorityMessage.class).fireAsync((id) -> {
                threadNames.add(Thread.currentThread().getName());
                observers.add(id);
                latch.countDown();
            }, NotificationOptions.builder().set(WeldNotificationOptions.MODE, NotificationMode.SERIAL).build());
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            // Observers were notified using the same thread
            assertEquals(1, threadNames.size());
            assertEquals(4, observers.size());
            assertEquals("1", observers.get(0));
            assertEquals("20", observers.get(1));
            assertEquals("300", observers.get(2));
            assertEquals("last", observers.get(3));
        }
    }

    @Test
    public void testAsyncEventExecutedInDifferentThread() throws InterruptedException {
        try (WeldContainer container = createWeld()) {
            BlockingQueue<Message> synchronizer = new LinkedBlockingQueue<>();
            Set<String> threadNames = new CopyOnWriteArraySet<>();
            container.event().select(Message.class)
                    .fireAsync(() -> threadNames.add(Thread.currentThread().getName()),
                            WeldNotificationOptions.withParallelMode())
                    .thenAccept(synchronizer::add);
            Message message = synchronizer.poll(2, TimeUnit.SECONDS);
            assertNotNull(message);
            // Eeach observer was notified using a different thread
            assertEquals(2, threadNames.size());
        }
    }

    @Test
    public void testExceptionPropagated() throws InterruptedException {
        try (WeldContainer container = createWeld()) {
            BlockingQueue<Throwable> synchronizer = new LinkedBlockingQueue<>();
            container.event().select(Message.class).fireAsync(() -> {
                throw new IllegalStateException(NotificationModeTest.class.getName());
            }, WeldNotificationOptions.withParallelMode()).whenComplete((event, throwable) -> synchronizer.add(throwable));

            Throwable materializedThrowable = synchronizer.poll(2, TimeUnit.SECONDS);
            assertTrue(materializedThrowable instanceof CompletionException);
            Throwable[] suppressed = ((CompletionException) materializedThrowable).getSuppressed();
            assertEquals(2, suppressed.length);
            assertTrue(suppressed[0] instanceof IllegalStateException);
            assertEquals(NotificationModeTest.class.getName(), suppressed[0].getMessage());
            assertTrue(suppressed[1] instanceof IllegalStateException);
            assertEquals(NotificationModeTest.class.getName(), suppressed[1].getMessage());
        }
    }

    @Test
    public void testInvalidMode() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try (WeldContainer container = createWeld()) {
            try {
                container.event().select(Message.class).fireAsync(() -> latch.countDown(),
                        NotificationOptions.of(WeldNotificationOptions.MODE, "unsupported"));
                fail("Notification should have failed ");
            } catch (IllegalArgumentException expected) {
            }
            // Assert that observers were not notified
            assertFalse(latch.await(500, TimeUnit.MILLISECONDS));
        }
    }

    static WeldContainer createWeld() {
        return new Weld().property(ConfigurationKey.EXECUTOR_THREAD_POOL_TYPE.get(), "FIXED")
                .property(ConfigurationKey.EXECUTOR_THREAD_POOL_SIZE.get(), 3)
                .initialize();
    }

}
