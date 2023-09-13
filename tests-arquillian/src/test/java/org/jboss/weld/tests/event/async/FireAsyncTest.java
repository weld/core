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
package org.jboss.weld.tests.event.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple testcase for WELD-1793
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class FireAsyncTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(FireAsyncTest.class))
                .addPackage(FireAsyncTest.class.getPackage());
    }

    @Inject
    private Event<Message> event;

    private static class ThreadCapturingMessage implements Message {
        private Thread receivingThread;

        @Override
        public void receive() {
            receivingThread = Thread.currentThread();
        }
    }

    @Test
    public void testAsyncEventExecutedInDifferentThread() throws InterruptedException {
        BlockingQueue<ThreadCapturingMessage> synchronizer = new LinkedBlockingQueue<>();
        event.fireAsync(new ThreadCapturingMessage()).thenAccept(synchronizer::add);
        assertFalse(synchronizer.poll(2, TimeUnit.SECONDS).receivingThread.equals(Thread.currentThread()));
    }

    @Test
    public void testExceptionPropagated() throws InterruptedException {
        BlockingQueue<Throwable> synchronizer = new LinkedBlockingQueue<>();
        event.fireAsync(() -> {
            throw new IllegalStateException(FireAsyncTest.class.getName());
        }).whenComplete((event, throwable) -> synchronizer.add(throwable));

        Throwable materializedThrowable = synchronizer.poll(2, TimeUnit.SECONDS);
        assertTrue(materializedThrowable instanceof CompletionException);
        Throwable[] suppressed = ((CompletionException) materializedThrowable).getSuppressed();
        assertEquals(1, suppressed.length);
        assertTrue(suppressed[0] instanceof IllegalStateException);
        assertEquals(FireAsyncTest.class.getName(), suppressed[0].getMessage());
    }
}
