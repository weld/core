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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.ForwardingExecutorService;

public class ProfilingExecutorServices implements ExecutorServices {

    private class Measurement {
        private static final int CALLER_STACK_TRACE_ORDER = 3;
        private volatile long start = 0;
        private final int id = executionId.incrementAndGet();

        public void startProfiling() {
            if (start != 0) {
                throw new IllegalStateException();
            }
            start = System.currentTimeMillis();
        }

        public void stopProfiling() {
            if (start == 0L) {
                throw new IllegalStateException();
            }
            final long current = System.currentTimeMillis();
            final long time = current - start;
            StackTraceElement myCaller = (Thread.currentThread().getStackTrace())[CALLER_STACK_TRACE_ORDER];
            BootstrapLogger.LOG.infov("ThreadPool task execution with ID #{0} called by {1}.{2}()  took {3} ms", id,
                    myCaller.getClassName(),
                    myCaller.getMethodName(), time);
            start = 0L;
            executionTimeSum.addAndGet(time);
        }
    }

    private class ProfilingExecutorService extends ForwardingExecutorService {

        protected ExecutorService delegate() {
            return delegate.getTaskExecutor();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            Measurement measurement = new Measurement();
            try {
                measurement.startProfiling();
                return delegate().invokeAll(tasks);
            } finally {
                measurement.stopProfiling();
            }
        }
    }

    private final ExecutorServices delegate;

    private final AtomicInteger executionId = new AtomicInteger();
    private final AtomicLong executionTimeSum = new AtomicLong();
    private final ProfilingExecutorService wrappedInstance = new ProfilingExecutorService();

    public ProfilingExecutorServices(ExecutorServices delegate) {
        this.delegate = delegate;
        BootstrapLogger.LOG.infov("Delegating to {0}", delegate);
    }

    @Override
    public ExecutorService getTaskExecutor() {
        return wrappedInstance;
    }

    @Override
    public void cleanup() {
        if (!getTaskExecutor().isShutdown()) {
            BootstrapLogger.LOG.infov("Total time spent in ThreadPool execution is {0} ms", executionTimeSum.get());
        }
        delegate.cleanup();
    }

    public <T> List<Future<T>> invokeAllAndCheckForExceptions(Collection<? extends Callable<T>> tasks) {
        Measurement measurement = new Measurement();
        measurement.startProfiling();
        try {
            return delegate.invokeAllAndCheckForExceptions(tasks);
        } finally {
            measurement.stopProfiling();
        }
    }

    @Override
    public <T> List<Future<T>> invokeAllAndCheckForExceptions(TaskFactory<T> factory) {
        Measurement measurement = new Measurement();
        measurement.startProfiling();
        try {
            return delegate.invokeAllAndCheckForExceptions(factory);
        } finally {
            measurement.stopProfiling();
        }
    }

    public ExecutorServices getDelegate() {
        return delegate;
    }
}
