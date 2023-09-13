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
import java.util.concurrent.Executors;

/**
 * @author pmuir
 */
public class SingleThreadExecutorServices extends AbstractExecutorServices {

    private final transient ExecutorService taskExecutor = Executors.newSingleThreadExecutor();

    /**
     * Provides access to the executor service used for asynchronous tasks.
     *
     * @return the ExecutorService for this manager
     */
    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    @Override
    protected int getThreadPoolSize() {
        return 1;
    }
}
