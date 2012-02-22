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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.jboss.weld.manager.api.ExecutorServices;

/**
 * The factory creates a new {@link Callable} for each item of the source iterable. The list of callables is then returned from
 * the {@link #createTasks(int)} method. The size of the thread pool is not considered.
 * @author Jozef Hartinger
 *
 * @param <T> the type of the processed items
 */
public abstract class TaskPerItemTaskFactory<T> implements ExecutorServices.TaskFactory<Void> {

    private final Iterable<? extends T> iterable;

    public TaskPerItemTaskFactory(Iterable<? extends T> iterable) {
        this.iterable = iterable;
    }

    @Override
    public List<Callable<Void>> createTasks(int threadPoolSize) {
        List<Callable<Void>> tasks = new LinkedList<Callable<Void>>();
        for (final T item : iterable) {
            tasks.add(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    doWork(item);
                    return null;
                }
            });
        }
        return tasks;
    }

    protected abstract void doWork(T item);
}
