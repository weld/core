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

import java.util.concurrent.TimeUnit;

import org.jboss.weld.logging.BootstrapLogger;
import org.jboss.weld.manager.api.ExecutorServices;

/**
 * Common functionality for {@link ExecutorServices}.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractManagedExecutorServices extends AbstractExecutorServices {

    private static final long SHUTDOWN_TIMEOUT = 60L;

    public void cleanup() {
        getTaskExecutor().shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!getTaskExecutor().awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                getTaskExecutor().shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!getTaskExecutor().awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    // Log the error here
                    BootstrapLogger.LOG.timeoutShuttingDownThreadPool(getTaskExecutor(), this);
                    // log.warn(BootstrapMessage.TIMEOUT_SHUTTING_DOWN_THREAD_POOL, getTaskExecutor(), this);
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            getTaskExecutor().shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
