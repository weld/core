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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ThreadFactory} that creates daemon threads so that Weld does not hang e.g. in a SE environment.
 *
 * @author Jozef Hartinger
 *
 */
public class DaemonThreadFactory implements ThreadFactory {

    public static final String WELD_WORKERS = "weld-workers";

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String threadNamePrefix;
    private final ThreadGroup threadGroup;

    public DaemonThreadFactory(ThreadGroup threadGroup, String threadNamePrefix) {
        this.threadGroup = threadGroup;
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(threadGroup, r, threadNamePrefix + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }

    // Holder class to postpone thread group creation until when it's needed.
    public static class ThreadPoolHolder {
        public static final ThreadGroup WELD_WORKERS_THREAD_GROUP = new ThreadGroup(WELD_WORKERS);
    }
}