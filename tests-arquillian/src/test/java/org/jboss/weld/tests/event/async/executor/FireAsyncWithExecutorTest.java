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
package org.jboss.weld.tests.event.async.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
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
 * Simple testcase for {@link Event#fireAsync(Object, Executor)}. See WELD-1793 for details.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class FireAsyncWithExecutorTest {

    private static final SynchronousQueue<Response> SYNCHRONIZER = new SynchronousQueue<>();
    private static final AtomicBoolean REQUEST_RECEIVED = new AtomicBoolean();
    private static final AtomicReference<Response> RESPONSE_RECEIVED = new AtomicReference<>();

    @Inject
    private Event<Request> request;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(FireAsyncWithExecutorTest.class))
                .addPackage(FireAsyncWithExecutorTest.class.getPackage());
    }

    @Test
    public void testGivenExecutorUsed() throws InterruptedException {
        // easily identifiable thread pool
        Executor executor = Executors.newSingleThreadExecutor(runnable -> {
            return new Thread(runnable, FireAsyncWithExecutorTest.class.getName());
        });
        request.fireAsync(new Request(), NotificationOptions.ofExecutor(executor));
        final Response response = SYNCHRONIZER.poll(30, TimeUnit.SECONDS);
        assertTrue(REQUEST_RECEIVED.get());
        assertNotNull(RESPONSE_RECEIVED.get());
        assertEquals(FireAsyncWithExecutorTest.class.getName(), RESPONSE_RECEIVED.get().getThread().getName());
        assertNotNull("Synchronization failed", response);
        assertEquals(FireAsyncWithExecutorTest.class.getName(), response.getThread().getName());
    }

    @Dependent
    public static class ObserverBean {
        public static void receiveRequest(@ObservesAsync Request request, Event<Response> event) {
            REQUEST_RECEIVED.set(true);
            event.fire(new Response(Thread.currentThread()));
        }

        public static void receive(@Observes Response response) throws InterruptedException {
            RESPONSE_RECEIVED.set(response);
            SYNCHRONIZER.offer(response, 10, TimeUnit.SECONDS);
        }
    }
}
