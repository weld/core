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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.weld.logging.messages.BootstrapMessage;
import org.slf4j.cal10n.LocLogger;

/**
 * Implementation of {@link ExtendedExecutorServices} that uses a fixed thread pool. The size of the underlying thread pool is
 * determined by executing Runtime.getRuntime().availableProcessors() + 1.
 *
 * @author Jozef Hartinger
 *
 */
public class FixedThreadPoolExecutorServices extends AbstractExecutorServices {

    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Use daemon threads so that Weld does not hang e.g. in a SE environment.
     */
    private static class DeamonThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private static final String THREAD_NAME_PREFIX = "weld-worker-";
        private final ThreadFactory delegate = Executors.defaultThreadFactory();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = delegate.newThread(r);
            thread.setDaemon(true);
            thread.setName(THREAD_NAME_PREFIX + threadNumber.getAndIncrement());
            return thread;
        }
    }

    private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);
    private final int threadPoolSize;

    private final ExecutorService executor;

    public FixedThreadPoolExecutorServices() {
        this(DEFAULT_THREAD_POOL_SIZE);
    }

    public FixedThreadPoolExecutorServices(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
        this.executor = Executors.newFixedThreadPool(threadPoolSize, new DeamonThreadFactory());
        log.debug(BootstrapMessage.THREADS_IN_USE, threadPoolSize);
    }

    @Override
    public ExecutorService getTaskExecutor() {
        return executor;
    }

    @Override
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    @Override
    public String toString() {
        return "FixedThreadPoolExecutorServices [threadPoolSize=" + threadPoolSize + "]";
    }
}
