/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.weld.logging.BootstrapLogger;

/**
 * Implementation of {@link ExtendedExecutorServices} that uses a fixed thread pool. However threads are terminated if no new tasks arrive within the keep-alive time.
 *
 * @author Martin Kouba
 */
public class TimingOutFixedThreadPoolExecutorServices extends AbstractExecutorServices {

    private final int threadPoolSize;
    /**
     * Keep-alive time in seconds
     */
    private long keepAliveTime;

    private final ThreadPoolExecutor executor;

    public TimingOutFixedThreadPoolExecutorServices(int threadPoolSize, long keepAliveTime) {

        this.threadPoolSize = threadPoolSize;
        this.keepAliveTime = keepAliveTime;

        this.executor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new DaemonThreadFactory("weld-worker-"));
        // Terminate threads if no new tasks arrive within the keep-alive time
        this.executor.allowCoreThreadTimeOut(true);

        BootstrapLogger.LOG.threadsInUse(threadPoolSize);
    }

    public int getPoolSize() {
        return executor.getPoolSize();
    }

    @Override
    public ExecutorService getTaskExecutor() {
        return executor;
    }

    @Override
    protected int getThreadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public String toString() {
        return String.format("TimingOutFixedThreadPoolExecutorServices [threadPoolSize=%s, keepAliveTime=%s]", threadPoolSize,
                keepAliveTime);
    }

}
