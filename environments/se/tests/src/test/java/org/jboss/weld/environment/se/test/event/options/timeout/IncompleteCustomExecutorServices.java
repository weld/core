/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.event.options.timeout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.jboss.weld.executor.AbstractExecutorServices;
import org.jboss.weld.executor.DaemonThreadFactory;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class IncompleteCustomExecutorServices extends AbstractExecutorServices {

    static final String PREFIX = "weld-worker-test";

    private final transient ExecutorService taskExecutor = Executors
            .newSingleThreadExecutor(new DaemonThreadFactory(new ThreadGroup(DaemonThreadFactory.WELD_WORKERS), PREFIX));

    public ExecutorService getTaskExecutor() {
        return taskExecutor;
    }

    @Override
    protected int getThreadPoolSize() {
        return 1;
    }

    @Override
    public ScheduledExecutorService getTimerExecutor() {
        // deliberately return null
        return null;
    }

}
