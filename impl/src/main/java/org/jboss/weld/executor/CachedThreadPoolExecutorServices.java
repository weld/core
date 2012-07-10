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

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.weld.bootstrap.BootstrapConfiguration;
import org.jboss.weld.logging.messages.BootstrapMessage;
import org.slf4j.cal10n.LocLogger;

/**
 * Implementation of {@link ExtendedExecutorServices} that uses a cached thread pool. Similar to
 * {@link Executors#newCachedThreadPool()} but defines max thread pool size.
 *
 * @author Martin Kouba
 */
public class CachedThreadPoolExecutorServices extends AbstractExecutorServices {

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);

    private final int maxThreadPoolSize;
    /**
     * Keep-alive time in seconds
     */
    private long keepAliveTime;

    private final ThreadPoolExecutor executor;

    public CachedThreadPoolExecutorServices() {
        this(BootstrapConfiguration.DEFAULT_THREAD_POOL_SIZE, BootstrapConfiguration.DEFAULT_KEEP_ALIVE_TIME);
    }

    public CachedThreadPoolExecutorServices(int maxThreadPoolSize, long keepAliveTime) {

        this.maxThreadPoolSize = maxThreadPoolSize;
        this.keepAliveTime = keepAliveTime;

        this.executor = new ThreadPoolExecutor(0, maxThreadPoolSize,
                keepAliveTime, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new DeamonThreadFactory(new ThreadGroup("weld-workers"), "weld-worker-"));

        log.debug(BootstrapMessage.THREADS_IN_USE, maxThreadPoolSize);
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
        return maxThreadPoolSize;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public String toString() {
        return String.format("CachedThreadPoolExecutorServices [maxThreadPoolSize=%s, keepAliveTime=%s]", maxThreadPoolSize,
                keepAliveTime);
    }

}
