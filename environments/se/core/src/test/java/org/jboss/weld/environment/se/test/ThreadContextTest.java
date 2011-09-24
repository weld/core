/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.test;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.beans.threading.ThreadRunner;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ThreadContext, @ThreadScoped and the RunnableDecorator. The
 * decorator is enabled in the META-INF/beans.xml of the test resources.
 *
 * @author Peter Royle
 */
public class ThreadContextTest {

    public static final int NUM_THREADS = 10;
    public static final int NUM_LOOPS = 10;

    @Test
    public void testThreadContext() {
        WeldContainer weld = new Weld().initialize();

        List<ThreadRunner> threadRunners = new ArrayList<ThreadRunner>(NUM_THREADS);
        List<Thread> threads = new ArrayList<Thread>(NUM_THREADS);
        for (int threadIdx = 0; threadIdx < NUM_THREADS; threadIdx++) {
            final ThreadRunner threadRunner = weld.instance().select(ThreadRunner.class).get();
            threadRunner.setName("ThreadRunner thread #" + threadIdx);

            Thread thread = new Thread(threadRunner);
            thread.start();
            threads.add(thread);
            threadRunners.add(threadRunner);
        }

        // wait for all threads to complete
        assertEquals(NUM_THREADS, threads.size());
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        // bubble any exception from other threads to the surface
        assertEquals(NUM_THREADS, threadRunners.size());
        for (ThreadRunner threadRunner : threadRunners) {
            for (Exception e : threadRunner.getExceptions()) {
                throw new RuntimeException(e);
            }
        }

        shutdownManager(weld);
    }

    private void shutdownManager(WeldContainer weld) {
        ShutdownManager shutdownManager = weld.instance().select(ShutdownManager.class).get();
        shutdownManager.shutdown();
    }


}
