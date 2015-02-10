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
package org.jboss.weld.tests.experimental.event.async.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.experimental.ExperimentalEvent;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple testcase for {@link ExperimentalEvent#fireAsync(Object, Executor)}. See WELD-1793 for details.
 *
 * @author Jozef Hartinger
 *
 */
@RunWith(Arquillian.class)
public class FireAsyncWithExecutorTest {

    private static final SynchronousQueue<Response> SYNCHRONIZER = new SynchronousQueue<>();

    @Inject
    private ExperimentalEvent<Request> request;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(FireAsyncWithExecutorTest.class.getPackage());
    }

    @Test
    public void testGivenExecutorUsed() throws InterruptedException {
        // easily identifiable thread pool
        Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, FireAsyncWithExecutorTest.class.getName());
            }
        });
        request.fireAsync(new Request(), executor);
        final Response response = SYNCHRONIZER.poll(30, TimeUnit.SECONDS);
        Assert.assertNotNull(response);
        Assert.assertEquals(FireAsyncWithExecutorTest.class.getName(), response.getThread().getName());
    }

    public static void receiveRequest(@Observes Request request, Event<Response> event) {
        event.fire(new Response(Thread.currentThread()));
    }

    public static void receive(@Observes Response response) {
        SYNCHRONIZER.add(response);
    }
}
