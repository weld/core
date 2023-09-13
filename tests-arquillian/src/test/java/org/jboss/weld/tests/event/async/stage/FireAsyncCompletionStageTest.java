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
package org.jboss.weld.tests.event.async.stage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.EmbeddedContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 *
 */
@Category(EmbeddedContainer.class)
@RunWith(Arquillian.class)
public class FireAsyncCompletionStageTest {

    // An easily identifiable thread pool
    private ExecutorService simpleExecutor;

    @Inject
    private Event<Payload> event;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(FireAsyncCompletionStageTest.class))
                .addPackage(FireAsyncCompletionStageTest.class.getPackage())
                .addAsServiceProvider(Service.class, CustomExecutorServices.class);
    }

    @Before
    public void init() {
        simpleExecutor = Executors.newSingleThreadExecutor(runnable -> {
            return new Thread(runnable, FireAsyncCompletionStageTest.class.getName());
        });
    }

    public void tearDown() {
        if (simpleExecutor != null) {
            simpleExecutor.shutdown();
        }
    }

    @Test
    public void testGivenExecutorUsed() throws InterruptedException, ExecutionException {
        Payload payload = event.fireAsync(new Payload()).whenCompleteAsync((p, e) -> {
            // Observer must be notified in a thread from CustomExecutorServices
            assertTrue(p.getThreadName().startsWith(CustomExecutorServices.PREFIX));
            // Current thread must come from simple executor
            p.setThreadName(Thread.currentThread().getName());
        }, simpleExecutor).toCompletableFuture().get();
        assertEquals(FireAsyncCompletionStageTest.class.getName(), payload.getThreadName());
    }

    @Test
    public void testDefaultAsyncFacility() throws InterruptedException, ExecutionException {
        Payload payload = event.fireAsync(new Payload()).whenCompleteAsync((p, e) -> {
            // Observer must be notified in a thread from CustomExecutorServices
            assertTrue(p.getThreadName().startsWith(CustomExecutorServices.PREFIX));
        }).whenCompleteAsync((p, e) -> {
            // The default async executor comes from CustomExecutorServices
            p.setThreadName(Thread.currentThread().getName());
        }).toCompletableFuture().get();
        assertTrue(payload.getThreadName().startsWith(CustomExecutorServices.PREFIX));
    }
}
