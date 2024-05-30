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
package org.jboss.weld.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.api.ExecutorServices;

/**
 * Common implementation of {@link ExecutorServices}
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractExecutorServices implements ExecutorServices {

    private static final long SHUTDOWN_TIMEOUT = 60L;

    private final ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(1,
            new DaemonThreadFactory("weld-timer-"));

    /**
     * Returns a singleton instance of ScheduledExecutorService.
     *
     * @return A managed instance of ScheduledExecutorService
     */
    @Override
    public ScheduledExecutorService getTimerExecutor() {
        return timerExecutor;
    }

    @Override
    public <T> List<Future<T>> invokeAllAndCheckForExceptions(Collection<? extends Callable<T>> tasks) {
        try {
            // the cast is needed to compile this expression in JDK 8, works without it on JDK 10+
            return checkForExceptions(getTaskExecutor().invokeAll((Collection<? extends Callable<T>>) wrap(tasks)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeploymentException(e);
        }
    }

    public <T> List<Future<T>> invokeAllAndCheckForExceptions(TaskFactory<T> factory) {
        return invokeAllAndCheckForExceptions(factory.createTasks(getThreadPoolSize()));
    }

    protected <T> List<Future<T>> checkForExceptions(List<Future<T>> futures) {
        for (Future<T> result : futures) {
            try {
                result.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new WeldException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw RuntimeException.class.cast(cause);
                } else {
                    throw new WeldException(cause);
                }
            }
        }
        return futures;
    }

    /**
     * Indicates the maximum number of threads in this thread pool. If the value is unknown or if the max number of threads is
     * not bounded this method should
     * return -1
     */
    protected abstract int getThreadPoolSize();

    @Override
    public void cleanup() {
        shutdown();
    }

    protected void shutdown() {
        getTaskExecutor().shutdown();
        getTimerExecutor().shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!getTaskExecutor().awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                getTaskExecutor().shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!getTaskExecutor().awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    // Log the error here
                    BootstrapLogger.LOG.timeoutShuttingDownThreadPool(getTaskExecutor(), this);
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            getTaskExecutor().shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }

        // Do the same for timer executor
        try {
            if (!getTimerExecutor().isShutdown()) { // no need to do the full wait, one already elapsed
                getTimerExecutor().shutdownNow();
                if (!getTimerExecutor().awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    // Log the error here
                    BootstrapLogger.LOG.timeoutShuttingDownThreadPool(getTimerExecutor(), this);
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            getTimerExecutor().shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This method is invoked with the body of
     * {@link AbstractExecutorServices#invokeAllAndCheckForExceptions(java.util.Collection)}
     *
     * It allows to wrap the tasks with some additional logic. For instance, Weld's {@link CommonForkJoinPoolExecutorServices}
     * overrides this in order to set TCCL to null prior to execution.
     */
    public <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        //no-op by default
        return tasks;
    }
}
